package uz.uzinfocom.app.modules.report.form11.application.query.dto;

/**
 * "Form 11" count aggregate — the shape {@code C} this report plugs into
 * {@code ReportHierarchyService} / {@code ReportCountSource}. Carries the
 * absolute case count plus the three current-period cuts the report shows
 * alongside it:
 * <ul>
 *   <li>{@code total} — every confirmed notification
 *   ({@code form058} + {@code form058_1}, status = 'APPROVED');</li>
 *   <li>{@code city} — the subset whose patient is {@code CITY_RESIDENT};</li>
 *   <li>{@code rural} — the subset whose patient is {@code VILLAGE_RESIDENT};</li>
 *   <li>{@code child} — the subset whose patient is under 18 at {@code created_at}.</li>
 * </ul>
 * {@code city}/{@code rural}/{@code child} are each a subset of {@code total}
 * ({@code city + rural} may be below {@code total} when a patient has no
 * population type recorded). Only the "previous year" load's {@code total} is
 * ever read — see {@code Form11ReportQueryService}.
 */
public record Form11Counts(long total, long city, long rural, long child) {

    public static final Form11Counts EMPTY = new Form11Counts(0, 0, 0, 0);

    /** Component-wise add — used for the shared hierarchy engine's {@code merge}. */
    public Form11Counts plus(Form11Counts other) {
        return new Form11Counts(
                this.total + other.total,
                this.city + other.city,
                this.rural + other.rural,
                this.child + other.child
        );
    }
}
