package uz.uzinfocom.app.modules.report.forecast.application.query.dto;

/**
 * The count aggregate {@code C} the forecast report plugs into {@code
 * ReportHierarchyService} / {@code ReportCountSource} for its geography
 * breakdown: a whole bucketed time series for one geography node, as counts
 * aligned position-for-position to a fixed bucket axis (the training
 * window's bucket-start list, captured once per request in {@code
 * ForecastGeographyCountSource}).
 *
 * <p>{@code merge} is an element-wise add — that is exactly how the shared
 * hierarchy engine folds per-organization series into a per-region /
 * per-district / "Jami" series.
 */
public record ForecastSeries(long[] counts) {

    public static ForecastSeries zero(int length) {
        return new ForecastSeries(new long[length]);
    }

    public ForecastSeries plus(ForecastSeries other) {
        long[] a = this.counts;
        long[] b = other.counts;
        int n = Math.max(a.length, b.length);
        long[] out = new long[n];
        for (int i = 0; i < n; i++) {
            out[i] = (i < a.length ? a[i] : 0L) + (i < b.length ? b[i] : 0L);
        }
        return new ForecastSeries(out);
    }

    public long total() {
        long sum = 0;
        for (long c : counts) {
            sum += c;
        }
        return sum;
    }

    /** Counts as {@code double[]} — the input shape {@code TimeSeriesForecaster} works in. */
    public double[] asDoubles() {
        double[] out = new double[counts.length];
        for (int i = 0; i < counts.length; i++) {
            out[i] = counts[i];
        }
        return out;
    }
}
