package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * The count shape {@code C} the statistics report plugs into {@code
 * ReportHierarchyService} / {@code ReportCountSource}. One geography node
 * carries: an {@link #overall} total — every form058/form058_1 case in
 * scope, regardless of whether its patient has a recognized social category
 * — a per-category breakdown ({@link #byCategoryCode}, keyed by {@code
 * ref_catalog(type = 'CATEGORY')} code), and this same node's {@link #cards}
 * and {@link #acts} counts. Merging two nodes (organization → district →
 * region → republic) sums all four independently.
 */
public record StatisticsNodeCounts(
        StatisticsCounts overall,
        Map<String, StatisticsCounts> byCategoryCode,
        StatisticsCardCounts cards,
        StatisticsActCounts acts
) {

    public static final StatisticsNodeCounts EMPTY =
            new StatisticsNodeCounts(StatisticsCounts.EMPTY, Map.of(), StatisticsCardCounts.EMPTY, StatisticsActCounts.EMPTY);

    public StatisticsCounts category(String code) {
        return byCategoryCode.getOrDefault(code, StatisticsCounts.EMPTY);
    }

    public StatisticsNodeCounts plus(StatisticsNodeCounts other) {
        Map<String, StatisticsCounts> merged = new HashMap<>(this.byCategoryCode);
        other.byCategoryCode.forEach((code, counts) -> merged.merge(code, counts, StatisticsCounts::plus));
        return new StatisticsNodeCounts(
                this.overall.plus(other.overall), merged, this.cards.plus(other.cards), this.acts.plus(other.acts)
        );
    }
}
