package uz.uzinfocom.app.platform.devpanel.application.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevLegacyMigrationResponse;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevLegacyMigrationResponse.Step;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevLegacyMigrationResponse.Table;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevLegacyMigrationStartRequest;
import uz.uzinfocom.app.shared.exception.ConflictException;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Runs the one-off legacy ({@code public}) -> {@code public2} data migration
 * (the SQL under {@code classpath:legacy-migration/}, the same files
 * {@code db/legacy-migration/run.sh} runs through psql) from the dev panel.
 *
 * <p>The files are written for psql: this strips the psql-only lines
 * ({@code \set}, {@code \echo}) and the files' own {@code BEGIN}/{@code COMMIT},
 * and supplies the transaction itself. A real run commits file by file (as the
 * runner scripts do, so a failing file rolls back alone and the run stops); a
 * dry run keeps everything in a single transaction and rolls it back at the end.
 *
 * <p>Runs on its own thread with a dedicated connection - the pool's
 * {@code statement_timeout}/{@code idle_in_transaction_session_timeout} are
 * lifted for that transaction only. One run at a time.
 */
@Service
public class DevLegacyMigrationService {

    private static final Logger log = LoggerFactory.getLogger(DevLegacyMigrationService.class);

    private static final String RESOURCE_DIR = "legacy-migration/";
    private static final int MAX_ROWS_PER_TABLE = 500;
    private static final int MAX_NOTICES_PER_STEP = 200;

    /** The full ordered migration (the same list as run.sh / run.ps1). */
    static final List<String> FULL_RUN = List.of(
            "00-prep.sql", "05-legacy-compat.sql", "10-organization.sql", "20-users.sql", "30-patient.sql",
            "40-form058.sql", "45-form058-1.sql",
            "50-card.sql", "51-card161.sql", "52-card174.sql", "53-card175.sql", "54-card205.sql",
            "55-card-tube.sql", "60-act.sql", "61-act-subtypes.sql", "62-preserve-unmapped.sql", "90-finalize.sql",
            "97-backfill-form058-accept-approve-info.sql"
    );

    /** Standalone, safe-to-rerun fixes that are not part of the full run. */
    static final List<String> STANDALONE = List.of(
            "95-fix-sequences.sql", "96-fix-organization-provider-key.sql"
    );

    private static final Pattern PSQL_META_LINE = Pattern.compile("(?m)^[ \\t]*\\\\.*$");
    private static final Pattern TX_CONTROL_LINE = Pattern.compile("(?im)^[ \\t]*(BEGIN|COMMIT)[ \\t]*;[ \\t]*$");

