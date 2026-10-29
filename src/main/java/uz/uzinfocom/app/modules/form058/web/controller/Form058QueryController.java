package uz.uzinfocom.app.modules.form058.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.modules.form058.application.export.Form058ExcelExportSource;
import uz.uzinfocom.app.modules.form058.application.query.Form058AffiliatedFilter;
import uz.uzinfocom.app.modules.form058.application.query.Form058Filter;
import uz.uzinfocom.app.modules.form058.application.query.Form058QueryService;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058AffiliatedTableResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058TableResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.detail.Form058DetailResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.pdf.Form058PdfResponse;
import uz.uzinfocom.app.platform.export.application.ExportJobService;
import uz.uzinfocom.app.platform.export.application.dto.ExportJobResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(
        name = "Form 058",
        description = "Управление формой №058 — экстренным извещением об инфекционном заболевании: "
                + "создание, редактирование, удаление, утверждение и аннулирование."
)
@RequestMapping(ApiPaths.Form058.ROOT)
public class Form058QueryController {

    private final Form058QueryService form058QueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;
    private final ExportJobService exportJobService;
    private final Form058ExcelExportSource form058ExcelExportSource;

    @Operation(
            summary = "Список форм №058",
            description = "Возвращает постраничный список форм с возможностью фильтрации по статусу, "
                    + "организациям-отправителю/получателю и другим параметрам."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<Form058TableResponse> findAll(
            @ParameterObject @Valid Form058Filter filter,
            HttpServletRequest httpRequest
    ) {
        return pagedResponseAssembler
                .toResponse(form058QueryService.findAll(filter), messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Список форм №058, доступных через affiliation пациента",
            description = "Возвращает постраничный список форм, видимых текущей организации не как "
                    + "отправителю/получателю, а потому что место работы или учёбы пациента (affiliation) "
                    + "совпадает с текущей организацией — например, для санэпидстанции, обслуживающей "
                    + "предприятие или учебное заведение. Отдельный эндпоинт вместо флага в общем списке: "
                    + "область видимости здесь принципиально другая (не sender/receiver), поэтому и "
                    + "набор фильтров отличается — нет direction и organizationId. Каждая строка дополнительно "
                    + "помечена типом принадлежности (affiliationType: WORKPLACE/EDUCATIONAL), объясняющим, "
                    + "почему форма видна именно этой организации."
    )
    @GetMapping(ApiPaths.Form058.AFFILIATED)
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<Form058AffiliatedTableResponse> findAllAffiliated(
            @ParameterObject @Valid Form058AffiliatedFilter filter,
            HttpServletRequest httpRequest
    ) {
        return pagedResponseAssembler
                .toResponse(form058QueryService.findAllAffiliated(filter), messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Найти форму №058 по ПИНФЛ пациента",
            description = "Возвращает детальные сведения о форме по идентификационному номеру физического "
                    + "лица (ПИНФЛ/nnuzb) пациента."
    )
    @GetMapping("/by-nnuzb")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form058DetailResponse> byNnuzb(
            @Parameter(description = "Идентификационный номер физического лица (ПИНФЛ) пациента.", required = true)
            @RequestParam @NotBlank String nnuzb
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form058QueryService.getByNnuzb(nnuzb)
        );
    }

    @Operation(
            summary = "Получить форму №058 по идентификатору",
            description = "Возвращает полные детальные сведения по форме, включая клиническую, "
                    + "эпидемиологическую информацию и данные пациента."
    )
    @GetMapping(ApiPaths.Form058.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form058DetailResponse> byId(
            @Parameter(description = "Идентификатор формы №058.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form058QueryService.getById(id)
        );
    }

    @Operation(
            summary = "Сведения формы №058 для печатного бланка",
            description = "Возвращает сведения по форме в виде, готовом для печатного бланка формы №058: "
                    + "все коды справочников (регион, район, пол, семейное положение, профессия, место "
                    + "возникновения заболевания и т.д.) приведены к человекочитаемым наименованиям."
    )
    @GetMapping(ApiPaths.Form058.PDF)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form058PdfResponse> pdf(
            @Parameter(description = "Идентификатор формы №058.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form058QueryService.getPdf(id)
        );
    }

    @Operation(
            summary = "Экспорт списка форм №058 в Excel",
            description = "Ставит в очередь фоновую задачу экспорта в Excel по тем же фильтрам, что и список "
                    + "форм. Прогресс и скачивание готового файла — через /v1/exports (см. соответствующие "
                    + "методы)."
    )
    @PostMapping(ApiPaths.Form058.EXPORT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ExportJobResponse> export(
            @ParameterObject @Valid Form058Filter filter
    ) {
        return ApiResponse.success(
                messageResolver.resolve("export.job.submitted"),
                exportJobService.submit(form058ExcelExportSource, filter)
        );
    }
}