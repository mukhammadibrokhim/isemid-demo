package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;

import java.util.List;

@Schema(description = "Числа одного узла географии за один из двух сравниваемых периодов («Davr A» / «Davr "
        + "B»): случаи форм №058 + №058-1, подтверждённые (status = APPROVED) и неподтверждённые/первичные "
        + "(status not in (APPROVED, CANCELED)) отдельно, с разбивкой по возрасту, полу и социальной "
        + "категории пациента, плюс карты и акты, привязанные к этим случаям.")
public record StatisticsPeriodCountsResponse(
        @Schema(description = "«Tasdiqlangan» — всего подтверждённых случаев в узле за этот период, "
                + "независимо от категории пациента.")
        long confirmedTotal,

        @Schema(description = "«Tasdiqlanmagan» — всего неподтверждённых случаев в узле за этот период, "
                + "независимо от категории пациента.")
        long primaryTotal,

        @Schema(description = "Разбивка по возрасту (18 лет) за этот период.")
        StatisticsAgeBreakdownResponse ageBreakdown,

        @Schema(description = "Разбивка по полу за этот период.")
        StatisticsGenderBreakdownResponse genderBreakdown,

        @Schema(description = "По одной ячейке на каждую запись справочника ref_catalog(type = CATEGORY) за "
                + "этот период, в стабильном порядке (по nameUz), одинаковом для всех строк и обоих периодов.")
        List<StatisticsCategoryCellResponse> categories,

        @Schema(description = "Всего карт (любого из 5 типов эпидемиологических карт), привязанных к случаям "
                + "форм №058/№058-1 в этом узле за этот период (по дате создания самой карты).")
        long cardsTotal,

        @Schema(description = "Разбивка карт по статусу за этот период.")
        List<CardStatusCountResponse> cardsByStatus,

        @Schema(description = "Всего актов, привязанных к картам этого узла за этот период (по дате создания "
                + "самого акта).")
        long actsTotal,

        @Schema(description = "Разбивка актов по статусу за этот период.")
        List<ActStatusCountResponse> actsByStatus
) {
}
