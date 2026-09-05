package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;

/** One aggregate row per {@link CardStatus} across a whole organization scope — the report's root node. */
public record StatisticsCardStatusCountProjection(CardStatus status, long count) {
}
