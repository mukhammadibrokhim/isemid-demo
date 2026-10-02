package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

/**
 * Optional age cross-filter for "Statistika" — the same 18-year cut the report
 * already breaks every block down by, promoted to a whole-report filter so the
 * frontend can drill the entire table into one age band by clicking the
 * age slice of a node. Age is taken as of each case's own {@code created_at}
 * ({@code extract(year from age(created_at, birth_date))}), matching every
 * other report and the report's own age breakdown.
 */
public enum StatisticsAgeGroup {

    /** {@code age_years < 18} (cases with no birth date are excluded, same as the age breakdown). */
    UNDER_18,

    /** {@code age_years >= 18}. */
    ADULT
}
