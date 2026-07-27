package uz.uzinfocom.app.modules.report.form1.web.controller;

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
import uz.uzinfocom.app.modules.report.form1.application.query.Form1ReportQueryService;
import uz.uzinfocom.app.modules.report.form1.application.query.dto.Form1ReportNodeResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * "Form 1" — «Мониторинг инфекционных и паразитарных заболеваний», форма
 * №058 и №058-1 объединены, за произвольный период {@code from}/{@code to}
 * (не привязан к одному дню). Дерево строится постранично, по одному уровню
 * иерархии за вызов (см. {@code ReportHierarchyService} — общий движок для
 * всех отчётов {@code modules.report}), в рамках доступа текущей
 * организации (SANEPID-иерархия: вся республика / регион / район /
 * собственная организация).
 * <p>
 * Живёт под {@code modules.report.form1} — каждый следующий отчёт получает
 * свой собственный пакет под {@code modules.report}, названный {@code
 * formN} (например {@code report.form2}, {@code report.form3}), чтобы
 * отчёты не путались друг с другом.
 */
@Tag(
        name = "Report — Form 1",
        description = "API отчёта «Form 1: Мониторинг инфекционных и паразитарных заболеваний» (формы "
                + "№058 + №058-1, объединённые), за выбранный период: подтверждённые диагнозы и первичные "
                + "экстренные извещения, по административной иерархии "
                + "(республика→регион→район→организация), в рамках доступа текущей организации."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form1Report.ROOT)
@RequiredArgsConstructor
public class Form1ReportController {

    private final Form1ReportQueryService form1ReportQueryService;
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
    @GetMapping(ApiPaths.Form1Report.ROOT_NODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Form1ReportNodeResponse>> root(
            @Parameter(description = "Начало периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Фильтр по коду диагноза МКБ-10 (КХК-10), необязательный.")
            @RequestParam(required = false) String diagnosisCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form1ReportQueryService.getRoot(from, to, diagnosisCode)
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
    @GetMapping(ApiPaths.Form1Report.CHILDREN)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Form1ReportNodeResponse>> children(
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
                form1ReportQueryService.getChildren(regionCode, districtCode, from, to, diagnosisCode)
        );
    }
}
