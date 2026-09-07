package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

/**
 * One aggregate row per ({@link StatisticsFormType form}, {@link CardStatus},
 * {@link CardType}) triple across a whole organization scope — the raw
 * material for the report's root node. A card has exactly one status and one
 * type, so summing this over types yields the by-status breakdown, over
 * statuses the by-type breakdown, and over both the block total.
 */
public record StatisticsCardStatusCountProjection(
        StatisticsFormType formType, CardStatus status, CardType type, long count
) {
}
