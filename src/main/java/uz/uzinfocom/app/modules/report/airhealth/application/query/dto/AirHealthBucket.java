package uz.uzinfocom.app.modules.report.airhealth.application.query.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/** Time bucket of the air-and-health series. Weeks start on Monday, like PostgreSQL's {@code date_trunc('week')}. */
public enum AirHealthBucket {
    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private final String sqlTruncField;

    AirHealthBucket(String sqlTruncField) {
        this.sqlTruncField = sqlTruncField;
    }

    /** Fixed literal set, safe to interpolate into {@code date_trunc}. */
    public String sqlTruncField() {
        return sqlTruncField;
    }

    /** The first day of the bucket containing {@code date}. */
    public LocalDate start(LocalDate date) {
        return switch (this) {
            case DAY -> date;
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> date.withDayOfMonth(1);
        };
    }

    public LocalDate next(LocalDate bucketStart) {
        return switch (this) {
            case DAY -> bucketStart.plusDays(1);
            case WEEK -> bucketStart.plusWeeks(1);
            case MONTH -> bucketStart.plusMonths(1);
        };
    }
}
