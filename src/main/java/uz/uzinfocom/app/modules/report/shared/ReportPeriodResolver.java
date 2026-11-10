package uz.uzinfocom.app.modules.report.shared;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * Turns a {@code (year, ReportPeriod)} selection into the four {@link
 * ReportDateRange}s "Form 10" compares: the "Joriy davr" and "Yig'ma" spans
 * for the chosen year and for the year before it (year-over-year). All spans
 * are half-open {@code [from, toExclusive)} instants in the application's
 * reporting zone, matching {@link ReportDateRangeResolver}.
 * <ul>
 *   <li><b>Joriy davr</b>: {@code period.startMonth()}‥{@code period.endMonth()}
 *   of the year.</li>
 *   <li><b>Yig'ma</b>: January‥{@code period.endMonth()} of the year (always
 *   from the start of the year).</li>
 * </ul>
 * The previous-year spans use the <b>same month numbers</b> shifted back one
 * calendar year — so "9 oylik 2026" ("Yig'ma" Jan–Sep 2026) is compared with
 * Jan–Sep 2025, and "Yanvar 2027" with January 2026.
 */
@Component
public class ReportPeriodResolver {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    /** "Joriy davr" span for {@code year}: {@code [startMonth, endMonth]} of that year. */
    public ReportDateRange current(int year, ReportPeriod period) {
        return range(year, period.startMonth(), period.endMonth());
    }

    /** "Yig'ma" span for {@code year}: January through {@code endMonth} of that year. */
    public ReportDateRange cumulative(int year, ReportPeriod period) {
        return range(year, 1, period.endMonth());
    }

    private ReportDateRange range(int year, int startMonth, int endMonth) {
        LocalDate from = LocalDate.of(year, startMonth, 1);
        LocalDate toExclusive = YearMonth.of(year, endMonth).atEndOfMonth().plusDays(1);
        return new ReportDateRange(
                from.atStartOfDay(APPLICATION_ZONE).toInstant(),
                toExclusive.atStartOfDay(APPLICATION_ZONE).toInstant()
        );
    }
}
