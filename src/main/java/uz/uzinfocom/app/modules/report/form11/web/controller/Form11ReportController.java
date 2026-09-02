package uz.uzinfocom.app.modules.report.form11.web.controller;

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
import uz.uzinfocom.app.modules.report.form11.application.query.Form11ReportQueryService;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11ReportNodeResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * "Form 11" — «Yuqumli va parazitar kasalliklar bilan kasallanish
 * ko'rsatkichlari», показатели заболеваемости: только подтверждённые ({@code
 * status = 'APPROVED'}) извещения, формы №058 и №058-1 объединены,
 * за произвольный период {@code from}/{@code to}. Каждый узел иерархии
 * показывает абсолютный и интенсивный (на {@code koef} населения {@code
 * population}) показатели рядом с теми же за тот же период год назад
 * ("O'tgan yil"/"Joriy yil"/"O'sish-Pasayish %"), плюс срезы текущего периода
 * по городскому / сельскому населению и детям до 18 лет. Дерево строится
 * постранично, по одному уровню иерархии за вызов, тем же движком {@code
 * ReportHierarchyService}, что и {@code Form9ReportController}; разбивки узла
 * нет — только география (регион→район→организация).
 * <p>
 * {@code koef}/{@code population} — плоские параметры запроса (по умолчанию
 * 100000/10000), один знаменатель на все узлы: перенесено из легаси {@code
 * Form11ReportController}. Excel-экспорт таблицы строит фронтенд из JSON.
 */
@Tag(
        name = "Report — Form 11",
        description = "API отчёта «Form 11: показатели заболеваемости инфекционными и паразитарными "
                + "болезнями» (формы №058 + №058-1, объединённые), только подтверждённые извещения "
                + "(status = APPROVED), абсолютный и интенсивный (на koef населения) показатели за "
                + "выбранный период в сравнении с тем же периодом год назад, по административной "
                + "иерархии (республика→регион→район→организация) в рамках доступа текущей организации, "
                + "плюс городской / сельский / детский (до 18 лет) срезы текущего периода."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form11Report.ROOT)
@RequiredArgsConstructor
public class Form11ReportController {

    private final Form11ReportQueryService form11ReportQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Первый уровень иерархии + итого",
            description = "Возвращает первый уровень иерархии в рамках области доступа текущей организации "
                    + "(регионы — для республиканского доступа, районы — для областного, организации — для "
                    + "районного), каждый с абсолютным и интенсивным показателями за выбранный период и за "
                    + "тот же период год назад и городским / сельским / детским срезами текущего периода, "
                    + "плюс последней строкой — суммарный итог (\"Jami\") по всей области доступа."
    )
    @GetMapping(ApiPaths.Form11Report.ROOT_NODE)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form11ReportNodeResponse>> root(
            @Parameter(description = "Начало периода (включительно). По умолчанию — вся история.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Фильтр по коду диагноза МКБ-10 (КХК-10), необязательный.")
            @RequestParam(required = false) String diagnosisCode,
            @Parameter(description = "Коэффициент интенсивного показателя (на сколько населения). По умолчанию 100000.")
            @RequestParam(defaultValue = "100000") long koef,
            @Parameter(description = "Численность населения — знаменатель интенсивного показателя. По умолчанию 10000.")
            @RequestParam(defaultValue = "10000") long population
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form11ReportQueryService.getRoot(from, to, diagnosisCode, koef, population)
        );
    }

    @Operation(
            summary = "Дочерние узлы отчёта",
            description = "Возвращает следующий уровень иерархии за выбранный период (и тот же период год "
                    + "назад). Без параметров — уровень, соответствующий области доступа вызывающего. С "
                    + "districtCode — организации указанного района. С регионом (regionCode) без districtCode "
                    + "— районы указанного региона. Запрос за пределами области доступа вызывающего отклоняется."
    )
    @GetMapping(ApiPaths.Form11Report.CHILDREN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<Form11ReportNodeResponse>> children(
            @Parameter(description = "Код региона (необязательный).")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Код района (необязательный).")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "Начало периода (включительно). По умолчанию — вся история.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Фильтр по коду диагноза МКБ-10 (КХК-10), необязательный.")
            @RequestParam(required = false) String diagnosisCode,
            @Parameter(description = "Коэффициент интенсивного показателя (на сколько населения). По умолчанию 100000.")
            @RequestParam(defaultValue = "100000") long koef,
            @Parameter(description = "Численность населения — знаменатель интенсивного показателя. По умолчанию 10000.")
            @RequestParam(defaultValue = "10000") long population
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form11ReportQueryService.getChildren(regionCode, districtCode, from, to, diagnosisCode, koef, population)
        );
    }
}
