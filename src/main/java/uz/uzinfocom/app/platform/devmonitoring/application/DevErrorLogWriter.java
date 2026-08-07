package uz.uzinfocom.app.platform.devmonitoring.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevErrorResponse;
import uz.uzinfocom.app.platform.devmonitoring.application.stream.DevSseBroadcaster;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevErrorLog;
import uz.uzinfocom.app.platform.devmonitoring.repository.DevErrorLogRepository;

import java.time.Instant;

/**
 * Persists one row per failed (status &gt;= 400) request, called from
 * {@code RequestLoggingFilter.logOnce(...)} with fields it has already
 * extracted - no duplicate parsing here. Runs on the app's existing
 * {@code applicationTaskExecutor} so this write never adds latency to the
 * request being logged; if the executor is saturated, only this audit row
 * is dropped (logged), never the original request/response.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DevErrorLogWriter {

    private final DevErrorLogRepository devErrorLogRepository;
    private final DevSseBroadcaster devSseBroadcaster;

    @Async("applicationTaskExecutor")
    @Transactional
    public void record(
            String traceId,
            String errorCode,
            int httpStatus,
            String exceptionType,
            String path,
            String method,
            String principal,
            String message
    ) {
        try {
            DevErrorLog entry = DevErrorLog.builder()
                    .traceId(traceId)
                    .errorCode(errorCode)
                    .httpStatus(httpStatus)
                    .exceptionType(exceptionType)
                    .path(path)
                    .method(method)
                    .principal(principal)
                    .message(message)
                    .occurredAt(Instant.now())
                    .build();

            devErrorLogRepository.save(entry);
        } catch (RuntimeException persistenceFailure) {
            log.warn("event=dev_error_log_write_failure traceId={} errorCode={} failureType={}",
                    traceId, errorCode, persistenceFailure.getClass().getName());
        }
    }
}
