package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Card counts for one geography node / one period — every card (of any of
 * the 5 epidemiological card types) whose owning form058/form058_1 case's
 * {@code sender_organization_id} is in scope, bucketed by {@link
 * CardStatus}. Mirrors {@link StatisticsCounts} — a plain additive shape,
 * safe to roll organization nodes up into region/district/republic totals.
 */
public record StatisticsCardCounts(long total, Map<CardStatus, Long> byStatus) {

    public static final StatisticsCardCounts EMPTY = new StatisticsCardCounts(0, Map.of());

    public StatisticsCardCounts plus(StatisticsCardCounts other) {
        Map<CardStatus, Long> merged = new HashMap<>(this.byStatus);
        other.byStatus.forEach((status, count) -> merged.merge(status, count, Long::sum));
        return new StatisticsCardCounts(this.total + other.total, merged);
    }
}
