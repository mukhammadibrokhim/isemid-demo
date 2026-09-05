package uz.uzinfocom.app.modules.report.statistics.web.controller;

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
import uz.uzinfocom.app.modules.report.statistics.application.query.StatisticsReportQueryService;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsNodeResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

@Tag(
        name = "Report — Statistika",
        description = "Эпидемиологическая статистика: строки — административная иерархия (республика→регион→"
                + "район→организация) в рамках доступа текущей организации; числа — подтверждённые (status = "
                + "APPROVED) и неподтверждённые/первичные (status not in (APPROVED, CANCELED)) случаи форм "
                + "№058 + №058-1, с разбивкой по возрасту (18 лет), полу и социальной категории пациента "
                + "(справочник ref_catalog, type = CATEGORY). Поддерживает сравнение двух произвольных, "
                + "независимо выбираемых периодов — «Davr A» (fromA/toA, обязательный) и опциональный «Davr "
                + "B» (fromB/toB) — а не только «текущий год / год назад»."
)
@Validated
@RestController
@RequestMapping(ApiPaths.StatisticsReport.ROOT)
@RequiredArgsConstructor
public class StatisticsReportController {

    private final StatisticsReportQueryService statisticsReportQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Первый уровень иерархии + итого",
            description = "Возвращает первый уровень иерархии в рамках области доступа текущей организации "
                    + "(регионы — для республиканского доступа, районы — для областного, организации — для "
                    + "районного), плюс последней строкой суммарный итог (\"Jami\") по всей области доступа. "
                    + "«Davr A» (fromA/toA) — по умолчанию вся история. «Davr B» (fromB/toB) — необязателен; "
                    + "если ни один из них не передан, поле periodB в ответе будет null (сравнения нет)."
    )
    @GetMapping(ApiPaths.StatisticsReport.ROOT_NODE)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<StatisticsNodeResponse>> root(
            @Parameter(description = "Davr A: начало периода (включительно). По умолчанию — вся история.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromA,
            @Parameter(description = "Davr A: конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toA,
            @Parameter(description = "Davr B (для сравнения): начало периода. Не передан вместе с toB — "
                    + "periodB отсутствует (null).")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromB,
            @Parameter(description = "Davr B (для сравнения): конец периода.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toB
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                statisticsReportQueryService.getRoot(fromA, toA, fromB, toB)
        );
    }

    @Operation(
            summary = "Дочерние узлы отчёта",
            description = "Возвращает следующий уровень иерархии за выбранный период (периоды). Без "
                    + "regionCode/districtCode — уровень, соответствующий области доступа вызывающего. С "
                    + "regionCode — районы региона; с districtCode — организации района. Запрос за пределами "
                    + "области доступа вызывающего отклоняется."
    )
    @GetMapping(ApiPaths.StatisticsReport.CHILDREN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<StatisticsNodeResponse>> children(
            @Parameter(description = "Код региона (необязательный).")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Код района (необязательный).")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "Davr A: начало периода (включительно). По умолчанию — вся история.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromA,
            @Parameter(description = "Davr A: конец периода (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toA,
            @Parameter(description = "Davr B (для сравнения): начало периода. Не передан вместе с toB — "
                    + "periodB отсутствует (null).")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromB,
            @Parameter(description = "Davr B (для сравнения): конец периода.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toB
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                statisticsReportQueryService.getChildren(regionCode, districtCode, fromA, toA, fromB, toB)
        );
    }
}
