package uz.uzinfocom.app.modules.report.shared;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Resolves a caller-supplied {@code [from, to]} (inclusive, may be null on
 * either side) into a half-open {@link ReportDateRange}, in the
 * application's reporting time zone. Lifted verbatim out of {@code
 * Form1ReportQueryService} ("Form 1") so every report under {@code
 * modules.report} shares one definition of "no date given" instead of each
 * report picking its own default.
 * <p>
 * When <b>both</b> {@code from} and {@code to} are omitted, the range is
 * unbounded from the earliest possible record through today — not "today
 * only" — so a caller who hasn't picked a period yet (the common case when
 * first opening a report) sees the full history rather than an
 * almost-always-empty single day. When only one side is supplied, the other
 * defaults to today, same as before.
 */
@Component
public class ReportDateRangeResolver {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    /** Well before any real case data — the "no lower bound" stand-in for {@link #resolve}. */
    private static final Instant EARLIEST_POSSIBLE = Instant.EPOCH;

    public ReportDateRange resolve(LocalDate from, LocalDate to) {
        return resolve(from, to, 0);
    }

    /**
     * Same defaulting as {@link #resolve(LocalDate, LocalDate)}, then shifts
     * both ends back by {@code yearsAgo} years — for a year-over-year
     * comparison report that needs "the same calendar dates one year
     * earlier" without re-deriving the "no date given" rule itself.
     */
    public ReportDateRange resolve(LocalDate from, LocalDate to, long yearsAgo) {
        LocalDate today = LocalDate.now(APPLICATION_ZONE).minusYears(yearsAgo);

        if (from == null && to == null) {
            return new ReportDateRange(EARLIEST_POSSIBLE, today.plusDays(1).atStartOfDay(APPLICATION_ZONE).toInstant());
        }

        LocalDate effectiveFrom = (from != null ? from.minusYears(yearsAgo) : today);
        LocalDate effectiveTo = (to != null ? to.minusYears(yearsAgo) : today);

        return new ReportDateRange(
                effectiveFrom.atStartOfDay(APPLICATION_ZONE).toInstant(),
                effectiveTo.plusDays(1).atStartOfDay(APPLICATION_ZONE).toInstant()
        );
    }
}
