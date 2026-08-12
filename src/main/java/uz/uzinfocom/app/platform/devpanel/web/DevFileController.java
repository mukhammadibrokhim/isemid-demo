package uz.uzinfocom.app.platform.devpanel.web;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.devpanel.application.query.DevFileQueryService;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevFileListResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevFileRoot;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Tag(
        name = "Dev Monitoring - Files",
        description = "Read-only browsing and download of the app's docs/ folder and its log directory "
                + "(including the rotated .log.gz archives under logs/archive), for the internal developer "
                + "monitoring panel."
)
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevFileController {

    private final DevFileQueryService devFileQueryService;
    private final MessageResolver messageResolver;

    @GetMapping(ApiPaths.Dev.FILES)
    public ApiResponse<DevFileListResponse> list(
            @Parameter(description = "Which base directory to browse.", required = true)
            @RequestParam DevFileRoot root,
            @Parameter(description = "Path relative to the base directory. Empty/omitted lists the root itself.")
            @RequestParam(defaultValue = "") String path
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), devFileQueryService.list(root, path));
    }

    @GetMapping(ApiPaths.Dev.FILES_DOWNLOAD)
    public ResponseEntity<Resource> download(
            @Parameter(description = "Which base directory the file lives under.", required = true)
            @RequestParam DevFileRoot root,
            @Parameter(description = "Path of the file, relative to the base directory.", required = true)
            @RequestParam String path
    ) {
        Path file = devFileQueryService.resolveDownloadable(root, path);

        String contentType;
        long size;
        try {
            contentType = Files.probeContentType(file);
            size = Files.size(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(size)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.getFileName().toString(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(new FileSystemResource(file));
    }
}
