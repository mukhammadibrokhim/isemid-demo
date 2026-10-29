package uz.uzinfocom.app.platform.devpanel.application.query;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevSqlQueryResponse;
import uz.uzinfocom.app.shared.exception.ConflictException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Ad-hoc SQL console for the dev panel ({@code POST /v1/dev/db-schema/query}).
 *
 * <p>Safety is layered, and none of it relies on parsing SQL cleverly:
 * <ol>
 *   <li>OFF unless {@code app.dev.db-schema.query-enabled=true} - the legacy
 *       schema holds real patient data.</li>
 *   <li>One statement only: any {@code ;} other than trailing ones is refused.
 *       This is deliberately blunt (a {@code ';'} string literal is refused
 *       too) because a second statement could be {@code COMMIT}, which would
 *       end the read-only transaction below.</li>
 *   <li>First keyword must be a read form (SELECT/WITH/VALUES/TABLE/EXPLAIN/SHOW).</li>
 *   <li>The statement runs inside {@code SET TRANSACTION READ ONLY}, so Postgres
 *       itself rejects any write (including data-modifying CTEs and
 *       {@code SELECT INTO}), and the transaction is always rolled back.</li>
 *   <li>{@code SET LOCAL statement_timeout} and a row cap bound cost.</li>
 * </ol>
 * {@code EXPLAIN ANALYZE} is refused: it executes the statement.
 */
@Slf4j
@Service
public class DevSqlConsoleService {

    static final int MAX_ROWS = 1000;
    private static final Set<String> READ_KEYWORDS = Set.of("select", "with", "values", "table", "explain", "show");

    private final JdbcTemplate jdbc;
    private final boolean enabled;
    private final int timeoutSeconds;

    public DevSqlConsoleService(
            JdbcTemplate jdbc,
            @Value("${app.dev.db-schema.query-enabled:false}") boolean enabled,
            @Value("${app.dev.db-schema.query-timeout-seconds:15}") int timeoutSeconds
    ) {
        this.jdbc = jdbc;
        this.enabled = enabled;
        this.timeoutSeconds = Math.max(1, timeoutSeconds);
    }

    public DevSqlQueryResponse execute(String rawSql, Integer limit, String actor) {
        if (!enabled) {
            throw new ConflictException("db-schema.query-disabled");
        }
        String sql = requireSingleReadStatement(rawSql);
        int bounded = limit == null ? MAX_ROWS : Math.max(1, Math.min(limit, MAX_ROWS));

        long started = System.nanoTime();
        log.info("dev sql console: actor={} sql={}", actor, abbreviate(sql));
        try {
            DevSqlQueryResponse result = jdbc.execute((Connection con) -> run(con, sql, bounded));
            long ms = (System.nanoTime() - started) / 1_000_000;
            return new DevSqlQueryResponse(result.columns(), result.rows(), result.isTruncated(), ms);
        } catch (org.springframework.dao.DataAccessException e) {
            Throwable root = e.getMostSpecificCause();
            throw new DevSqlRejectedException("db-schema.query-failed", root.getMessage());
        }
    }

    private DevSqlQueryResponse run(Connection con, String sql, int limit) throws SQLException {
        boolean autoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try (Statement st = con.createStatement()) {
            st.execute("SET TRANSACTION READ ONLY");
            st.execute("SET LOCAL statement_timeout = " + (timeoutSeconds * 1000L));
            st.setMaxRows(limit + 1);
            st.setFetchSize(Math.min(limit + 1, 500));
            if (!st.execute(sql)) {
                throw new DevSqlRejectedException("db-schema.query-no-result");
            }
            try (ResultSet rs = st.getResultSet()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                List<String> columns = new ArrayList<>(n);
                for (int i = 1; i <= n; i++) {
                    columns.add(md.getColumnLabel(i));
                }
                List<List<String>> rows = new ArrayList<>();
                boolean truncated = false;
                while (rs.next()) {
                    if (rows.size() == limit) {
                        truncated = true;
                        break;
                    }
                    List<String> row = new ArrayList<>(n);
                    for (int i = 1; i <= n; i++) {
                        row.add(rs.getString(i));
                    }
                    rows.add(row);
                }
                return new DevSqlQueryResponse(columns, rows, truncated, 0);
            }
        } finally {
            try {
                con.rollback();
            } finally {
                con.setAutoCommit(autoCommit);
            }
        }
    }

    static String requireSingleReadStatement(String raw) {
        String sql = raw == null ? "" : raw.strip();
        while (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).stripTrailing();
        }
        if (sql.isEmpty()) {
            throw new DevSqlRejectedException("db-schema.query-empty");
        }
        if (sql.indexOf(';') >= 0) {
            throw new DevSqlRejectedException("db-schema.query-multiple");
        }
        String lower = sql.toLowerCase(Locale.ROOT);
        String first = lower.replaceFirst("^[\\s(]+", "").split("[\\s(]+", 2)[0];
        if (!READ_KEYWORDS.contains(first)) {
            throw new DevSqlRejectedException("db-schema.query-not-read");
        }
        if (first.equals("explain") && lower.matches("(?s)^explain\\s*\\(?[^;]*\\banalyze\\b.*")) {
            throw new DevSqlRejectedException("db-schema.query-not-read");
        }
        return sql;
    }

    private static String abbreviate(String sql) {
        String oneLine = sql.replaceAll("\\s+", " ");
        return oneLine.length() <= 300 ? oneLine : oneLine.substring(0, 300) + "...";
    }
}
