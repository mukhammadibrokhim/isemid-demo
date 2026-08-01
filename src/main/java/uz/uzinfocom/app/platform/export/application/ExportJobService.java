package uz.uzinfocom.app.platform.export.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.export.application.dto.ExportJobFilterRequest;
import uz.uzinfocom.app.platform.export.application.dto.ExportJobResponse;
import uz.uzinfocom.app.platform.export.application.exception.ExportJobNotFoundException;
import uz.uzinfocom.app.platform.export.application.exception.ExportNotReadyException;
import uz.uzinfocom.app.platform.export.application.exception.ExportTooLargeException;
import uz.uzinfocom.app.platform.export.config.ExportProperties;
import uz.uzinfocom.app.platform.export.domain.ExportJob;
import uz.uzinfocom.app.platform.export.domain.ExportStatus;
import uz.uzinfocom.app.platform.export.repository.ExportJobRepository;
import uz.uzinfocom.app.shared.excel.ExcelColumn;
import uz.uzinfocom.app.shared.excel.ExcelExportWriter;
import uz.uzinfocom.app.shared.excel.ExcelRowWriter;
import uz.uzinfocom.app.shared.excel.ExcelStyleSettings;
import uz.uzinfocom.app.shared.excel.ExcelTitleBlock;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Runs {@link ExcelExportSource} implementations as background jobs: counts matching rows
 * up front (rejecting anything past {@link ExportProperties#getMaxRows()} before queuing
 * any work), streams the export to a local file off the request thread via the app's shared
 * {@code applicationTaskExecutor}, tracks progress on the {@link ExportJob} row, and exposes
 * that progress over SSE for the "my files" panel. See {@link ExcelExportSource} for how a
 * new module plugs into this without touching anything below.
 */
@Slf4j
@Service
public class ExportJobService {

    private static final long PROGRESS_UPDATE_EVERY_N_ROWS = 500;
    private static final long SSE_TIMEOUT_MILLIS = 15 * 60 * 1000L;

    private final ExportJobRepository exportJobRepository;
    private final ExcelExportWriter excelExportWriter;
    private final ExcelExportSettingsResolver excelExportSettingsResolver;
    private final ExportProperties exportProperties;
    private final Executor applicationTaskExecutor;
    private final ScheduledExecutorService sseScheduler;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public ExportJobService(
            ExportJobRepository exportJobRepository,
            ExcelExportWriter excelExportWriter,
            ExcelExportSettingsResolver excelExportSettingsResolver,
            ExportProperties exportProperties,
            @Qualifier("applicationTaskExecutor") Executor applicationTaskExecutor,
            PlatformTransactionManager transactionManager
    ) {
        this.exportJobRepository = exportJobRepository;
        this.excelExportWriter = excelExportWriter;
        this.excelExportSettingsResolver = excelExportSettingsResolver;
        this.exportProperties = exportProperties;
        this.applicationTaskExecutor = applicationTaskExecutor;
        this.sseScheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "export-progress-sse");
            thread.setDaemon(true);
            return thread;
        });

        // Progress updates are written while the exporting ExcelExportSource's own
        // read-only transaction is still open on the same thread (see forEachRow's
        // contract) - PROPAGATION_REQUIRES_NEW suspends that transaction and opens an
        // independent read-write one so each progress write both (a) actually commits,
        // rather than silently running against a read-only connection, and (b) becomes
        // visible immediately to the SSE poller on another thread, instead of waiting for
        // the whole export to finish.
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    }

    public <F, T> ExportJobResponse submit(ExcelExportSource<F, T> source, F filter) {
        long matchedRows = source.count(filter);
        if (matchedRows > exportProperties.getMaxRows()) {
            throw new ExportTooLargeException(matchedRows, exportProperties.getMaxRows());
        }

        ExportJob job = ExportJob.builder()
                .exportType(source.exportType())
                .status(ExportStatus.PENDING)
                .totalRows(matchedRows)
                .processedRows(0L)
                .build();
        job = exportJobRepository.save(job);

        Long jobId = job.getId();
        Runnable exportTask = withCallerContext(() -> runExport(jobId, source, filter));

        try {
            applicationTaskExecutor.execute(exportTask);
        } catch (RejectedExecutionException saturated) {
            log.error("event=export_job_rejected jobId={} exportType={}", jobId, source.exportType());
            failJob(jobId, "Export queue is saturated");
            throw saturated;
        }

        return toResponse(job);
    }

    /**
     * {@code runExport} re-resolves the caller's organization scope (and, for direction=ALL
     * exports, their super-admin authority) from scratch via the same
     * {@code resolveSpecification} the list endpoint uses - but it runs on an
     * {@code applicationTaskExecutor} worker thread, not the original request thread, so the
     * plain {@link CurrentOrganizationContext} ThreadLocal and Spring Security's
     * thread-local {@link SecurityContext} are both empty there by default. Captured here,
     * on the calling (request) thread where they're actually populated, and re-applied on
     * whichever worker thread ends up running the task - {@code CurrentOrganizationContext}
     * holds an already-detached {@code Organization} (see {@code OrganizationContextFilter}),
     * so reusing it after the request's own transaction has closed is safe.
     */
    private Runnable withCallerContext(Runnable task) {
        Organization callerOrganization = CurrentOrganizationContext.getOptional().orElse(null);
        SecurityContext callerSecurityContext = SecurityContextHolder.getContext();

        return () -> {
            if (callerOrganization != null) {
                CurrentOrganizationContext.set(callerOrganization);
            }
            SecurityContextHolder.setContext(callerSecurityContext);

            try {
                task.run();
            } finally {
                CurrentOrganizationContext.clear();
                SecurityContextHolder.clearContext();
            }
        };
    }

    private <F, T> void runExport(Long jobId, ExcelExportSource<F, T> source, F filter) {
        markProcessing(jobId);

        List<ExcelColumn<T>> columns = excelExportSettingsResolver.resolveColumns(source);
        ExcelStyleSettings style = excelExportSettingsResolver.resolveStyle(source);
        List<ExcelTitleBlock> titleBlocks = source.titleBlocks(filter);

        Path targetFile = resolveTargetFile(jobId, source.fileNamePrefix());
        AtomicLong processed = new AtomicLong();

        try {
            Files.createDirectories(targetFile.getParent());

            try (OutputStream out = Files.newOutputStream(targetFile);
                 ExcelRowWriter<T> rowWriter = excelExportWriter.open(out, source.sheetName(), titleBlocks, columns, style)) {

                source.forEachRow(filter, row -> {
                    rowWriter.write(row);
                    long count = processed.incrementAndGet();
                    if (count % PROGRESS_UPDATE_EVERY_N_ROWS == 0) {
                        updateProgress(jobId, count);
                    }
                });
            }

            long fileSize = Files.size(targetFile);
            completeJob(jobId, targetFile, fileSize);
        } catch (Exception exception) {
            log.error("event=export_job_failed jobId={} exportType={}", jobId, source.exportType(), exception);
            failJob(jobId, exception.getMessage());
            deleteQuietly(targetFile);
        }
    }

    private Path resolveTargetFile(Long jobId, String fileNamePrefix) {
        Path dir = Path.of(exportProperties.getStorageDir());
        String fileName = fileNamePrefix + "_" + jobId + ".xlsx";
        return dir.resolve(fileName);
    }

    private void markProcessing(Long jobId) {
        exportJobRepository.findById(jobId).ifPresent(job -> {
            job.markProcessing(job.getTotalRows() == null ? 0 : job.getTotalRows());
            exportJobRepository.save(job);
        });
    }

    private void updateProgress(Long jobId, long processedRows) {
        requiresNewTransactionTemplate.executeWithoutResult(status ->
                exportJobRepository.findById(jobId).ifPresent(job -> {
                    job.updateProgress(processedRows);
                    exportJobRepository.save(job);
                })
        );
    }

    private void completeJob(Long jobId, Path file, long fileSizeBytes) {
        exportJobRepository.findById(jobId).ifPresent(job -> {
            job.updateProgress(job.getTotalRows() == null ? job.getProcessedRows() : job.getTotalRows());
            job.markCompleted(file.getFileName().toString(), file.toString(), fileSizeBytes);
            exportJobRepository.save(job);
        });
    }

    private void failJob(Long jobId, String errorMessage) {
        exportJobRepository.findById(jobId).ifPresent(job -> {
            job.markFailed(errorMessage);
            exportJobRepository.save(job);
        });
    }

    private void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // best-effort cleanup of a partially written export
        }
    }

    public Page<ExportJobResponse> findMyJobs(Long currentUserId, ExportJobFilterRequest filter) {
        Pageable pageable = PageRequest.of(
                normalizePage(filter.page()),
                normalizeSize(filter.size()),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ExportJob> page = filter.exportType() != null && !filter.exportType().isBlank()
                ? exportJobRepository.findByCreatedByAndExportType(currentUserId, filter.exportType(), pageable)
                : exportJobRepository.findByCreatedBy(currentUserId, pageable);

        return page.map(this::toResponse);
    }

    public SseEmitter streamProgress(Long jobId, Long currentUserId) {
        ExportJob job = exportJobRepository.findByIdAndCreatedBy(jobId, currentUserId)
                .orElseThrow(() -> new ExportJobNotFoundException(jobId));

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[1];

        futureHolder[0] = sseScheduler.scheduleAtFixedRate(
                () -> pushProgress(job.getId(), emitter),
                0, 1, TimeUnit.SECONDS
        );

        emitter.onCompletion(() -> futureHolder[0].cancel(false));
        emitter.onTimeout(() -> {
            futureHolder[0].cancel(false);
            emitter.complete();
        });
        emitter.onError(exception -> futureHolder[0].cancel(false));

        return emitter;
    }

    private void pushProgress(Long jobId, SseEmitter emitter) {
        exportJobRepository.findById(jobId).ifPresentOrElse(job -> {
            try {
                emitter.send(SseEmitter.event().name("progress").data(toResponse(job)));
                if (job.isTerminal()) {
                    emitter.complete();
                }
            } catch (IOException | IllegalStateException clientGone) {
                // The listener (browser tab closed, network drop, emitter already timed out,
                // etc.) is simply no longer there - a routine disconnect, not a server error.
                // completeWithError() would hand this exception to Spring MVC's normal
                // error-response machinery, which then fails trying to write a JSON
                // ErrorResponse body onto a response already committed as
                // text/event-stream ("No converter for ... with preset Content-Type
                // 'text/event-stream'") - so this just stops quietly instead.
                emitter.complete();
            }
        }, emitter::complete);
    }

    public Path resolveDownloadable(Long jobId, Long currentUserId) {
        ExportJob job = exportJobRepository.findByIdAndCreatedBy(jobId, currentUserId)
                .orElseThrow(() -> new ExportJobNotFoundException(jobId));

        if (job.getStatus() != ExportStatus.COMPLETED || job.getFilePath() == null) {
            throw new ExportNotReadyException(jobId);
        }

        return Path.of(job.getFilePath());
    }

    @Scheduled(cron = "${app.export.cleanup-cron}")
    public void cleanupExpiredExports() {
        Instant cutoff = Instant.now().minus(exportProperties.getRetentionDays(), ChronoUnit.DAYS);

        List<ExportJob> expired = new ArrayList<>();
        expired.addAll(exportJobRepository.findByStatusAndCompletedAtBefore(ExportStatus.COMPLETED, cutoff));
        expired.addAll(exportJobRepository.findByStatusAndCompletedAtBefore(ExportStatus.FAILED, cutoff));

        for (ExportJob job : expired) {
            if (job.getFilePath() != null) {
                deleteQuietly(Path.of(job.getFilePath()));
            }
            exportJobRepository.delete(job);
        }

        if (!expired.isEmpty()) {
            log.info("event=export_job_cleanup removedCount={}", expired.size());
        }
    }

    private ExportJobResponse toResponse(ExportJob job) {
        return new ExportJobResponse(
                job.getId(),
                job.getExportType(),
                job.getStatus(),
                job.progressPercent(),
                job.getProcessedRows(),
                job.getTotalRows(),
                job.getFileName(),
                job.getFileSizeBytes(),
                job.getErrorMessage(),
                job.getCreatedAt(),
                job.getCompletedAt()
        );
    }

    private int normalizePage(Integer page) {
        int value = page == null ? 1 : page;
        return Math.max(value, 1) - 1;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return 20;
        }
        return Math.min(size, 200);
    }
}
