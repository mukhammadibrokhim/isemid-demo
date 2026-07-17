package uz.uzinfocom.app.platform.reference.web.manualreport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.reference.application.manualreport.command.ManualReportCommandService;
import uz.uzinfocom.app.platform.reference.application.manualreport.dto.ManualReportCreateRequest;
import uz.uzinfocom.app.platform.reference.application.manualreport.dto.ManualReportUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.ManualReportQueryService;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.dto.ManualReportFilterRequest;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.dto.ManualReportTableResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

import java.util.List;

@Tag(
        name = "Reference - Manual Reports",
        description = "API для управления справочником ручных отчётов. Ручные отчёты группируют коды диагнозов " +
                "МКБ-10 в именованные, настраиваемые администратором группы отчётности, используемые для " +
                "эпидемиологической отчётности."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Reference.MANUAL_REPORTS)
@RequiredArgsConstructor
public class ManualReportController {

    private final ManualReportQueryService manualReportQueryService;
    private final ManualReportCommandService manualReportCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Получить постраничные данные ручных отчётов",
            description = """
                    Возвращает активные записи справочника ручных отчётов в виде постраничной таблицы.

                    Поддерживаемые фильтры: code, name.
                    Нумерация страниц начинается с 1.
                    Поддерживаемые поля сортировки: id, code, shortName, nameUz, nameUzCyril, nameRu, nameKaa, createdAt, updatedAt.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Ручные отчёты успешно получены."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<ManualReportTableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute ManualReportFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<ManualReportTableResponse> page = manualReportQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить ручной отчёт по идентификатору",
            description = "Возвращает одну активную запись ручного отчёта по внутреннему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Ручной отчёт успешно получен."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ManualReportResponse> getById(
            @Parameter(description = "Внутренний идентификатор ручного отчёта.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), manualReportQueryService.getById(id));
    }

    @Operation(
            summary = "Получить ручной отчёт по коду",
            description = "Возвращает одну активную запись ручного отчёта по его нормализованному коду."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Ручной отчёт успешно получен."
    )
    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ManualReportResponse> getByCode(
            @Parameter(description = "Код ручного отчёта.", required = true, example = "TUBERCULOSIS")
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), manualReportQueryService.getByCode(code));
    }

    @Operation(
            summary = "Получить ручные отчёты по коду МКБ-10",
            description = "Возвращает все активные ручные отчёты, в набор кодов МКБ-10 которых входит указанный " +
                    "код диагноза. Используется для определения, в какую группу(ы) отчётности засчитывается " +
                    "диагноз формы №058."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Ручные отчёты успешно получены."
    )
    @GetMapping(ApiPaths.Reference.BY_MKB10_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ManualReportResponse>> getByMkb10Code(
            @Parameter(description = "Код диагноза по МКБ-10.", required = true, example = "A15")
            @PathVariable @NotBlank @Size(max = 20) String code
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                manualReportQueryService.getByMkb10Code(code)
        );
    }

    @Operation(
            summary = "Создать ручной отчёт",
            description = "Создаёт новую запись ручного отчёта. Код нормализуется, а коды МКБ-10 обрезаются от " +
                    "пробелов и приводятся к верхнему регистру перед сохранением."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Ручной отчёт успешно создан."
    )
    @PostMapping
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<ManualReportResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемого ручного отчёта.",
                    required = true
            )
            @Valid @RequestBody ManualReportCreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), manualReportCommandService.create(request));
    }

    @Operation(
            summary = "Обновить ручной отчёт",
            description = "Обновляет существующую активную запись ручного отчёта по внутреннему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Ручной отчёт успешно обновлён."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<ManualReportResponse> update(
            @Parameter(description = "Внутренний идентификатор ручного отчёта.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные ручного отчёта.",
                    required = true
            )
            @Valid @RequestBody ManualReportUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), manualReportCommandService.update(id, request));
    }

    @Operation(
            summary = "Удалить ручной отчёт",
            description = "Мягко удаляет запись ручного отчёта. Удалённые записи исключаются из эндпоинтов чтения."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Ручной отчёт успешно удалён."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Void> delete(
            @Parameter(description = "Внутренний идентификатор ручного отчёта.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        manualReportCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
