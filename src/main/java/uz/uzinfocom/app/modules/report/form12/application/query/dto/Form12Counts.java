package uz.uzinfocom.app.modules.report.form12.application.query.dto;

/**
 * "Form 12" count aggregate — the shape {@code C} this report plugs into
 * {@code ReportHierarchyService} / {@code ReportCountSource} for its geography
 * drill-down. Carries the confirmed ({@code status = 'APPROVED'}) case count
 * for one nosological form (or one geography node), plus the two age cuts the
 * report shows next to it:
 * <ul>
 *   <li>{@code total} — every APPROVED {@code form058} / {@code form058_1}
 *   notification whose confirmed diagnosis ({@code coalesce(final_icd10_code,
 *   icd10_code)}) is in the nosological form's code set;</li>
 *   <li>{@code under14} — the subset under 14 complete calendar years at
 *   {@code created_at};</li>
 *   <li>{@code under18} — the subset under 18 (a superset of {@code under14}).</li>
 * </ul>
 */
public record Form12Counts(long total, long under14, long under18) {

    public static final Form12Counts EMPTY = new Form12Counts(0, 0, 0);

    /** Component-wise add — used both for the hierarchy engine's {@code merge} and the code roll-up. */
    public Form12Counts plus(Form12Counts other) {
        return new Form12Counts(
                this.total + other.total,
                this.under14 + other.under14,
                this.under18 + other.under18
        );
    }
}
