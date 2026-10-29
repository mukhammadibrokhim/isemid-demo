package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

/**
 * Time bucket the "Statistika" dynamics series ({@code /series}) is aggregated
 * into — the same DAY / WEEK / MONTH set the forecast report uses, kept as a
 * self-contained enum here so the two reports stay independent. Weeks are
 * ISO weeks (Monday-start); months are calendar months. The {@code date_trunc}
 * field is a fixed enum literal, safe to inline into native SQL.
 */
public enum StatisticsSeriesBucket {

    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private final String sqlTruncField;

    StatisticsSeriesBucket(String sqlTruncField) {
        this.sqlTruncField = sqlTruncField;
    }

    /**
     * {@code date_trunc('<field>', ...)} — a fixed enum literal, never caller input.
     */
    public String sqlTruncField() {
        return sqlTruncField;
    }

    /**
     * Start date of the bucket that follows {@code start}.
     */
    public LocalDate next(LocalDate start) {
        return switch (this) {
            case DAY -> start.plusDays(1);
            case WEEK -> start.plusWeeks(1);
            case MONTH -> start.plusMonths(1);
        };
    }

    /**
     * Normalizes any date to the first day of the bucket that contains it.
     */
    public LocalDate truncate(LocalDate date) {
        return switch (this) {
            case DAY -> date;
            case WEEK -> date.with(WeekFields.ISO.dayOfWeek(), 1);
            case MONTH -> date.withDayOfMonth(1);
        };
    }

    /**
     * Last day of the bucket that starts at {@code start} (inclusive).
     */
    public LocalDate endOf(LocalDate start) {
        return next(start).minusDays(1);
    }
}
