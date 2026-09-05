package uz.uzinfocom.app.modules.report.form13.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.report.form13.application.query.Form13ByDiseaseReportQueryService;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13ByDiseaseReportNodeResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * "Form 13 by disease" — the disease-first counterpart of «Form 13», the same
 * relationship «Form 12» has to «Form 13» itself. Корневой уровень — не
 * география, а плоский список записей справочника ручных отчётов ({@code
 * /v1/references/manual-reports}) с тегом типа {@code FORM_13} в поле «Hisobot
 * turi». Числа строки — подтверждённые (status = APPROVED) случаи форм №058 /
 * №058-1, чей заключительный код МКБ-10 (final_icd10_code, без отката к
 * первичному icd10_code) входит в набор кодов этой записи: всего / до 14 лет /
 * до 18 лет, каждый за выбранный период рядом с тем же периодом год назад и
 * разницей. Последней строкой — «Jami» (только записи с includeInTotal).
 * <p>
 * Раскрытие строки — drill-down по географии (республика→регион→район→
 * организация), тем же движком {@code ReportHierarchyService}, что и
 * «Form 13» / «Form 12»; разбивки узла нет. Excel-экспорт таблицы строит
 * фронтенд.
 */
@Tag(
        name = "Report — Form 13 (by disease)",
        description = "«Прямой» (по нозологическим формам) вариант отчёта «Form 13»: строки — записи "
                + "справочника ручных отчётов с типом FORM_13; числа — подтверждённые (status = APPROVED) "
                + "случаи форм №058 + №058-1, чей заключительный код МКБ-10 (final_icd10_code) входит в набор "
                + "кодов МКБ-10 записи (всего / до 14 лет / до 18 лет), за выбранный период в сравнении с тем "
                + "же периодом год назад, в рамках доступа текущей организации. Раскрытие строки — drill-down "
                + "по административной иерархии (республика→регион→район→организация)."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form13Report.ROOT)
@RequiredArgsConstructor
public class Form13ByDiseaseReportController {

    private final Form13ByDiseaseReportQueryService form13ByDiseaseReportQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Нозологические формы + итого",
            description = "Возвращает по одной строке на каждую запись справочника ручных отчётов с типом "
                    + "FORM_13 — со счётами всего / до 14 лет / до 18 лет за выбранный период и за тот же "
                    + "период год назад — плюс последней строкой суммарный итог (\"Jami\") по строкам с "
                    + "включённым флагом includeInTotal. Период по умолчанию — вся история."
    )
    @GetMapping(ApiPaths.Form13Report.BY_DISEASE_ROOT_NODE)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form13ByDiseaseReportNodeResponse>> root(
            @Parameter(description = "Начало периода (включительно). По умолчанию — вся история.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form13ByDiseaseReportQueryService.getRoot(from, to)
        );
    }

    @Operation(
            summary = "География одной нозологической формы",
            description = "Возвращает следующий уровень административной иерархии для указанной "
                    + "нозологической формы (manualReportId) за выбранный период и тот же период год назад. "
                    + "Без regionCode/districtCode — уровень, соответствующий области доступа вызывающего. С "
                    + "regionCode — районы региона; с districtCode — организации района. Запрос за пределами "
                    + "области доступа вызывающего отклоняется."
    )
    @GetMapping(ApiPaths.Form13Report.BY_DISEASE_CHILDREN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form13ByDiseaseReportNodeResponse>> children(
            @Parameter(description = "Id записи справочника ручных отчётов (нозологическая форма).", required = true)
            @RequestParam @NotNull @Positive Long manualReportId,
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
                form13ByDiseaseReportQueryService.getChildren(manualReportId, regionCode, districtCode, from, to)
        );
    }
}
