package uz.uzinfocom.app.modules.report.shared;

import java.time.Instant;

/**
 * Half-open {@code [fromInclusive, toExclusive)} instant range used by every
 * report's counting query. Kept as a single shared shape so a report that
 * needs two periods (e.g. year-over-year comparison) can resolve both with
 * the same {@link ReportDateRangeResolver} and pass each around uniformly.
 */
public record ReportDateRange(Instant fromInclusive, Instant toExclusive) {
}
