package uz.uzinfocom.app.modules.report.forecast.application.query.forecasting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The classical epidemiological "endemic channel" / epidemic-threshold line
 * (WHO / CDC): for each seasonal phase (month-of-year, ISO week-of-year, or
 * day-of-week — see {@code ForecastBucketUnit.seasonIndex}) the expected
 * level is the historical mean for that phase and the alert threshold is
 * {@code mean + 1.96 · SD}. A forecast bucket whose point estimate rises
 * above its phase threshold is flagged — "more than the usual seasonal
 * ceiling for this time of year."
 *
 * <p>A phase with fewer than two historical observations can't support a
 * spread estimate, so it falls back to a single global {@code mean + 1.96 ·
 * SD} computed over the whole history.
 */
public final class EndemicChannel {

    private static final double Z_95 = 1.96;

    private final Map<Integer, Double> thresholdBySeasonIndex;
    private final double globalThreshold;

    private EndemicChannel(Map<Integer, Double> thresholdBySeasonIndex, double globalThreshold) {
        this.thresholdBySeasonIndex = thresholdBySeasonIndex;
        this.globalThreshold = globalThreshold;
    }

    /**
     * @param countsBySeasonIndex historical bucket counts grouped by their seasonal phase index
     */
    public static EndemicChannel from(Map<Integer, List<Double>> countsBySeasonIndex) {
        Map<Integer, Double> perPhase = new HashMap<>();
        double allSum = 0.0;
        double allCount = 0.0;

        for (Map.Entry<Integer, List<Double>> entry : countsBySeasonIndex.entrySet()) {
            List<Double> values = entry.getValue();
            for (double v : values) {
                allSum += v;
                allCount++;
            }
            if (values.size() >= 2) {
                perPhase.put(entry.getKey(), meanPlusBand(values));
            }
        }

        double globalMean = allCount > 0 ? allSum / allCount : 0.0;
        double globalThreshold = allCount >= 2
                ? meanPlusBand(flatten(countsBySeasonIndex.values()))
                : globalMean + Z_95 * Math.sqrt(Math.max(0.0, globalMean));

        return new EndemicChannel(perPhase, globalThreshold);
    }

    /** Alert threshold for a bucket in the given seasonal phase. */
    public double thresholdFor(int seasonIndex) {
        return thresholdBySeasonIndex.getOrDefault(seasonIndex, globalThreshold);
    }

    private static double meanPlusBand(List<Double> values) {
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double ss = 0.0;
        for (double v : values) {
            ss += (v - mean) * (v - mean);
        }
        double sd = values.size() >= 2 ? Math.sqrt(ss / (values.size() - 1)) : 0.0;
        return mean + Z_95 * sd;
    }

    private static List<Double> flatten(Iterable<List<Double>> lists) {
        java.util.List<Double> out = new java.util.ArrayList<>();
        for (List<Double> l : lists) {
            out.addAll(l);
        }
        return out;
    }
}
