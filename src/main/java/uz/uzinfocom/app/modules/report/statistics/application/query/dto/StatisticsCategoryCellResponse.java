package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна ячейка-категория в строке отчёта: подтверждённые/неподтверждённые случаи форм "
        + "№058 + №058-1, чей patient.category_code равен коду одной записи справочника ref_catalog(type = "
        + "CATEGORY), в данном узле географии, за выбранный период.")
public record StatisticsCategoryCellResponse(
        @Schema(description = "Код записи справочника (ref_catalog.code, type = CATEGORY) — «MTM turi».")
        String code,

        @Schema(description = "Локализованное наименование категории.")
        String name,

        @Schema(description = "Подтверждённые (status = APPROVED) случаи с этой категорией.")
        long confirmedTotal,

        @Schema(description = "Неподтверждённые (status not in (APPROVED, CANCELED)) случаи с этой категорией.")
        long primaryTotal
) {
}
