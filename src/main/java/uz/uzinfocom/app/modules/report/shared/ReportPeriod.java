package uz.uzinfocom.app.modules.report.shared;

/**
 * A reporting period selector for the "Joriy davr / Yig'ma" family of reports
 * (currently only "Form 10"). Every value resolves to an inclusive month span
 * {@code [startMonth, endMonth]} within a single calendar year:
 * <ul>
 *   <li>the <b>"Joriy davr"</b> block covers that span exactly — one month
 *   ({@link #JANUARY}…{@link #DECEMBER}), a quarter's three months ({@link
 *   #Q1}…{@link #Q4}), or the half-year / nine-month / full-year span;</li>
 *   <li>the <b>"Yig'ma"</b> block always runs from January 1 to {@link
 *   #endMonth()} — cumulative from the start of the year regardless of which
 *   value is picked.</li>
 * </ul>
 * So {@link #Q2} ("Joriy" = Apr–Jun, "Yig'ma" = Jan–Jun) is distinct from
 * {@link #HALF_YEAR} ("Joriy" = Jan–Jun, "Yig'ma" = Jan–Jun), and likewise
 * {@link #Q3} vs {@link #NINE_MONTHS}, {@link #Q4} vs {@link #YEAR}.
 * <p>
 * The actual {@link ReportDateRange} pair is produced by {@link
 * ReportPeriodResolver}.
 */
public enum ReportPeriod {

    JANUARY(1, 1),
    FEBRUARY(2, 2),
    MARCH(3, 3),
    APRIL(4, 4),
    MAY(5, 5),
    JUNE(6, 6),
    JULY(7, 7),
    AUGUST(8, 8),
    SEPTEMBER(9, 9),
    OCTOBER(10, 10),
    NOVEMBER(11, 11),
    DECEMBER(12, 12),

    /** 1-chorak — январь, февраль, март. */
    Q1(1, 3),
    /** 2-chorak — апрель, май, июнь. */
    Q2(4, 6),
    /** 3-chorak — июль, август, сентябрь. */
    Q3(7, 9),
    /** 4-chorak — октябрь, ноябрь, декабрь. */
    Q4(10, 12),

    /** Ярим йиллик — январь–июнь. */
    HALF_YEAR(1, 6),
    /** 9 ойлик — январь–сентябрь. */
    NINE_MONTHS(1, 9),
    /** Йиллик — январь–декабрь. */
    YEAR(1, 12);

    private final int startMonth;
    private final int endMonth;

    ReportPeriod(int startMonth, int endMonth) {
        this.startMonth = startMonth;
        this.endMonth = endMonth;
    }

    /** First month of the "Joriy davr" span, 1-based. */
    public int startMonth() {
        return startMonth;
    }

    /** Last month of the "Joriy davr" span (and the "Yig'ma" cumulative end), 1-based. */
    public int endMonth() {
        return endMonth;
    }

    /** The single-month value for {@code monthNumber} (1–12) — the default period. */
    public static ReportPeriod ofMonth(int monthNumber) {
        return values()[monthNumber - 1];
    }
}
