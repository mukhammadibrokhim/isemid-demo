package uz.uzinfocom.app.modules.report.form32.web;

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
import uz.uzinfocom.app.modules.report.form32.application.command.Form32EntryCommandService;
import uz.uzinfocom.app.modules.report.form32.application.command.dto.Form32EntryCreateRequest;
import uz.uzinfocom.app.modules.report.form32.application.command.dto.Form32EntryUpdateRequest;
import uz.uzinfocom.app.modules.report.form32.application.query.Form32EntryQueryService;
import uz.uzinfocom.app.modules.report.form32.application.query.dto.Form32EntryFilterRequest;
import uz.uzinfocom.app.modules.report.form32.application.query.dto.Form32EntryTableResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

@Tag(
        name = "Report — Form 3-2 Manual Entry",
        description = "API ручных статистических записей «Shakl №3-2» — санитарные проверки (проверенные "
                + "объекты, выявленные нарушения, привлечённые к ответственности должностные лица, "
                + "приостановленные/закрытые объекты), вводимые вручную по создающей организации и "
                + "произвольному периоду."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form32Entry.ROOT)
@RequiredArgsConstructor
public class Form32EntryController {

    private final Form32EntryQueryService form32EntryQueryService;
    private final Form32EntryCommandService form32EntryCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Создать ручную запись Shakl №3-2",
            description = "Создаёт запись для текущей организации вызывающего."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form32EntryTableResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемой записи Shakl №3-2.",
                    required = true
            )
            @Valid @RequestBody Form32EntryCreateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.created"),
                form32EntryCommandService.create(request)
        );
    }

    @Operation(
            summary = "Получить постраничные данные записей Shakl №3-2",
            description = "Возвращает записи в рамках области доступа текущей организации (собственная " +
                    "организация плюс все организации в её области видимости — регион/район/республика), " +
                    "каждая со своей создавшей организацией и худудом."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<Form32EntryTableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute Form32EntryFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<Form32EntryTableResponse> page = form32EntryQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Обновить запись Shakl №3-2",
            description = "Разрешено только организации, создавшей запись, либо isemid_admin/isemid_super_admin."
    )
    @PutMapping(ApiPaths.Form32Entry.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form32EntryTableResponse> update(
            @Parameter(description = "Внутренний идентификатор записи.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные записи Shakl №3-2.",
                    required = true
            )
            @Valid @RequestBody Form32EntryUpdateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                form32EntryCommandService.update(id, request)
        );
    }

    @Operation(
            summary = "Удалить запись Shakl №3-2",
            description = "Разрешено только организации, создавшей запись, либо isemid_admin/isemid_super_admin."
    )
    @DeleteMapping(ApiPaths.Form32Entry.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> delete(
            @Parameter(description = "Внутренний идентификатор записи.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        form32EntryCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
