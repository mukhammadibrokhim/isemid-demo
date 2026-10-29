package uz.uzinfocom.app.platform.devpanel.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.devpanel.application.command.DevDbDumpService;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevDbDumpResponse;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevDbDumpStartRequest;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.nio.file.Path;
import java.util.List;

@Tag(
        name = "Dev Panel - DB dumps",
        description = "pg_dump backups of the database (safety copy before the legacy migration). "
                + "ROLE_DEV_SUPER_ADMIN only, and only while app.dev.db-dump.enabled=true."
)
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevDbDumpController {

    private final DevDbDumpService service;
    private final MessageResolver messageResolver;

    @Operation(summary = "Start a dump; returns immediately with status RUNNING")
    @PostMapping(ApiPaths.Dev.DB_DUMPS)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<DevDbDumpResponse> start(@RequestBody(required = false) DevDbDumpStartRequest request) {
        return ApiResponse.success(messageResolver.resolve("common.success"), service.start(request));
    }

    @Operation(summary = "All dumps, newest first")
    @GetMapping(ApiPaths.Dev.DB_DUMPS)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<List<DevDbDumpResponse>> list() {
        return ApiResponse.success(messageResolver.resolve("common.success"), service.list());
    }

    @Operation(summary = "Download a finished dump (pg_dump custom format; restore with pg_restore)")
    @GetMapping(ApiPaths.Dev.DB_DUMP_DOWNLOAD)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        Path file = service.file(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.getFileName().toString()).build().toString())
                .body(new FileSystemResource(file));
    }

    @Operation(summary = "Delete a dump (not while it is RUNNING)")
    @DeleteMapping(ApiPaths.Dev.DB_DUMP_BY_ID)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.success"));
    }
}
