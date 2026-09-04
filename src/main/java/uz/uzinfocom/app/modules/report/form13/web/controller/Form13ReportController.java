package uz.uzinfocom.app.modules.report.form13.web.controller;

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
import uz.uzinfocom.app.modules.report.form13.application.query.Form13ReportQueryService;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13ReportNodeResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * "Form 13" — «перевёрнутая» Form 12: тот же материал (подтверждённые, status =
 * APPROVED, случаи форм №058 + №058-1; диагноз — coalesce(final_icd10_code,
 * icd10_code); набор болезней — записи справочника ручных отчётов), но строки —
 * география (республика→регион→район→организация, drill-down по одному уровню за
 * вызов тем же движком {@code ReportHierarchyService}), а столбцы — болезни:
 * каждая запись справочника ручных отчётов с тегом {@code FORM_13}, для каждой
 * пара «O'tgan yil / Joriy yil» × «Jami / до 14 лет / до 18 лет». Прироста нет.
 * Excel-экспорт таблицы строит фронтенд из JSON.
 */
@Tag(
        name = "Report — Form 13",
        description = "API отчёта «Form 13: инфекционные и паразитарные заболевания по территориям и "
                + "нозологическим формам». Строки — административная иерархия "
                + "(республика→регион→район→организация) в рамках доступа текущей организации; столбцы — "
                + "записи справочника ручных отчётов с тегом FORM_13. Числа — подтверждённые (status = "
                + "APPROVED) случаи форм №058 + №058-1, чей подтверждённый диагноз входит в набор кодов "
                + "МКБ-10 записи (всего / до 14 лет / до 18 лет), за выбранный период в сравнении с тем же "
                + "периодом год назад."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form13Report.ROOT)
@RequiredArgsConstructor
public class Form13ReportController {

    private final Form13ReportQueryService form13ReportQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Первый уровень иерархии + итого",
            description = "Возвращает первый уровень иерархии в рамках области доступа текущей организации "
                    + "(регионы — для республиканского доступа, районы — для областного, организации — для "
                    + "районного), каждый со столбцами-болезнями за выбранный период и за тот же период год "
                    + "назад, плюс последней строкой суммарный итог (\"Jami\") по всей области доступа. "
                    + "Период по умолчанию — вся история."
    )
    @GetMapping(ApiPaths.Form13Report.ROOT_NODE)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form13ReportNodeResponse>> root(
            @Parameter(description = "Начало периода (включительно). По умолчанию — вся история.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form13ReportQueryService.getRoot(from, to)
        );
    }

    @Operation(
            summary = "Дочерние узлы отчёта",
            description = "Возвращает следующий уровень иерархии за выбранный период (и тот же период год "
                    + "назад). Без regionCode/districtCode — уровень, соответствующий области доступа "
                    + "вызывающего. С regionCode — районы региона; с districtCode — организации района. "
                    + "Запрос за пределами области доступа вызывающего отклоняется."
    )
    @GetMapping(ApiPaths.Form13Report.CHILDREN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form13ReportNodeResponse>> children(
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
                form13ReportQueryService.getChildren(regionCode, districtCode, from, to)
        );
    }
}
