package uz.uzinfocom.app.modules.report.forecast.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

/**
 * Time bucket the forecast series is aggregated into. Each unit carries its
 * own {@code date_trunc} argument (PostgreSQL), its own seasonal period (for
 * the seasonal forecast model and the endemic channel), a sensible default
 * training look-back, and a hard cap on how far ahead a single request may
 * forecast.
 */
public enum ForecastBucketUnit {

    /** Daily counts. Seasonality = weekday effect (7). */
    DAY("day", 7, 180, 90),

    /** ISO-week counts (weeks start Monday). Seasonality = week-of-year (52). */
    WEEK("week", 52, 104, 52),

    /** Calendar-month counts. Seasonality = month-of-year (12). */
    MONTH("month", 12, 36, 24);

    private final String sqlTruncField;
    private final int seasonLength;
    private final int defaultLookbackBuckets;
    private final int maxHorizon;

    ForecastBucketUnit(String sqlTruncField, int seasonLength, int defaultLookbackBuckets, int maxHorizon) {
        this.sqlTruncField = sqlTruncField;
        this.seasonLength = seasonLength;
        this.defaultLookbackBuckets = defaultLookbackBuckets;
        this.maxHorizon = maxHorizon;
    }

    /** {@code date_trunc('<field>', ...)} — a fixed enum literal, safe to inline into native SQL. */
    public String sqlTruncField() {
        return sqlTruncField;
    }

    public int seasonLength() {
        return seasonLength;
    }

    public int defaultLookbackBuckets() {
        return defaultLookbackBuckets;
    }

    public int maxHorizon() {
        return maxHorizon;
    }

    /** Start date of the bucket that follows {@code start}. */
    public LocalDate next(LocalDate start) {
        return switch (this) {
            case DAY -> start.plusDays(1);
            case WEEK -> start.plusWeeks(1);
            case MONTH -> start.plusMonths(1);
        };
    }

    /** {@code start} shifted back by {@code buckets} whole buckets. */
    public LocalDate minusBuckets(LocalDate start, int buckets) {
        return switch (this) {
            case DAY -> start.minusDays(buckets);
            case WEEK -> start.minusWeeks(buckets);
            case MONTH -> start.minusMonths(buckets);
        };
    }

    /** Start date of the bucket that precedes {@code start}. */
    public LocalDate previous(LocalDate start) {
        return switch (this) {
            case DAY -> start.minusDays(1);
            case WEEK -> start.minusWeeks(1);
            case MONTH -> start.minusMonths(1);
        };
    }

    /** Normalizes any date to the first day of the bucket that contains it. */
    public LocalDate truncate(LocalDate date) {
        return switch (this) {
            case DAY -> date;
            case WEEK -> date.with(WeekFields.ISO.dayOfWeek(), 1);
            case MONTH -> date.withDayOfMonth(1);
        };
    }

    /**
     * The seasonal-phase index of a bucket — the key both the seasonal
     * forecast model and the endemic channel use to line "the same time of
     * year" up across years: month-of-year (1-12), ISO week-of-year (1-53),
     * or day-of-week (1-7).
     */
    public int seasonIndex(LocalDate bucketStart) {
        return switch (this) {
            case DAY -> bucketStart.getDayOfWeek().getValue();
            case WEEK -> bucketStart.get(WeekFields.ISO.weekOfWeekBasedYear());
            case MONTH -> bucketStart.getMonthValue();
        };
    }
}
