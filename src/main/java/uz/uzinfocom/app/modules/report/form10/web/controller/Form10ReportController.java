package uz.uzinfocom.app.modules.report.form10.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.report.form10.application.query.Form10ReportQueryService;
import uz.uzinfocom.app.modules.report.form10.application.query.dto.Form10ReportNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportPeriod;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * "Form 10" — «Respublika bo'yicha ma'muriy hududlar kesimida yuqumli
 * kasalliklar bilan kasallanish to'g'risidagi ma'lumotlar»: только
 * подтверждённые ({@code status = 'APPROVED'}) извещения, формы №058 и №058-1
 * объединены. Параметры — {@code year} и {@code period} ({@link ReportPeriod}).
 * Каждый узел иерархии показывает два блока — «Joriy davr» (месячный интервал
 * выбранного периода) и «Yig'ma» (с января по конец периода) — с абсолютным и
 * интенсивным (на {@code koef} населения территории) показателями рядом с теми
 * же за прошлый год и приростом %, отдельно по всему населению и по детям до
 * 14 лет. Дерево строится постранично, по одному уровню за вызов, тем же
 * движком {@code ReportHierarchyService}, что и {@code Form11ReportController};
 * разбивки узла нет — только география. Excel-экспорт таблицы строит фронтенд
 * из JSON.
 */
@Tag(
        name = "Report — Form 10",
        description = "API отчёта «Form 10: данные о заболеваемости инфекционными болезнями в разрезе "
                + "административных территорий» (формы №058 + №058-1, объединённые), только подтверждённые "
                + "извещения (status = APPROVED). Год + период (месяц / квартал / полугодие / 9 месяцев / "
                + "год); два блока «Joriy davr» и «Yig'ma», абсолютный и интенсивный (на koef населения) "
                + "показатели в сравнении с прошлым годом, по административной иерархии "
                + "(республика→регион→район→организация) в рамках доступа текущей организации, с отдельным "
                + "срезом по детям до 14 лет."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form10Report.ROOT)
@RequiredArgsConstructor
public class Form10ReportController {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    private final Form10ReportQueryService form10ReportQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Первый уровень иерархии + итого",
            description = "Возвращает первый уровень иерархии в рамках области доступа текущей организации "
                    + "(регионы — для республиканского доступа, районы — для областного, организации — для "
                    + "районного), каждый с блоками «Joriy davr» и «Yig'ma» за выбранный год и прошлый год, "
                    + "плюс последней строкой — суммарный итог (\"Jami\") по всей области доступа."
    )
    @GetMapping(ApiPaths.Form10Report.ROOT_NODE)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form10ReportNodeResponse>> root(
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
                form10ReportQueryService.getRoot(resolveYear(year), resolvePeriod(period), diagnosisCode, koef)
        );
    }

    @Operation(
            summary = "Дочерние узлы отчёта",
            description = "Возвращает следующий уровень иерархии за выбранный год (и прошлый год). Без "
                    + "параметров — уровень, соответствующий области доступа вызывающего. С districtCode — "
                    + "организации указанного района. С регионом (regionCode) без districtCode — районы "
                    + "указанного региона. Запрос за пределами области доступа вызывающего отклоняется."
    )
    @GetMapping(ApiPaths.Form10Report.CHILDREN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form10ReportNodeResponse>> children(
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
                form10ReportQueryService.getChildren(
                        regionCode, districtCode, resolveYear(year), resolvePeriod(period), diagnosisCode, koef
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
