package uz.uzinfocom.app.modules.report.form31.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.report.form31.application.command.Form31EntryCommandService;
import uz.uzinfocom.app.modules.report.form31.application.command.dto.Form31EntryCreateRequest;
import uz.uzinfocom.app.modules.report.form31.application.command.dto.Form31EntryUpdateRequest;
import uz.uzinfocom.app.modules.report.form31.application.query.Form31EntryQueryService;
import uz.uzinfocom.app.modules.report.form31.application.query.dto.Form31EntryFilterRequest;
import uz.uzinfocom.app.modules.report.form31.application.query.dto.Form31EntryTableResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

@Tag(
        name = "Report — Form 3-1 Manual Entry",
        description = "API ручных статистических записей «Shakl №3-1» — ILI/SARI (гриппоподобные заболевания / "
                + "тяжёлые острые респираторные инфекции) сентинельное наблюдение и охват вакцинацией против "
                + "гриппа, вводимые вручную по создающей организации и произвольному периоду."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form31Entry.ROOT)
@RequiredArgsConstructor
public class Form31EntryController {

    private final Form31EntryQueryService form31EntryQueryService;
    private final Form31EntryCommandService form31EntryCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Создать ручную запись Shakl №3-1",
            description = "Создаёт запись для текущей организации вызывающего."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_CREATE')")
    public ApiResponse<Form31EntryTableResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемой записи Shakl №3-1.",
                    required = true
            )
            @Valid @RequestBody Form31EntryCreateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.created"),
                form31EntryCommandService.create(request)
        );
    }

    @Operation(
            summary = "Получить постраничные данные записей Shakl №3-1",
            description = "Возвращает записи в рамках области доступа текущей организации (собственная " +
                    "организация плюс все организации в её области видимости — регион/район/республика), " +
                    "каждая со своей создавшей организацией и худудом."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public PagedResponse<Form31EntryTableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute Form31EntryFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<Form31EntryTableResponse> page = form31EntryQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Обновить запись Shakl №3-1",
            description = "Разрешено только организации, создавшей запись, либо isemid_admin/isemid_super_admin."
    )
    @PutMapping(ApiPaths.Form31Entry.BY_ID)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_UPDATE')")
    public ApiResponse<Form31EntryTableResponse> update(
            @Parameter(description = "Внутренний идентификатор записи.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные записи Shakl №3-1.",
                    required = true
            )
            @Valid @RequestBody Form31EntryUpdateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                form31EntryCommandService.update(id, request)
        );
    }

    @Operation(
            summary = "Удалить запись Shakl №3-1",
            description = "Разрешено только организации, создавшей запись, либо isemid_admin/isemid_super_admin."
    )
    @DeleteMapping(ApiPaths.Form31Entry.BY_ID)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_DELETE')")
    public ApiResponse<Void> delete(
            @Parameter(description = "Внутренний идентификатор записи.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        form31EntryCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
