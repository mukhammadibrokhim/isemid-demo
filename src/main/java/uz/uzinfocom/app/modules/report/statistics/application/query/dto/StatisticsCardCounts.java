package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.util.HashMap;
import java.util.Map;

/**
 * Card counts for one geography node / one form / one period — every card
 * whose owning form058/form058_1 case's {@code sender_organization_id} is in
 * scope, bucketed by {@link CardStatus} and by {@link CardType}. Mirrors
 * {@link StatisticsCounts} — a plain additive shape, safe to roll organization
 * nodes up into region/district/republic totals.
 */
public record StatisticsCardCounts(
        long total, Map<CardStatus, Long> byStatus, Map<CardType, Long> byType
) {

    public static final StatisticsCardCounts EMPTY = new StatisticsCardCounts(0, Map.of(), Map.of());

    public StatisticsCardCounts plus(StatisticsCardCounts other) {
        Map<CardStatus, Long> mergedStatus = new HashMap<>(this.byStatus);
        other.byStatus.forEach((status, count) -> mergedStatus.merge(status, count, Long::sum));

        Map<CardType, Long> mergedType = new HashMap<>(this.byType);
        other.byType.forEach((type, count) -> mergedType.merge(type, count, Long::sum));

        return new StatisticsCardCounts(this.total + other.total, mergedStatus, mergedType);
    }
}
