package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Числа одного узла географии за один из двух сравниваемых периодов («Davr A» / «Davr "
        + "B»): формы №058, №058-1 и №129 представлены ТРЕМЯ ОТДЕЛЬНЫМИ блоками (не суммируются в один "
        + "«общий» показатель). Блоки форм №058/№058-1 несут ещё и свои карты/акты (карта/акт относится "
        + "ровно к одной из этих форм); у формы №129 карт/актов нет.")
public record StatisticsPeriodCountsResponse(
        @Schema(description = "Блок формы №058.")
        StatisticsFormBlockResponse form058,

        @Schema(description = "Блок формы №058-1.")
        StatisticsFormBlockResponse form0581,

        @Schema(description = "Блок формы №129.")
        StatisticsForm129BlockResponse form129
) {
}
