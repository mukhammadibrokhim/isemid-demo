package uz.uzinfocom.app.modules.report.form2.manual.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.report.form2.manual.application.command.Form2ManualEntryCommandService;
import uz.uzinfocom.app.modules.report.form2.manual.application.command.dto.Form2ManualEntryCreateRequest;
import uz.uzinfocom.app.modules.report.form2.manual.application.command.dto.Form2ManualEntryUpdateRequest;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.Form2ManualEntryQueryService;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.dto.Form2ManualEntryFilterRequest;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.dto.Form2ManualEntryPrefillResponse;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.dto.Form2ManualEntryTableResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

import java.time.LocalDate;

@Tag(
        name = "Report — Form 2 Manual Entry",
        description = "API ручных статистических записей «Shakl №2» — данные, не учитываемые нигде в системе " +
                "(дезинфекция, проверенные/закрытые объекты, ДДУ, школы, штрафы, передано в прокуратуру), " +
                "вводимые вручную по создающей организации и произвольному периоду."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form2ManualEntry.ROOT)
@RequiredArgsConstructor
public class Form2ManualEntryController {

    private final Form2ManualEntryQueryService form2ManualEntryQueryService;
    private final Form2ManualEntryCommandService form2ManualEntryCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Автозаполнение формы создания",
            description = "Возвращает худуд и оба автоматически вычисляемых показателя (\"O'tgan yilda\"/" +
                    "\"Joriy yilda qayd etilgan holatlar\") для текущей организации вызывающего за указанный " +
                    "период — используется для предзаполнения полей формы \"Shakl №2 yaratish\" перед вводом " +
                    "остальных данных вручную."
    )
    @GetMapping(ApiPaths.Form2ManualEntry.PREFILL)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<Form2ManualEntryPrefillResponse> prefill(
            @Parameter(description = "Начало периода отчёта.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода отчёта.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form2ManualEntryQueryService.prefill(from, to)
        );
    }

    @Operation(
            summary = "Создать ручную запись Shakl №2",
            description = "Создаёт запись для текущей организации вызывающего. Автоматически вычисляемые " +
                    "показатели пересчитываются сервером по переданному периоду, а не принимаются из запроса."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_CREATE')")
    public ApiResponse<Form2ManualEntryTableResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемой записи Shakl №2.",
                    required = true
            )
            @Valid @RequestBody Form2ManualEntryCreateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.created"),
                form2ManualEntryCommandService.create(request)
        );
    }

    @Operation(
            summary = "Получить постраничные данные записей Shakl №2",
            description = "Возвращает записи в рамках области доступа текущей организации (собственная " +
                    "организация плюс все организации в её области видимости — регион/район/республика), " +
                    "каждая со своей создавшей организацией и худудом."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public PagedResponse<Form2ManualEntryTableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute Form2ManualEntryFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<Form2ManualEntryTableResponse> page = form2ManualEntryQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Обновить запись Shakl №2",
            description = "Разрешено только организации, создавшей запись, либо isemid_admin/isemid_super_admin. " +
                    "Автоматически вычисляемые показатели пересчитываются сервером по переданному периоду."
    )
    @PutMapping(ApiPaths.Form2ManualEntry.BY_ID)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_UPDATE')")
    public ApiResponse<Form2ManualEntryTableResponse> update(
            @Parameter(description = "Внутренний идентификатор записи.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные записи Shakl №2.",
                    required = true
            )
            @Valid @RequestBody Form2ManualEntryUpdateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                form2ManualEntryCommandService.update(id, request)
        );
    }

    @Operation(
            summary = "Удалить запись Shakl №2",
            description = "Разрешено только организации, создавшей запись, либо isemid_admin/isemid_super_admin."
    )
    @DeleteMapping(ApiPaths.Form2ManualEntry.BY_ID)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_DELETE')")
    public ApiResponse<Void> delete(
            @Parameter(description = "Внутренний идентификатор записи.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        form2ManualEntryCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
