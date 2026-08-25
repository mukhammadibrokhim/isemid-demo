package uz.uzinfocom.app.modules.report.form6.web.controller;

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
import uz.uzinfocom.app.modules.report.form6.application.query.Form6ReportQueryService;
import uz.uzinfocom.app.modules.report.form6.application.query.dto.Form6AgeBreakdownResponse;
import uz.uzinfocom.app.modules.report.form6.application.query.dto.Form6ReportNodeResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * "Form 6" — возрастная структура первичных (ещё не решённых — не
 * отменённых и не подтверждённых) извещений об инфекционных и паразитарных
 * заболеваниях, форма №058 и №058-1 объединены, за произвольный период
 * {@code from}/{@code to}. Дерево строится постранично, по одному уровню
 * иерархии за вызов, тем же движком {@code ReportHierarchyService}, что и
 * {@code Form1ReportController}. Каждый узел (и каждая строка возрастной
 * разбивки) показывает выбранный период рядом с тем же периодом год назад
 * ("O'tgan yil"/"Joriy yil"/"O'sish-Kamayish").
 * <p>
 * Живёт под {@code modules.report.form6}, по тому же соглашению об
 * именовании пакетов, что {@code report.form1}/{@code report.form4}.
 */
@Tag(
        name = "Report — Form 6",
        description = "API отчёта «Form 6: возрастная структура инфекционных и паразитарных заболеваний» "
                + "(формы №058 + №058-1, объединённые), первичные извещения, за выбранный период в сравнении "
                + "с тем же периодом год назад, по административной иерархии (республика→регион→район"
                + "→организация), в рамках доступа текущей организации, плюс возрастная разбивка по каждому узлу."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form6Report.ROOT)
@RequiredArgsConstructor
public class Form6ReportController {

    private final Form6ReportQueryService form6ReportQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Первый уровень иерархии + итого",
            description = "Возвращает первый уровень иерархии в рамках области доступа текущей организации "
                    + "(регионы — для республиканского доступа, районы — для областного, организации — для "
                    + "районного), каждый со счётом за выбранный период и за тот же период год назад, плюс "
                    + "последней строкой — суммарный итог (\"Jami\") по всей области доступа."
    )
    @GetMapping(ApiPaths.Form6Report.ROOT_NODE)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form6ReportNodeResponse>> root(
            @Parameter(description = "Начало периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Фильтр по коду диагноза МКБ-10 (КХК-10), необязательный.")
            @RequestParam(required = false) String diagnosisCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form6ReportQueryService.getRoot(from, to, diagnosisCode)
        );
    }

    @Operation(
            summary = "Дочерние узлы отчёта",
            description = "Возвращает следующий уровень иерархии за выбранный период (и тот же период год "
                    + "назад). Без параметров — уровень, соответствующий области доступа вызывающего. С "
                    + "districtCode — организации указанного района. С регионом (regionCode) без districtCode "
                    + "— районы указанного региона. Запрос за пределами области доступа вызывающего отклоняется."
    )
    @GetMapping(ApiPaths.Form6Report.CHILDREN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form6ReportNodeResponse>> children(
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
                form6ReportQueryService.getChildren(regionCode, districtCode, from, to, diagnosisCode)
        );
    }

    @Operation(
            summary = "Возрастная структура одного узла",
            description = "Возвращает разбивку по возрастным группам для указанного узла (regionCode/"
                    + "districtCode) и всего его поддерева — либо, без параметров, для всей области доступа "
                    + "вызывающего. Первой строкой — \"Jami\" (итого), затем возрастные группы от младших к "
                    + "старшим, каждая со счётом за выбранный период и за тот же период год назад."
    )
    @GetMapping(ApiPaths.Form6Report.AGE_BREAKDOWN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<Form6AgeBreakdownResponse> ageBreakdown(
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
                form6ReportQueryService.getAgeBreakdown(regionCode, districtCode, from, to, diagnosisCode)
        );
    }
}
