package uz.uzinfocom.app.platform.devpanel.application.query;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevDbRowsResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevDbSchemaResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevDbSchemaResponse.SchemaInfo;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevDbSchemaResponse.TableInfo;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevDbTableResponse;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only look at the two schemas the legacy migration moves between
 * ({@code public} = legacy, {@code public2} = new). Structure only, plus an
 * optional bounded row sample that is OFF by default - the legacy schema holds
 * real patient data.
 *
 * <p>Identifiers are never taken from the caller into SQL: the schema must be
 * one of the two allowed names and the table must exist in the catalog, and the
 * resulting names are quoted by Postgres itself ({@code quote_ident}).
 */
@Service
public class DevDbSchemaQueryService {

    static final List<String> ALLOWED_SCHEMAS = List.of("public", "public2");
    private static final int MAX_ROW_LIMIT = 100;

    private final JdbcTemplate jdbc;
    private final boolean rowPreviewEnabled;

    public DevDbSchemaQueryService(
            JdbcTemplate jdbc,
            @Value("${app.dev.db-schema.row-preview-enabled:false}") boolean rowPreviewEnabled
    ) {
        this.jdbc = jdbc;
        this.rowPreviewEnabled = rowPreviewEnabled;
    }

    public DevDbSchemaResponse findSchemas(boolean exact) {
        List<SchemaInfo> out = new ArrayList<>();
        for (String schema : ALLOWED_SCHEMAS) {
            List<TableInfo> tables = new ArrayList<>();
            List<String> names = jdbc.queryForList("""
                    select c.relname from pg_class c join pg_namespace n on n.oid = c.relnamespace
                    where n.nspname = ? and c.relkind in ('r', 'p') order by c.relname
                    """, String.class, schema);
            for (String name : names) {
                long bytes = jdbc.queryForObject(
                        "select pg_total_relation_size(to_regclass(quote_ident(?) || '.' || quote_ident(?)))",
                        Long.class, schema, name);
                if (exact) {
                    long rows = jdbc.queryForObject(
                            "select count(*) from " + qualified(schema, name), Long.class);
                    tables.add(new TableInfo(name, rows, true, bytes));
                } else {
                    long est = jdbc.queryForObject("""
                            select c.reltuples::bigint from pg_class c
                            where c.oid = to_regclass(quote_ident(?) || '.' || quote_ident(?))
                            """, Long.class, schema, name);
                    tables.add(new TableInfo(name, est, false, bytes));
                }
            }
            out.add(new SchemaInfo(schema, tables));
        }
        return new DevDbSchemaResponse(out);
    }

    public DevDbTableResponse findTable(String schema, String table) {
        requireTable(schema, table);

        var columns = jdbc.query("""
                select column_name, data_type, udt_name, is_nullable, character_maximum_length, column_default
                from information_schema.columns where table_schema = ? and table_name = ?
                order by ordinal_position
                """, (rs, i) -> {
            String type = "USER-DEFINED".equals(rs.getString("data_type"))
                    ? rs.getString("udt_name") : rs.getString("data_type");
            int len = rs.getInt("character_maximum_length");
            Integer maxLength = rs.wasNull() ? null : len;
            return new DevDbTableResponse.Column(
                    rs.getString("column_name"), type, "YES".equals(rs.getString("is_nullable")),
                    maxLength, rs.getString("column_default"));
        }, schema, table);

        var constraints = jdbc.query("""
                select conname, contype, pg_get_constraintdef(oid) as def from pg_constraint
                where conrelid = to_regclass(quote_ident(?) || '.' || quote_ident(?))
                order by contype, conname
                """, (rs, i) -> new DevDbTableResponse.Constraint(
                rs.getString("conname"), constraintType(rs.getString("contype")), rs.getString("def")),
                schema, table);

        var indexes = jdbc.query("""
                select indexname, indexdef from pg_indexes where schemaname = ? and tablename = ?
                order by indexname
                """, (rs, i) -> new DevDbTableResponse.Index(rs.getString(1), rs.getString(2)), schema, table);

        return new DevDbTableResponse(schema, table, columns, constraints, indexes);
    }

    public DevDbRowsResponse findRows(String schema, String table, int limit) {
        if (!rowPreviewEnabled) {
            throw new ConflictException("db-schema.row-preview-disabled");
        }
        requireTable(schema, table);
        int bounded = Math.max(1, Math.min(limit, MAX_ROW_LIMIT));

        return jdbc.query("select * from " + qualified(schema, table) + " limit " + bounded, rs -> {
            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();
            List<String> columns = new ArrayList<>(n);
            for (int i = 1; i <= n; i++) {
                columns.add(md.getColumnLabel(i));
            }
            List<List<String>> rows = new ArrayList<>();
            while (rs.next()) {
                List<String> row = new ArrayList<>(n);
                for (int i = 1; i <= n; i++) {
                    row.add(rs.getString(i));
                }
                rows.add(row);
            }
            return new DevDbRowsResponse(schema, table, columns, rows, bounded);
        });
    }

    private void requireTable(String schema, String table) {
        if (!ALLOWED_SCHEMAS.contains(schema)) {
            throw new NotFoundException("db-schema.not-found", schema);
        }
        Integer found = jdbc.queryForObject("""
                select count(*) from pg_class c join pg_namespace n on n.oid = c.relnamespace
                where n.nspname = ? and c.relname = ? and c.relkind in ('r', 'p')
                """, Integer.class, schema, table);
        if (found == null || found == 0) {
            throw new NotFoundException("db-schema.not-found", schema + "." + table);
        }
    }

    /** Both parts already verified against the catalog; quoted by Postgres, not by string concatenation. */
    private String qualified(String schema, String table) {
        return jdbc.queryForObject("select quote_ident(?) || '.' || quote_ident(?)", String.class, schema, table);
    }

    private static String constraintType(String c) {
        return switch (c) {
            case "p" -> "PRIMARY KEY";
            case "f" -> "FOREIGN KEY";
            case "u" -> "UNIQUE";
            case "c" -> "CHECK";
            case "x" -> "EXCLUDE";
            default -> c;
        };
    }
}
