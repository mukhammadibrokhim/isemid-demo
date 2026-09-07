package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Числа формы №129 (лабораторные извещения о серологии) в одном узле географии за один "
        + "из двух сравниваемых периодов. У формы №129 нет разбивки подтверждённые/неподтверждённые и нет "
        + "карт/актов — только общее число с разбивкой по возрасту (18 лет), полу, социальной категории "
        + "пациента и статусу извещения.")
public record StatisticsForm129BlockResponse(
        @Schema(description = "Всего извещений формы №129 в узле за период, независимо от статуса.")
        long total,

        @Schema(description = "«18 yoshdan kichiklar» — извещений формы №129 по пациентам младше 18 лет.")
        long under18,

        @Schema(description = "«Katta yoshlilar» (18+).")
        long adult,

        @Schema(description = "«Ayollar».")
        long female,

        @Schema(description = "«Erkaklar».")
        long male,

        @Schema(description = "По одной ячейке на каждую запись справочника ref_catalog(type = CATEGORY), в "
                + "том же порядке, что и в блоках форм №058/№058-1.")
        List<StatisticsForm129CategoryCellResponse> categories,

        @Schema(description = "Разбивка по статусу извещения (SENT / ACCEPTED / CANCELED), в порядке enum, с "
                + "нулями там, где извещений в этом статусе нет.")
        List<StatisticsForm129StatusCountResponse> byStatus
) {
}
