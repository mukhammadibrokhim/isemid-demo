package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.ActTypeCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTypeCountResponse;

import java.util.List;

@Schema(description = "Числа одной формы (№058 или №058-1) в одном узле географии за один из двух "
        + "сравниваемых периодов: подтверждённые (status = APPROVED) и неподтверждённые/первичные (status "
        + "not in (APPROVED, CANCELED)) случаи отдельно, с разбивкой по возрасту, полу и социальной "
        + "категории пациента, плюс карты и акты, привязанные именно к случаям этой формы.")
public record StatisticsFormBlockResponse(
        @Schema(description = "«Tasdiqlangan» — всего подтверждённых случаев этой формы в узле за период.")
        long confirmedTotal,

        @Schema(description = "«Tasdiqlanmagan» — всего неподтверждённых случаев этой формы в узле за период.")
        long primaryTotal,

        @Schema(description = "Разбивка по возрасту (18 лет).")
        StatisticsAgeBreakdownResponse ageBreakdown,

        @Schema(description = "Разбивка по полу.")
        StatisticsGenderBreakdownResponse genderBreakdown,

        @Schema(description = "По одной ячейке на каждую запись справочника ref_catalog(type = CATEGORY), в "
                + "стабильном порядке (по nameUz).")
        List<StatisticsCategoryCellResponse> categories,

        @Schema(description = "Всего карт, привязанных к случаям этой формы в узле за период (по дате создания "
                + "самой карты).")
        long cardsTotal,

        @Schema(description = "Разбивка карт по статусу.")
        List<CardStatusCountResponse> cardsByStatus,

        @Schema(description = "Разбивка карт по типу (CARD161/174/175/205/CARD_TUBE).")
        List<CardTypeCountResponse> cardsByType,

        @Schema(description = "Всего актов, привязанных к картам этой формы в узле за период (по дате создания "
                + "самого акта).")
        long actsTotal,

        @Schema(description = "Разбивка актов по статусу.")
        List<ActStatusCountResponse> actsByStatus,

        @Schema(description = "Разбивка актов по типу (ACT153/154/156/223/224).")
        List<ActTypeCountResponse> actsByType
) {
}
