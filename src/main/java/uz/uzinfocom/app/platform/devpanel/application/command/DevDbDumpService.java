package uz.uzinfocom.app.platform.devpanel.application.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevDbDumpResponse;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevDbDumpStartRequest;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Database backups ({@code pg_dump -Fc}) taken from the dev panel, so a safety copy exists
 * before the destructive legacy migration run.
 *
 * <p>Each dump is a {@code <id>.dump} file plus a {@code <id>.properties} sidecar (status,
 * schemas, who, when, error) in {@code app.dev.db-dump.dir} - no table, the listing survives
 * restarts. A dump left {@code RUNNING} by a restart is reported as {@code FAILED}. Needs the
 * {@code pg_dump} binary on the host ({@code app.dev.db-dump.pg-dump-path}); one dump at a time,
 * off by default ({@code app.dev.db-dump.enabled}).
 */
@Service
public class DevDbDumpService {

    private static final Logger log = LoggerFactory.getLogger(DevDbDumpService.class);

    private static final Pattern ID = Pattern.compile("\\d{8}-\\d{6}-[0-9a-f]{4}");
    private static final Pattern SCHEMA = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,62}");
    private static final List<String> DEFAULT_SCHEMAS = List.of("public", "public2");
    private static final DateTimeFormatter ID_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);
    private static final int MAX_ERROR_CHARS = 4000;
    private static final long TIMEOUT_MINUTES = 120;

    private final SecureRandom random = new SecureRandom();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "dev-db-dump");
        t.setDaemon(true);
        return t;
    });

    private final boolean enabled;
    private final Path dir;
    private final String pgDump;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private volatile String runningId;

    public DevDbDumpService(
            @Value("${app.dev.db-dump.enabled:false}") boolean enabled,
            @Value("${app.dev.db-dump.dir:./db-dumps}") String dir,
            @Value("${app.dev.db-dump.pg-dump-path:pg_dump}") String pgDump,
            @Value("${spring.datasource.url}") String jdbcUrl,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password
    ) {
        this.enabled = enabled;
        this.dir = Path.of(dir).toAbsolutePath().normalize();
        this.pgDump = pgDump;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public synchronized DevDbDumpResponse start(DevDbDumpStartRequest request) {
        requireEnabled();
        if (runningId != null) {
            throw new ConflictException("db-dump.already-running");
        }
        List<String> schemas = resolveSchemas(request == null ? null : request.schemas());

        String id = ID_TIME.format(Instant.now()) + "-" + "%04x".formatted(random.nextInt(0x10000));
        Meta meta = new Meta(id, schemas, Instant.now(), actor(), "RUNNING", null);
        try {
            Files.createDirectories(dir);
            store(meta);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write to the dump directory " + dir, e);
        }
        runningId = id;
        executor.execute(() -> execute(meta));
        log.warn("DB dump {} started by {} (schemas={})", id, meta.createdBy, schemas);
        return toResponse(meta);
    }

    public List<DevDbDumpResponse> list() {
        requireEnabled();
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> p.getFileName().toString().endsWith(".properties"))
                    .map(p -> load(stripExt(p)))
                    .filter(m -> m != null)
                    .sorted(Comparator.comparing(Meta::createdAt).reversed())
                    .map(this::toResponse)
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot list the dump directory " + dir, e);
        }
    }

    /** The dump file for a SUCCEEDED dump. */
    public Path file(String id) {
        requireEnabled();
        Meta meta = require(id);
        if (!"SUCCEEDED".equals(effectiveStatus(meta))) {
            throw new ConflictException("db-dump.not-ready");
        }
        return dumpPath(id);
    }

    public synchronized void delete(String id) {
        requireEnabled();
        Meta meta = require(id);
        if ("RUNNING".equals(effectiveStatus(meta))) {
            throw new ConflictException("db-dump.running");
        }
        try {
            Files.deleteIfExists(dumpPath(id));
            Files.deleteIfExists(metaPath(id));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot delete dump " + id, e);
        }
        log.warn("DB dump {} deleted by {}", id, actor());
    }

    private void execute(Meta meta) {
        Path out = dumpPath(meta.id);
        String status = "FAILED";
        String error = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command(meta, out)).redirectErrorStream(true);
            pb.environment().put("PGPASSWORD", password == null ? "" : password);
            Process p = pb.start();
            String output;
            try (InputStream in = p.getInputStream()) {
                output = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
            }
            if (!p.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                p.destroyForcibly();
                error = "pg_dump timed out after " + TIMEOUT_MINUTES + " minutes";
            } else if (p.exitValue() != 0) {
                error = output.isEmpty() ? "pg_dump exited with code " + p.exitValue() : output;
            } else {
                status = "SUCCEEDED";
            }
        } catch (IOException e) {
            error = "Cannot run pg_dump (" + pgDump + "): " + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            error = "Interrupted";
        } catch (Exception e) {
            error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        } finally {
            if (!"SUCCEEDED".equals(status)) {
                try {
                    Files.deleteIfExists(out);
                } catch (IOException ignored) {
                    // best effort: a partial file is reported FAILED and can be deleted via the API
                }
            }
            try {
                store(new Meta(meta.id, meta.schemas, meta.createdAt, meta.createdBy, status,
                        error == null ? null : truncate(error)));
            } catch (IOException e) {
                log.error("Cannot record the result of DB dump {}", meta.id, e);
            }
            runningId = null;
            log.warn("DB dump {} finished: {}", meta.id, status);
        }
    }

    private List<String> command(Meta meta, Path out) {
        URI uri = URI.create(jdbcUrl.startsWith("jdbc:") ? jdbcUrl.substring(5) : jdbcUrl);
        String db = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
        List<String> cmd = new ArrayList<>(List.of(pgDump,
                "-h", uri.getHost() == null ? "localhost" : uri.getHost(),
                "-p", String.valueOf(uri.getPort() > 0 ? uri.getPort() : 5432),
                "-U", username,
                "-d", db,
                "-Fc", "--no-owner", "--no-password",
                "-f", out.toString()));
        for (String s : meta.schemas) {
            cmd.add("-n");
            cmd.add(s);
        }
        return cmd;
    }

    private static List<String> resolveSchemas(List<String> requested) {
        if (requested == null || requested.isEmpty()) {
            return DEFAULT_SCHEMAS;
        }
        List<String> out = new ArrayList<>();
        for (String s : requested) {
            if (s == null || !SCHEMA.matcher(s).matches()) {
                throw new ConflictException("db-dump.invalid-schema", s);
            }
            if (!out.contains(s)) {
                out.add(s);
            }
        }
        return out;
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new ConflictException("db-dump.disabled");
        }
    }

    private Meta require(String id) {
        Meta meta = id != null && ID.matcher(id).matches() ? load(id) : null;
        if (meta == null) {
            throw new NotFoundException("db-dump.not-found");
        }
        return meta;
    }

    /** A RUNNING record with no live dump behind it was cut off by a restart. */
    private String effectiveStatus(Meta meta) {
        if ("RUNNING".equals(meta.status) && !meta.id.equals(runningId)) {
            return "FAILED";
        }
        return meta.status;
    }

    private DevDbDumpResponse toResponse(Meta meta) {
        String status = effectiveStatus(meta);
        String error = meta.error;
        if (!status.equals(meta.status) && error == null) {
            error = "Interrupted (application restarted during the dump)";
        }
        long size = 0;
        try {
            Path f = dumpPath(meta.id);
            if (Files.exists(f)) {
                size = Files.size(f);
            }
        } catch (IOException ignored) {
            // size stays 0
        }
        return new DevDbDumpResponse(meta.id, meta.id + ".dump", meta.schemas, size,
                meta.createdAt, meta.createdBy, status, error);
    }

    private Meta load(String id) {
        Path p = metaPath(id);
        if (!Files.isRegularFile(p)) {
            return null;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(p)) {
            props.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            String schemas = props.getProperty("schemas", "");
            return new Meta(id,
                    schemas.isEmpty() ? List.of() : List.of(schemas.split(",")),
                    Instant.parse(props.getProperty("createdAt")),
                    props.getProperty("createdBy", "unknown"),
                    props.getProperty("status", "FAILED"),
                    props.getProperty("error"));
        } catch (Exception e) {
            log.warn("Unreadable dump record {}", p, e);
            return null;
        }
    }

    private void store(Meta meta) throws IOException {
        Properties props = new Properties();
        props.setProperty("schemas", String.join(",", meta.schemas));
        props.setProperty("createdAt", meta.createdAt.toString());
        props.setProperty("createdBy", meta.createdBy);
        props.setProperty("status", meta.status);
        if (meta.error != null) {
            props.setProperty("error", meta.error);
        }
        try (OutputStream out = Files.newOutputStream(metaPath(meta.id))) {
            props.store(new java.io.OutputStreamWriter(out, StandardCharsets.UTF_8), null);
        }
    }

    private Path dumpPath(String id) {
        return dir.resolve(id + ".dump");
    }

    private Path metaPath(String id) {
        return dir.resolve(id + ".properties");
    }

    private static String stripExt(Path p) {
        String n = p.getFileName().toString();
        return n.substring(0, n.length() - ".properties".length());
    }

    private static String truncate(String s) {
        return s.length() <= MAX_ERROR_CHARS ? s : s.substring(0, MAX_ERROR_CHARS) + "...";
    }

    private static String actor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "unknown" : auth.getName();
    }

    private record Meta(String id, List<String> schemas, Instant createdAt, String createdBy,
                        String status, String error) {
    }
}
