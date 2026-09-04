package uz.uzinfocom.app.modules.report.form281.web.controller;

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
import uz.uzinfocom.app.modules.report.form281.application.query.Form281ReportQueryService;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281ReportNodeResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * "Form 28.1" — «Ayrim yuqumli va parazitar kasalliklar haqida ma'lumotlar»,
 * структурно клон «Form 12»: корневой уровень — плоский список записей
 * справочника ручных отчётов ({@code /v1/references/manual-reports}) с тегом
 * типа {@code FORM_28_1}; раскрытие строки — drill-down по административной
 * иерархии (республика→регион→район→организация) для этой нозологической формы
 * тем же движком {@code ReportHierarchyService}. Числа строки — подтверждённые
 * (status = APPROVED, deleted = false) случаи форм №058 + №058-1, чей
 * заключительный код МКБ-10 ({@code final_icd10_code}, без отката к первичному
 * {@code icd10_code}) входит в набор кодов записи, за один произвольный период
 * {@code [from, to]} (без сравнения год назад). Метрики — столбцы варакаи: всего
 * / женщины / до 17 включ. / до 14 включ. / до 1 года / 1–2 года / 3–5 лет, и те
 * же возрастные срезы отдельно по сельскому населению. Последней строкой —
 * «Jami» (только записи с includeInTotal). Excel-экспорт таблицы строит
 * фронтенд из JSON.
 */
@Tag(
        name = "Report — Form 28.1",
        description = "API отчёта «Form 28.1: сведения об отдельных инфекционных и паразитарных "
                + "заболеваниях». Строки — записи справочника ручных отчётов с типом FORM_28_1; числа — "
                + "подтверждённые (status = APPROVED, deleted = false) случаи форм №058 + №058-1, чей "
                + "заключительный код МКБ-10 (final_icd10_code) входит в набор кодов МКБ-10 записи, за "
                + "выбранный период, в рамках доступа текущей организации. Метрики — всего / женщины / до "
                + "17 включ. / до 14 включ. / до 1 года / 1–2 года / 3–5 лет, и те же возрастные срезы по "
                + "сельскому населению. Раскрытие строки — drill-down по административной иерархии "
                + "(республика→регион→район→организация)."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form281Report.ROOT)
@RequiredArgsConstructor
public class Form281ReportController {

    private final Form281ReportQueryService form281ReportQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Нозологические формы + итого",
            description = "Возвращает по одной строке на каждую запись справочника ручных отчётов с типом "
                    + "FORM_28_1 — с метриками варакаи за выбранный период — плюс последней строкой "
                    + "суммарный итог (\"Jami\") по строкам с включённым флагом includeInTotal. Период по "
                    + "умолчанию — вся история."
    )
    @GetMapping(ApiPaths.Form281Report.ROOT_NODE)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form281ReportNodeResponse>> root(
            @Parameter(description = "Начало периода (включительно). По умолчанию — вся история.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form281ReportQueryService.getRoot(from, to)
        );
    }

    @Operation(
            summary = "География одной нозологической формы",
            description = "Возвращает следующий уровень административной иерархии для указанной "
                    + "нозологической формы (manualReportId) за выбранный период. Без "
                    + "regionCode/districtCode — уровень, соответствующий области доступа вызывающего. С "
                    + "regionCode — районы региона; с districtCode — организации района. Запрос за "
                    + "пределами области доступа вызывающего отклоняется."
    )
    @GetMapping(ApiPaths.Form281Report.CHILDREN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form281ReportNodeResponse>> children(
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
                form281ReportQueryService.getChildren(manualReportId, regionCode, districtCode, from, to)
        );
    }
}
