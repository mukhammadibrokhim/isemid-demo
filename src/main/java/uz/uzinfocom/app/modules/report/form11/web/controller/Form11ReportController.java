package uz.uzinfocom.app.modules.report.form11.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.report.form11.application.export.Form11ExcelExportSource;
import uz.uzinfocom.app.modules.report.form11.application.export.Form11ExportFilter;
import uz.uzinfocom.app.modules.report.form11.application.query.Form11ReportQueryService;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11ReportNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportPeriod;
import uz.uzinfocom.app.platform.export.application.ExportJobService;
import uz.uzinfocom.app.platform.export.application.dto.ExportJobResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * "Form 11" — «Yuqumli va parazitar kasalliklar bilan kasallanish
 * ko'rsatkichlari»: только подтверждённые ({@code status = 'APPROVED'})
 * извещения, формы №058 и №058-1 объединены. Параметры — {@code year} и
 * {@code period} ({@link ReportPeriod}). Каждый узел иерархии показывает два
 * блока — «Joriy davr» (месячный интервал выбранного периода) и «Yig'ma» (с
 * января по конец периода) — с абсолютным и интенсивным (на {@code koef}
 * населения территории) показателями рядом с теми же за прошлый год и
 * приростом %, отдельно по всему населению, городскому, сельскому населению
 * и детям до 18 лет. Дерево строится постранично, по одному уровню за вызов,
 * тем же движком {@code ReportHierarchyService}, что и {@code
 * Form10ReportController} (структурно идентичен ему); разбивки узла нет —
 * только география. Excel-экспорт таблицы строит фронтенд из JSON.
 */
@Tag(
        name = "Report — Form 11",
        description = "API отчёта «Form 11: показатели заболеваемости инфекционными и паразитарными "
                + "болезнями» (формы №058 + №058-1, объединённые), только подтверждённые извещения "
                + "(status = APPROVED). Год + период (месяц / квартал / полугодие / 9 месяцев / год); два "
                + "блока «Joriy davr» и «Yig'ma», абсолютный и интенсивный (на koef населения территории) "
                + "показатели в сравнении с прошлым годом, по административной иерархии "
                + "(республика→регион→район→организация) в рамках доступа текущей организации, с отдельными "
                + "срезами по городскому / сельскому населению и детям до 18 лет."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form11Report.ROOT)
@RequiredArgsConstructor
public class Form11ReportController {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    private final Form11ReportQueryService form11ReportQueryService;
    private final MessageResolver messageResolver;
    private final ExportJobService exportJobService;
    private final Form11ExcelExportSource form11ExcelExportSource;

    @Operation(
            summary = "Первый уровень иерархии + итого",
            description = "Возвращает первый уровень иерархии в рамках области доступа текущей организации "
                    + "(регионы — для республиканского доступа, районы — для областного, организации — для "
                    + "районного), каждый с блоками «Joriy davr» и «Yig'ma» за выбранный год и прошлый год, "
                    + "плюс последней строкой — суммарный итог (\"Jami\") по всей области доступа."
    )
    @GetMapping(ApiPaths.Form11Report.ROOT_NODE)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form11ReportNodeResponse>> root(
            @Parameter(description = "Отчётный год. По умолчанию — текущий.")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Период: месяц (JANUARY…DECEMBER), квартал (Q1…Q4), полугодие "
                    + "(HALF_YEAR), 9 месяцев (NINE_MONTHS) или год (YEAR). По умолчанию — текущий месяц.")
            @RequestParam(required = false) ReportPeriod period,
            @Parameter(description = "Фильтр по коду диагноза МКБ-10 (КХК-10), необязательный.")
            @RequestParam(required = false) String diagnosisCode,
            @Parameter(description = "Коэффициент интенсивного показателя (на сколько населения). По умолчанию 100000.")
            @RequestParam(defaultValue = "100000") long koef
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form11ReportQueryService.getRoot(resolveYear(year), resolvePeriod(period), diagnosisCode, koef)
        );
    }

    @Operation(
            summary = "Дочерние узлы отчёта",
            description = "Возвращает следующий уровень иерархии за выбранный год (и прошлый год). Без "
                    + "параметров — уровень, соответствующий области доступа вызывающего. С districtCode — "
                    + "организации указанного района. С регионом (regionCode) без districtCode — районы "
                    + "указанного региона. Запрос за пределами области доступа вызывающего отклоняется."
    )
    @GetMapping(ApiPaths.Form11Report.CHILDREN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form11ReportNodeResponse>> children(
            @Parameter(description = "Код региона (необязательный).")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Код района (необязательный).")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "Отчётный год. По умолчанию — текущий.")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Период: месяц (JANUARY…DECEMBER), квартал (Q1…Q4), полугодие "
                    + "(HALF_YEAR), 9 месяцев (NINE_MONTHS) или год (YEAR). По умолчанию — текущий месяц.")
            @RequestParam(required = false) ReportPeriod period,
            @Parameter(description = "Фильтр по коду диагноза МКБ-10 (КХК-10), необязательный.")
            @RequestParam(required = false) String diagnosisCode,
            @Parameter(description = "Коэффициент интенсивного показателя (на сколько населения). По умолчанию 100000.")
            @RequestParam(defaultValue = "100000") long koef
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form11ReportQueryService.getChildren(
                        regionCode, districtCode, resolveYear(year), resolvePeriod(period), diagnosisCode, koef
                )
        );
    }

    @Operation(
            summary = "Экспорт Form 11 в Excel",
            description = "Ставит в очередь фоновую задачу экспорта в Excel по всей доступной иерархии "
                    + "(регион→район→организация) за выбранный год и период. Прогресс и скачивание готового "
                    + "файла — через /v1/exports."
    )
    @PostMapping(ApiPaths.Form11Report.EXPORT)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<ExportJobResponse> export(
            @Parameter(description = "Отчётный год. По умолчанию — текущий.")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Период: месяц (JANUARY…DECEMBER), квартал (Q1…Q4), полугодие "
                    + "(HALF_YEAR), 9 месяцев (NINE_MONTHS) или год (YEAR). По умолчанию — текущий месяц.")
            @RequestParam(required = false) ReportPeriod period,
            @Parameter(description = "Фильтр по коду диагноза МКБ-10 (КХК-10), необязательный.")
            @RequestParam(required = false) String diagnosisCode,
            @Parameter(description = "Коэффициент интенсивного показателя (на сколько населения). По умолчанию 100000.")
            @RequestParam(defaultValue = "100000") long koef
    ) {
        return ApiResponse.success(
                messageResolver.resolve("export.job.submitted"),
                exportJobService.submit(
                        form11ExcelExportSource,
                        new Form11ExportFilter(resolveYear(year), resolvePeriod(period), diagnosisCode, koef)
                )
        );
    }

    private int resolveYear(Integer year) {
        return year != null ? year : LocalDate.now(APPLICATION_ZONE).getYear();
    }

    private ReportPeriod resolvePeriod(ReportPeriod period) {
        return period != null ? period : ReportPeriod.ofMonth(LocalDate.now(APPLICATION_ZONE).getMonthValue());
    }
}
