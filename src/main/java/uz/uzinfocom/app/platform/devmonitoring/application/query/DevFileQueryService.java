package uz.uzinfocom.app.platform.devmonitoring.application.query;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevFileEntryResponse;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevFileListResponse;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevFileRoot;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.exception.SecurityException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Read-only browsing/download of two local directories for the dev-monitoring
 * panel: the repo's {@code docs/} folder and the app's own log directory
 * (whatever {@code logging.file.path} resolves to in logback-spring.xml,
 * "logs" by default - including its {@code archive/} subfolder of rotated
 * {@code .log.gz} files, which is just a normal subdirectory under LOGS).
 * <p>
 * Every incoming relative path is resolved against its base directory and
 * re-checked with {@link Path#startsWith(Path)} after normalization, and
 * rejected outright if it parses as absolute - both are required because
 * {@link Path#resolve(Path)} returns the argument as-is (ignoring the base)
 * when it is itself absolute.
 */
@Service
public class DevFileQueryService {

    private final Path docsRoot;
    private final Path logsRoot;

    public DevFileQueryService(
            @Value("${app.dev-files.docs-dir:docs}") String docsDir,
            @Value("${logging.file.path:logs}") String logsDir
    ) {
        this.docsRoot = Path.of(docsDir).toAbsolutePath().normalize();
        this.logsRoot = Path.of(logsDir).toAbsolutePath().normalize();
    }

    public DevFileListResponse list(DevFileRoot root, String relativePath) {
        Path base = baseOf(root);
        Path target = resolveWithinBase(base, relativePath);

        if (!Files.isDirectory(target)) {
            throw new NotFoundException();
        }

        try (Stream<Path> children = Files.list(target)) {
            List<DevFileEntryResponse> entries = children
                    .sorted(Comparator.<Path>comparingInt(child -> Files.isDirectory(child) ? 0 : 1)
                            .thenComparing(child -> child.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .map(child -> toEntry(base, child))
                    .toList();
            return new DevFileListResponse(root, relativize(base, target), entries);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path resolveDownloadable(DevFileRoot root, String relativePath) {
        Path target = resolveWithinBase(baseOf(root), relativePath);
        if (!Files.isRegularFile(target)) {
            throw new NotFoundException();
        }
        return target;
    }

    private Path baseOf(DevFileRoot root) {
        return switch (root) {
            case DOCS -> docsRoot;
            case LOGS -> logsRoot;
        };
    }

    private Path resolveWithinBase(Path base, String relativePath) {
        String safeRelative = relativePath == null ? "" : relativePath.trim();

        Path candidate;
        try {
            candidate = Path.of(safeRelative);
        } catch (InvalidPathException e) {
            throw new SecurityException("error.forbidden");
        }

        if (candidate.isAbsolute()) {
            throw new SecurityException("error.forbidden");
        }

        Path resolved = base.resolve(candidate).normalize();
        if (!resolved.equals(base) && !resolved.startsWith(base)) {
            throw new SecurityException("error.forbidden");
        }
        return resolved;
    }

    private DevFileEntryResponse toEntry(Path base, Path child) {
        try {
            boolean directory = Files.isDirectory(child);
            return new DevFileEntryResponse(
                    child.getFileName().toString(),
                    relativize(base, child),
                    directory,
                    directory ? null : Files.size(child),
                    Files.getLastModifiedTime(child).toInstant()
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String relativize(Path base, Path target) {
        String relative = base.relativize(target).toString().replace('\\', '/');
        return relative.isEmpty() ? "" : relative;
    }
}
