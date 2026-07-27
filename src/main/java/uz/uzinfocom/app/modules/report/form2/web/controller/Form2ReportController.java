package uz.uzinfocom.app.modules.report.form2.web.controller;

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
import uz.uzinfocom.app.modules.report.form2.application.query.Form2ReportQueryService;
import uz.uzinfocom.app.modules.report.form2.application.query.dto.Form2ReportNodeResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * "Form 2" — «Социальный состав заболеваемости», форма №058 и №058-1
 * объединены, за произвольный период {@code from}/{@code to}. Дерево
 * строится постранично, по одному уровню иерархии за вызов (см. {@code
 * ReportHierarchyService} — общий движок для всех отчётов {@code
 * modules.report}), в рамках доступа текущей организации.
 */
@Tag(
        name = "Report — Form 2",
        description = "API отчёта «Form 2: Социальный состав заболеваемости» (формы №058 + №058-1, "
                + "объединённые), за выбранный период: первичные экстренные извещения по социальным/"
                + "профессиональным категориям, по административной иерархии "
                + "(республика→регион→район→организация), в рамках доступа текущей организации."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form2Report.ROOT)
@RequiredArgsConstructor
public class Form2ReportController {

    private final Form2ReportQueryService form2ReportQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Первый уровень иерархии + итого",
            description = "Возвращает первый уровень иерархии в рамках области доступа текущей организации "
                    + "(регионы — для республиканского доступа, районы — для областного, организации — для "
                    + "районного), каждый со своими показателями за выбранный период, плюс последней строкой "
                    + "— суммарный итог (\"Jami\") по всей области доступа. Для организации без доступной "
                    + "иерархии (доступ только к собственной организации) возвращает единственную строку — "
                    + "показатели самой организации."
    )
    @GetMapping(ApiPaths.Form2Report.ROOT_NODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Form2ReportNodeResponse>> root(
            @Parameter(description = "Начало периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Фильтр по коду диагноза МКБ-10 (КХК-10), необязательный.")
            @RequestParam(required = false) String diagnosisCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form2ReportQueryService.getRoot(from, to, diagnosisCode)
        );
    }

    @Operation(
            summary = "Дочерние узлы отчёта",
            description = "Возвращает следующий уровень иерархии за выбранный период. Без параметров — "
                    + "уровень, соответствующий области доступа вызывающего (регионы/районы/организации). "
                    + "С districtCode — организации указанного района. С регионом (regionCode) без "
                    + "districtCode — районы указанного региона. Запрос за пределами области доступа "
                    + "вызывающего отклоняется."
    )
    @GetMapping(ApiPaths.Form2Report.CHILDREN)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Form2ReportNodeResponse>> children(
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
                form2ReportQueryService.getChildren(regionCode, districtCode, from, to, diagnosisCode)
        );
    }
}