    private final DataSource dataSource;
    private final boolean enabled;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "dev-legacy-migration");
        t.setDaemon(true);
        return t;
    });
    private final AtomicLong runSeq = new AtomicLong();
    private volatile Run current;

    public DevLegacyMigrationService(
            DataSource dataSource,
            @Value("${app.legacy-migration.enabled:false}") boolean enabled
    ) {
        this.dataSource = dataSource;
        this.enabled = enabled;
    }

    public synchronized DevLegacyMigrationResponse start(DevLegacyMigrationStartRequest request) {
        if (!enabled) {
            throw new ConflictException("legacy-migration.disabled");
        }
        boolean dryRun = request == null || request.dryRun() == null || request.dryRun();
        if (!dryRun && (request.confirm() == null || !"yes".equals(request.confirm().trim()))) {
            throw new ConflictException("legacy-migration.confirm-required");
        }
        if (current != null && current.isRunning()) {
            throw new ConflictException("legacy-migration.already-running");
        }
        List<String> files = resolveFiles(request == null ? null : request.files());

        Run run = new Run(runSeq.incrementAndGet(), dryRun, actor(), files);
        current = run;
        executor.execute(() -> execute(run));
        log.warn("Legacy migration run {} started by {} (dryRun={}, files={})",
                run.id, run.startedBy, dryRun, files.size());
        return run.snapshot();
    }

    /** Latest run, or {@code null} if none has been started since the application came up. */
    public DevLegacyMigrationResponse status() {
        Run run = current;
        return run == null ? null : run.snapshot();
    }

    private List<String> resolveFiles(List<String> requested) {
        if (requested == null || requested.isEmpty()) {
            return FULL_RUN;
        }
        List<String> allowed = new ArrayList<>(FULL_RUN);
        allowed.addAll(STANDALONE);
        for (String f : requested) {
            if (!allowed.contains(f)) {
                throw new ConflictException("legacy-migration.unknown-file", f);
            }
        }
        // Canonical order regardless of how the caller listed them (FK order matters).
        return allowed.stream().filter(requested::contains).toList();
    }

    private void execute(Run run) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (StepState step : run.steps) {
                    run.begin(step);
                    long t0 = System.nanoTime();
                    try {
                        runFile(conn, step);
                        if (!run.dryRun) {
                            conn.commit();
                        }
                        run.finish(step, null, ms(t0));
                    } catch (Exception e) {
                        conn.rollback();
                        String msg = rootMessage(e);
                        run.finish(step, msg, ms(t0));
                        run.fail(step.file + ": " + msg);
                        log.error("Legacy migration run {} failed at {}", run.id, step.file, e);
                        return;
                    }
                }
                if (run.dryRun) {
                    conn.rollback();
                }
                run.succeed();
                log.warn("Legacy migration run {} finished OK (dryRun={})", run.id, run.dryRun);
            } finally {
                resetSession(conn);
            }
        } catch (Exception e) {
            log.error("Legacy migration run {} aborted", run.id, e);
            run.fail(rootMessage(e));
        }
    }

    private void runFile(Connection conn, StepState step) throws SQLException, IOException {
        String sql = TX_CONTROL_LINE.matcher(
                PSQL_META_LINE.matcher(load(step.file)).replaceAll("")
        ).replaceAll("");

        try (Statement st = conn.createStatement()) {
            st.setEscapeProcessing(false);
            // Lifted for this transaction only (SET LOCAL) - the pool's 60s statement cap would
            // kill the big INSERT ... SELECTs.
            st.execute("SET LOCAL statement_timeout = 0");
            st.execute("SET LOCAL idle_in_transaction_session_timeout = '30min'");
            st.execute("SET LOCAL client_min_messages = warning");

            boolean isResultSet = st.execute(sql);
            while (true) {
                if (isResultSet) {
                    try (ResultSet rs = st.getResultSet()) {
                        step.addTable(readTable(rs));
                    }
                } else if (st.getUpdateCount() == -1) {
                    break;
                }
                isResultSet = st.getMoreResults();
            }
            for (SQLWarning w = st.getWarnings(); w != null; w = w.getNextWarning()) {
                if (!step.addNotice(w.getMessage())) {
                    break;
                }
            }
        }
    }

    private static Table readTable(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int n = md.getColumnCount();
        List<String> columns = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            columns.add(md.getColumnLabel(i));
        }
        List<List<String>> rows = new ArrayList<>();
        boolean truncated = false;
        while (rs.next()) {
            if (rows.size() >= MAX_ROWS_PER_TABLE) {
                truncated = true;
                break;
            }
            List<String> row = new ArrayList<>(n);
            for (int i = 1; i <= n; i++) {
                row.add(rs.getString(i));
            }
            rows.add(row);
        }
        return new Table(columns, rows, truncated);
    }

    /** The scripts run {@code SET TIME ZONE}; undo it so the pooled connection is clean again. */
    private static void resetSession(Connection conn) {
        try {
            conn.rollback();
            conn.setAutoCommit(true);
            try (Statement st = conn.createStatement()) {
                st.execute("RESET TIME ZONE");
            }
        } catch (SQLException e) {
            log.warn("Could not reset the migration connection's session state", e);
        }
    }

    private static String load(String file) throws IOException {
        ClassPathResource resource = new ClassPathResource(RESOURCE_DIR + file);
        try (var in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String actor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "unknown" : auth.getName();
    }

    private static long ms(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private static String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
    }

    /** Mutable per-file state; only touched under the owning {@link Run}'s lock for reads. */
    private static final class StepState {
        final String file;
        volatile String status = "PENDING";
        volatile long durationMs;
        volatile String error;
        private final List<String> notices = new ArrayList<>();
        private final List<Table> tables = new ArrayList<>();

        StepState(String file) {
            this.file = file;
        }

        synchronized void addTable(Table t) {
            tables.add(t);
        }

        /** @return false once the per-step cap is reached. */
        synchronized boolean addNotice(String n) {
            if (notices.size() >= MAX_NOTICES_PER_STEP) {
                return false;
            }
            notices.add(n);
            return true;
        }

        synchronized Step toDto() {
            return new Step(file, status, durationMs, error, List.copyOf(notices), List.copyOf(tables));
        }
    }

    private static final class Run {
        final long id;
        final boolean dryRun;
        final String startedBy;
        final Instant startedAt = Instant.now();
        final List<StepState> steps;
        private String status = "RUNNING";
        private Instant finishedAt;
        private String error;

        Run(long id, boolean dryRun, String startedBy, List<String> files) {
            this.id = id;
            this.dryRun = dryRun;
            this.startedBy = startedBy;
            this.steps = files.stream().map(StepState::new).toList();
        }

        synchronized boolean isRunning() {
            return "RUNNING".equals(status);
        }

        void begin(StepState s) {
            s.status = "RUNNING";
        }

        void finish(StepState s, String err, long durationMs) {
            s.durationMs = durationMs;
            s.error = err;
            s.status = err == null ? "OK" : "FAILED";
        }

        synchronized void succeed() {
            status = "SUCCEEDED";
            finishedAt = Instant.now();
        }

        synchronized void fail(String message) {
            status = "FAILED";
            error = message;
            finishedAt = Instant.now();
        }

        synchronized DevLegacyMigrationResponse snapshot() {
            List<Step> out = new ArrayList<>(steps.size());
            for (StepState s : steps) {
                out.add(s.toDto());
            }
            return new DevLegacyMigrationResponse(id, status, dryRun, startedBy, startedAt, finishedAt, error, out);
        }
    }
}
