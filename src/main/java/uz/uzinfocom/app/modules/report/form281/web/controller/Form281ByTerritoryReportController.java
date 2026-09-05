package uz.uzinfocom.app.modules.report.form281.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.report.form281.application.query.Form281ByTerritoryReportQueryService;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281ByTerritoryNodeResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * "Form 28.1 by territory" — the geography-first counterpart of «Form 28.1»,
 * the same relationship «Form 13» has to «Form 12»: rows are the
 * administrative hierarchy (republic→region→district→organization, one level
 * per call via the shared {@code ReportHierarchyService}), columns are
 * diseases — every manual-report catalog entry tagged {@code FORM_28_1}, in a
 * stable per-response order. Same underlying data and access rules as {@code
 * Form281ReportController}; only the row/column axes are swapped.
 */
@Tag(
        name = "Report — Form 28.1 (by territory)",
        description = "«Перевёрнутый» (по территориям) вариант отчёта «Form 28.1»: строки — административная "
                + "иерархия (республика→регион→район→организация) в рамках доступа текущей организации; "
                + "столбцы — записи справочника ручных отчётов с тегом FORM_28_1. Числа — подтверждённые "
                + "(status = APPROVED, deleted = false) случаи форм №058 + №058-1, чей заключительный код "
                + "МКБ-10 (final_icd10_code) входит в набор кодов записи, за выбранный период. Один период — "
                + "без сравнения год назад."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form281Report.ROOT)
@RequiredArgsConstructor
public class Form281ByTerritoryReportController {

    private final Form281ByTerritoryReportQueryService form281ByTerritoryReportQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Первый уровень иерархии + итого",
            description = "Возвращает первый уровень иерархии в рамках области доступа текущей организации "
                    + "(регионы — для республиканского доступа, районы — для областного, организации — для "
                    + "районного), каждый со столбцами-болезнями за выбранный период, плюс последней строкой "
                    + "суммарный итог (\"Jami\") по всей области доступа. Период по умолчанию — вся история."
    )
    @GetMapping(ApiPaths.Form281Report.BY_TERRITORY_ROOT_NODE)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form281ByTerritoryNodeResponse>> root(
            @Parameter(description = "Начало периода (включительно). По умолчанию — вся история.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form281ByTerritoryReportQueryService.getRoot(from, to)
        );
    }

    @Operation(
            summary = "Дочерние узлы отчёта",
            description = "Возвращает следующий уровень иерархии за выбранный период. Без "
                    + "regionCode/districtCode — уровень, соответствующий области доступа вызывающего. С "
                    + "regionCode — районы региона; с districtCode — организации района. Запрос за пределами "
                    + "области доступа вызывающего отклоняется."
    )
    @GetMapping(ApiPaths.Form281Report.BY_TERRITORY_CHILDREN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form281ByTerritoryNodeResponse>> children(
            @Parameter(description = "Код региона (необязательный).")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Код района (необязательный).")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "Начало периода (включительно). По умолчанию — вся история.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form281ByTerritoryReportQueryService.getChildren(regionCode, districtCode, from, to)
        );
    }
}
