package uz.uzinfocom.app.integration.outbound.monitoring.application;

import java.time.temporal.ChronoUnit;

/**
 * Time bucket of the dynamics series, picked from the requested range like
 * the legacy report: up to 1 day apart -> hours, up to 30 -> days, up to 180
 * -> weeks (7-day steps anchored at {@code fromDate}), else months.
 * <p>
 * {@link #step}, {@link #labelFormat} and {@link #bucketSql} are fixed
 * literals interpolated into the SQL - never caller input. {@code bucketSql}
 * takes the case's local ({@code Asia/Tashkent}) timestamp expression as
 * {@code %1$s} and, for weeks, the anchor date parameter as {@code %2$s}.
 */
public enum MonitoringGranularity {
    HOUR("1 hour", "YYYY-MM-DD HH24", "to_char(date_trunc('hour', %1$s), 'YYYY-MM-DD HH24')"),
    DAY("1 day", "YYYY-MM-DD", "to_char(date_trunc('day', %1$s), 'YYYY-MM-DD')"),
    WEEK("7 days", "YYYY-MM-DD",
            "to_char((%2$s)::date + ((((%1$s)::date - (%2$s)::date) / 7) * 7), 'YYYY-MM-DD')"),
    MONTH("1 month", "YYYY-MM", "to_char(date_trunc('month', %1$s), 'YYYY-MM')");

    private final String step;
    private final String labelFormat;
    private final String bucketSql;

    MonitoringGranularity(String step, String labelFormat, String bucketSql) {
        this.step = step;
        this.labelFormat = labelFormat;
        this.bucketSql = bucketSql;
    }

    public String step() {
        return step;
    }

    public String labelFormat() {
        return labelFormat;
    }

    public boolean usesWeekAnchor() {
        return this == WEEK;
    }

    public String bucketSql(String localTimestampExpression, String weekAnchorExpression) {
        return bucketSql.formatted(localTimestampExpression, weekAnchorExpression);
    }

    public static MonitoringGranularity forRange(long daysBetween) {
        if (daysBetween <= 1) {
            return HOUR;
        }
        if (daysBetween <= 30) {
            return DAY;
        }
        if (daysBetween <= 180) {
            return WEEK;
        }
        return MONTH;
    }

    public static MonitoringGranularity forRange(java.time.LocalDate from, java.time.LocalDate to) {
        return forRange(ChronoUnit.DAYS.between(from, to));
    }
}
