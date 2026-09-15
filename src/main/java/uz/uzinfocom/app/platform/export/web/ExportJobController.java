package uz.uzinfocom.app.platform.export.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uz.uzinfocom.app.platform.export.application.ExportJobService;
import uz.uzinfocom.app.platform.export.application.dto.ExportJobFilterRequest;
import uz.uzinfocom.app.platform.export.application.dto.ExportJobResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.security.annotation.CurrentUser;
import uz.uzinfocom.app.platform.security.principal.PrincipalUser;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generic surface for background Excel export jobs - "my files", SSE progress and
 * download - shared by every module wired up as an {@code ExcelExportSource}. Submitting a
 * new export stays per-module (e.g. {@code POST /v1/form-058/export}); everything after
 * that is the same three endpoints regardless of which module produced the job.
 */
@Tag(
        name = "Exports",
        description = "Фоновые задачи экспорта в Excel: список файлов текущего пользователя, прогресс "
                + "через Server-Sent Events и скачивание готового файла."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Export.ROOT)
@RequiredArgsConstructor
public class ExportJobController {

    private final ExportJobService exportJobService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Мои файлы экспорта",
            description = "Постраничный список задач экспорта, поставленных текущим пользователем, новые сверху."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<ExportJobResponse> findMyJobs(
            @ParameterObject @Valid ExportJobFilterRequest filter,
            @CurrentUser PrincipalUser principal,
            HttpServletRequest httpRequest
    ) {
        Page<ExportJobResponse> page = exportJobService.findMyJobs(principal.id(), filter);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Прогресс задачи экспорта (SSE)",
            description = "Server-Sent Events поток с процентом выполнения задачи; закрывается автоматически "
                    + "после завершения или ошибки."
    )
    @GetMapping(ApiPaths.Export.PROGRESS)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter progress(
            @Parameter(description = "Идентификатор задачи экспорта.", required = true)
            @PathVariable @Positive Long id,
            @CurrentUser PrincipalUser principal
    ) {
        return exportJobService.streamProgress(id, principal.id());
    }

    @Operation(
            summary = "Скачать готовый файл экспорта",
            description = "Доступно только после завершения задачи (статус COMPLETED)."
    )
    @GetMapping(ApiPaths.Export.DOWNLOAD)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(
            @Parameter(description = "Идентификатор задачи экспорта.", required = true)
            @PathVariable @Positive Long id,
            @CurrentUser PrincipalUser principal
    ) {
        Path file = exportJobService.resolveDownloadable(id, principal.id());

        long size;
        try {
            size = Files.size(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(size)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.getFileName().toString(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(new FileSystemResource(file));
    }
}
