package uz.uzinfocom.app.modules.report.form9.web.controller;

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
import uz.uzinfocom.app.modules.report.form9.application.query.Form9ReportQueryService;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9MonthlyBreakdownResponse;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9ReportNodeResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * "Form 9" — «Юкумли касалликлар бўйича қиёсий маълумот», сравнительный
 * анализ: первичные (ещё не решённые — не отменённые и не подтверждённые)
 * извещения, форма №058 и №058-1 объединены, за произвольный период {@code
 * from}/{@code to}. Каждый узел иерархии показывает две метрики —
 * «зарегистрировано по первичному извещению» и «госпитализировано» — рядом с
 * теми же метриками за тот же период год назад ("O'tgan yil"/"Joriy
 * yil"/"Taqqoslash (+/-)"). Дерево строится постранично, по одному уровню
 * иерархии за вызов, тем же движком {@code ReportHierarchyService}, что и
 * {@code Form6ReportController}/{@code Form8ReportController}; разбивка узла —
 * по календарным месяцам (12 строк + "Jami").
 * <p>
 * Живёт под {@code modules.report.form9}, по тому же соглашению об
 * именовании пакетов, что {@code report.form6}/{@code report.form8}.
 * Excel-экспорт таблицы строит фронтенд из JSON, как и в остальных отчётах.
 */
@Tag(
        name = "Report — Form 9",
        description = "API отчёта «Form 9: сравнительные данные по инфекционным заболеваниям» "
                + "(формы №058 + №058-1, объединённые), первичные извещения (status NOT IN (APPROVED, "
                + "CANCELED)), две метрики (зарегистрировано; госпитализировано) за выбранный период "
                + "в сравнении с тем же периодом год назад, по административной иерархии (республика→"
                + "регион→район→организация), в рамках доступа текущей организации, плюс разбивка "
                + "каждого узла по календарным месяцам."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form9Report.ROOT)
@RequiredArgsConstructor
public class Form9ReportController {

    private final Form9ReportQueryService form9ReportQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Первый уровень иерархии + итого",
            description = "Возвращает первый уровень иерархии в рамках области доступа текущей организации "
                    + "(регионы — для республиканского доступа, районы — для областного, организации — для "
                    + "районного), каждый с обеими метриками за выбранный период и за тот же период год "
                    + "назад, плюс последней строкой — суммарный итог (\"Jami\") по всей области доступа."
    )
    @GetMapping(ApiPaths.Form9Report.ROOT_NODE)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form9ReportNodeResponse>> root(
            @Parameter(description = "Начало периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Фильтр по коду диагноза МКБ-10 (КХК-10), необязательный.")
            @RequestParam(required = false) String diagnosisCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form9ReportQueryService.getRoot(from, to, diagnosisCode)
        );
    }

    @Operation(
            summary = "Дочерние узлы отчёта",
            description = "Возвращает следующий уровень иерархии за выбранный период (и тот же период год "
                    + "назад). Без параметров — уровень, соответствующий области доступа вызывающего. С "
                    + "districtCode — организации указанного района. С регионом (regionCode) без districtCode "
                    + "— районы указанного региона. Запрос за пределами области доступа вызывающего отклоняется."
    )
    @GetMapping(ApiPaths.Form9Report.CHILDREN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form9ReportNodeResponse>> children(
            @Parameter(description = "Код региона (необязательный).")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Код района (необязательный).")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "Начало периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Фильтр по коду диагноза МКБ-10 (КХК-10), необязательный.")
            @RequestParam(required = false) String diagnosisCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form9ReportQueryService.getChildren(regionCode, districtCode, from, to, diagnosisCode)
        );
    }

    @Operation(
            summary = "Месячная разбивка одного узла",
            description = "Возвращает разбивку по календарным месяцам для указанного узла (regionCode/"
                    + "districtCode) и всего его поддерева — либо, без параметров, для всей области доступа "
                    + "вызывающего. 13 строк: 12 месяцев (январь→декабрь), затем \"Jami\" (итого); каждая "
                    + "строка — обе метрики за выбранный период и за тот же период год назад."
    )
    @GetMapping(ApiPaths.Form9Report.MONTHLY_BREAKDOWN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<Form9MonthlyBreakdownResponse> monthlyBreakdown(
            @Parameter(description = "Код региона (необязательный).")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Код района (необязательный).")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "Начало периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Фильтр по коду диагноза МКБ-10 (КХК-10), необязательный.")
            @RequestParam(required = false) String diagnosisCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form9ReportQueryService.getMonthlyBreakdown(regionCode, districtCode, from, to, diagnosisCode)
        );
    }
}
