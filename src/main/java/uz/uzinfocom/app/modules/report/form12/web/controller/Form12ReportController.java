package uz.uzinfocom.app.modules.report.form12.web.controller;

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
import uz.uzinfocom.app.modules.report.form12.application.query.Form12ReportQueryService;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12ReportNodeResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * "Form 12" — «Nozologik shakllar bo'yicha yuqumli va parazitar kasalliklar
 * bo'yicha ma'lumotlar», данные по инфекционным и паразитарным болезням в
 * разрезе нозологических форм. Корневой уровень — не география, а плоский
 * список нозологических форм: каждая строка — запись справочника ручных
 * отчётов ({@code /v1/references/manual-reports}) с тегом типа отчёта {@code
 * FORM_12} в поле «Hisobot turi». Числа строки — подтверждённые ({@code status
 * = 'APPROVED'}) случаи форм №058 / №058-1, чей подтверждённый диагноз ({@code
 * coalesce(final_icd10_code, icd10_code)}) входит в набор кодов МКБ-10 этой
 * записи: всего / до 14 лет / до 18 лет, каждый за выбранный период рядом с тем
 * же периодом год назад и разницей (как Form 6/8/9/11). Последней строкой —
 * «Jami», сумма только тех строк, у которых в справочнике включён флаг
 * «Jami qo'shilsinmi?» ({@code includeInTotal}).
 * <p>
 * Раскрытие строки нозологической формы — drill-down по географии
 * (республика→регион→район→организация), по одному уровню за вызов, тем же
 * движком {@code ReportHierarchyService}, что и остальные отчёты; разбивки узла
 * нет. Администратор наполняет справочник и проставляет тег {@code FORM_12} —
 * см. {@code ManualReportController}. Excel-экспорт таблицы строит фронтенд.
 */
@Tag(
        name = "Report — Form 12",
        description = "API отчёта «Form 12: инфекционные и паразитарные заболевания по нозологическим "
                + "формам». Строки — записи справочника ручных отчётов с типом FORM_12; числа — "
                + "подтверждённые (status = APPROVED) случаи форм №058 + №058-1, чей подтверждённый диагноз "
                + "входит в набор кодов МКБ-10 записи (всего / до 14 лет / до 18 лет), за выбранный период в "
                + "сравнении с тем же периодом год назад, в рамках доступа текущей организации. Раскрытие "
                + "строки — drill-down по административной иерархии (республика→регион→район→организация)."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form12Report.ROOT)
@RequiredArgsConstructor
public class Form12ReportController {

    private final Form12ReportQueryService form12ReportQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Нозологические формы + итого",
            description = "Возвращает по одной строке на каждую запись справочника ручных отчётов с типом "
                    + "FORM_12 — со счётами всего / до 14 лет / до 18 лет за выбранный период и за тот же "
                    + "период год назад — плюс последней строкой суммарный итог (\"Jami\") по строкам с "
                    + "включённым флагом includeInTotal. Период по умолчанию — вся история."
    )
    @GetMapping(ApiPaths.Form12Report.ROOT_NODE)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form12ReportNodeResponse>> root(
            @Parameter(description = "Начало периода (включительно). По умолчанию — вся история.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form12ReportQueryService.getRoot(from, to)
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
    @GetMapping(ApiPaths.Form12Report.CHILDREN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form12ReportNodeResponse>> children(
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
                form12ReportQueryService.getChildren(manualReportId, regionCode, districtCode, from, to)
        );
    }
}
