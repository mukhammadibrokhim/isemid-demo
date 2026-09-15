package uz.uzinfocom.app.modules.report.form9.application.query.dto;

/**
 * "Form 9" count aggregate — the shape {@code C} this report plugs into
 * {@code ReportHierarchyService} / {@code ReportCountSource}. Unlike "Form 6"
 * and "Form 8" (a plain {@link Long}), "Form 9" carries two metrics per node:
 * <ul>
 *   <li>{@code registered} — primary/not-yet-decided notifications
 *   ({@code form058} + {@code form058_1}, status NOT IN (APPROVED, CANCELED));</li>
 *   <li>{@code hospitalized} — the same set restricted to rows with a
 *   hospitalization reference ({@code form058.hospital_place_id} /
 *   {@code form058_1.hospitalized_at}).</li>
 * </ul>
 * {@code hospitalized} is always a subset of {@code registered}.
 */
public record Form9Counts(long registered, long hospitalized) {

    public static final Form9Counts EMPTY = new Form9Counts(0, 0);

    /** Component-wise add — used for the shared hierarchy engine's {@code merge}. */
    public Form9Counts plus(Form9Counts other) {
        return new Form9Counts(this.registered + other.registered, this.hospitalized + other.hospitalized);
    }
}
