package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;

import java.util.HashMap;
import java.util.Map;

/**
 * Act counts for one geography node / one form / one period — every act whose
 * card's owning form058/form058_1 case's {@code sender_organization_id} is in
 * scope, bucketed by {@link ActStatus} and by {@link ActType}. Mirrors
 * {@link StatisticsCounts} — a plain additive shape, safe to roll organization
 * nodes up into region/district/republic totals.
 */
public record StatisticsActCounts(
        long total, Map<ActStatus, Long> byStatus, Map<ActType, Long> byType
) {

    public static final StatisticsActCounts EMPTY = new StatisticsActCounts(0, Map.of(), Map.of());

    public StatisticsActCounts plus(StatisticsActCounts other) {
        Map<ActStatus, Long> mergedStatus = new HashMap<>(this.byStatus);
        other.byStatus.forEach((status, count) -> mergedStatus.merge(status, count, Long::sum));

        Map<ActType, Long> mergedType = new HashMap<>(this.byType);
        other.byType.forEach((type, count) -> mergedType.merge(type, count, Long::sum));

        return new StatisticsActCounts(this.total + other.total, mergedStatus, mergedType);
    }
}
