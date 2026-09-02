package uz.uzinfocom.app.modules.report.form7.web;

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
import uz.uzinfocom.app.modules.report.form7.application.command.Form7EntryCommandService;
import uz.uzinfocom.app.modules.report.form7.application.command.dto.Form7EntryCreateRequest;
import uz.uzinfocom.app.modules.report.form7.application.command.dto.Form7EntryUpdateRequest;
import uz.uzinfocom.app.modules.report.form7.application.query.Form7EntryQueryService;
import uz.uzinfocom.app.modules.report.form7.application.query.dto.Form7EntryFilterRequest;
import uz.uzinfocom.app.modules.report.form7.application.query.dto.Form7EntryPrefillResponse;
import uz.uzinfocom.app.modules.report.form7.application.query.dto.Form7EntryTableResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

import java.time.LocalDate;

@Tag(
        name = "Report — Form 7 Manual Entry",
        description = "API ручных статистических записей «Shakl №7» — движение регистра инфекционных "
                + "заболеваний за отчётный период (случаи на начало периода, вновь зарегистрированные "
                + "пациенты с возрастно-половыми и город/село срезами, наблюдение, случаи на конец периода, "
                + "прирост/убыль). Возрастно-половые срезы и «birlamchi tashxis tasdiqlandi» вычисляются "
                + "сервером по form058 + form058_1, остальное вводится вручную."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form7Entry.ROOT)
@RequiredArgsConstructor
public class Form7EntryController {

    private final Form7EntryQueryService form7EntryQueryService;
    private final Form7EntryCommandService form7EntryCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Автозаполнение формы создания",
            description = "Возвращает худуд и автоматически вычисляемый блок «Hisobot davrida ro'yxatga "
                    + "olingan bemorlar» (Jami / 14 yoshgacha / 18 yoshgacha / kattalar / ayollar) плюс "
                    + "«birlamchi tashxis tasdiqlandi» для текущей организации вызывающего за указанный "
                    + "период — используется для предзаполнения формы \"Shakl №7 yaratish\" перед вводом "
                    + "остальных данных вручную."
    )
    @GetMapping(ApiPaths.Form7Entry.PREFILL)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<Form7EntryPrefillResponse> prefill(
            @Parameter(description = "Начало периода отчёта.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода отчёта.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form7EntryQueryService.prefill(from, to)
        );
    }

    @Operation(
            summary = "Создать ручную запись Shakl №7",
            description = "Создаёт запись для текущей организации вызывающего. Автоматически вычисляемый "
                    + "блок пересчитывается сервером по переданному периоду, а не принимается из запроса."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_CREATE')")
    public ApiResponse<Form7EntryTableResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемой записи Shakl №7.",
                    required = true
            )
            @Valid @RequestBody Form7EntryCreateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.created"),
                form7EntryCommandService.create(request)
        );
    }

    @Operation(
            summary = "Получить постраничные данные записей Shakl №7",
            description = "Возвращает записи в рамках области доступа текущей организации (собственная "
                    + "организация плюс все организации в её области видимости — регион/район/республика), "
                    + "каждая со своей создавшей организацией и худудом."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public PagedResponse<Form7EntryTableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute Form7EntryFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<Form7EntryTableResponse> page = form7EntryQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Обновить запись Shakl №7",
            description = "Разрешено только организации, создавшей запись, либо isemid_admin/isemid_super_admin. "
                    + "Автоматически вычисляемый блок пересчитывается сервером по переданному периоду."
    )
    @PutMapping(ApiPaths.Form7Entry.BY_ID)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_UPDATE')")
    public ApiResponse<Form7EntryTableResponse> update(
            @Parameter(description = "Внутренний идентификатор записи.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные записи Shakl №7.",
                    required = true
            )
            @Valid @RequestBody Form7EntryUpdateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                form7EntryCommandService.update(id, request)
        );
    }

    @Operation(
            summary = "Удалить запись Shakl №7",
            description = "Разрешено только организации, создавшей запись, либо isemid_admin/isemid_super_admin."
    )
    @DeleteMapping(ApiPaths.Form7Entry.BY_ID)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_DELETE')")
    public ApiResponse<Void> delete(
            @Parameter(description = "Внутренний идентификатор записи.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        form7EntryCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
