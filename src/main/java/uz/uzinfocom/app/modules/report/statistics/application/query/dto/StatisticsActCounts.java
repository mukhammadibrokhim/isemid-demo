package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Act counts for one geography node / one period — every act (of any of the
 * 5 act types) whose card's owning form058/form058_1 case's {@code
 * sender_organization_id} is in scope, bucketed by {@link ActStatus}.
 * Mirrors {@link StatisticsCounts} — a plain additive shape, safe to roll
 * organization nodes up into region/district/republic totals.
 */
public record StatisticsActCounts(long total, Map<ActStatus, Long> byStatus) {

    public static final StatisticsActCounts EMPTY = new StatisticsActCounts(0, Map.of());

    public StatisticsActCounts plus(StatisticsActCounts other) {
        Map<ActStatus, Long> merged = new HashMap<>(this.byStatus);
        other.byStatus.forEach((status, count) -> merged.merge(status, count, Long::sum));
        return new StatisticsActCounts(this.total + other.total, merged);
    }
}
