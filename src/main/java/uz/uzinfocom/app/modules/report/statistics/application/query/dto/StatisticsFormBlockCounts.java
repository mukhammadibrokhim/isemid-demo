package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * All counts for <b>one</b> notification form (058 or 058-1) at one geography
 * node / one period: an {@link #overall} confirmed/primary total (with its
 * age and gender cuts), a per-social-category breakdown ({@link
 * #byCategoryCode}, keyed by {@code ref_catalog(type = 'CATEGORY')} code), and
 * this same node's {@link #cards} and {@link #acts} — cards/acts belong to
 * exactly one of 058/058-1, so they live inside the form block, not beside it.
 * Merging two nodes (organization → district → region → republic) sums all
 * four independently.
 */
public record StatisticsFormBlockCounts(
        StatisticsCounts overall,
        Map<String, StatisticsCounts> byCategoryCode,
        StatisticsCardCounts cards,
        StatisticsActCounts acts
) {

    public static final StatisticsFormBlockCounts EMPTY = new StatisticsFormBlockCounts(
            StatisticsCounts.EMPTY, Map.of(), StatisticsCardCounts.EMPTY, StatisticsActCounts.EMPTY
    );

    public StatisticsCounts category(String code) {
        return byCategoryCode.getOrDefault(code, StatisticsCounts.EMPTY);
    }

    public StatisticsFormBlockCounts plus(StatisticsFormBlockCounts other) {
        Map<String, StatisticsCounts> merged = new HashMap<>(this.byCategoryCode);
        other.byCategoryCode.forEach((code, counts) -> merged.merge(code, counts, StatisticsCounts::plus));
        return new StatisticsFormBlockCounts(
                this.overall.plus(other.overall), merged, this.cards.plus(other.cards), this.acts.plus(other.acts)
        );
    }
}
