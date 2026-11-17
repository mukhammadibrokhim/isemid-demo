package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна ячейка-категория в блоке формы №129: извещения формы №129, чей "
        + "patient.category_code равен коду одной записи справочника ref_catalog(type = CATEGORY), в данном "
        + "узле географии, за выбранный период. В отличие от форм №058/№058-1 здесь нет разбивки "
        + "подтверждённые/неподтверждённые — только общее число.")
public record StatisticsForm129CategoryCellResponse(
        @Schema(description = "Код записи справочника (ref_catalog.code, type = CATEGORY).")
        String code,

        @Schema(description = "Локализованное наименование категории.")
        String name,

        @Schema(description = "Число извещений формы №129 с этой категорией.")
        long total
) {
}
