package uz.uzinfocom.app.modules.report.form10.application.query.dto;

/**
 * "Form 10" count aggregate — the shape {@code C} this report plugs into
 * {@code ReportHierarchyService} / {@code ReportCountSource}, for one date
 * span. Two metrics:
 * <ul>
 *   <li>{@code total} — every confirmed notification ({@code form058} +
 *   {@code form058_1}, {@code status = 'APPROVED'}) in the span;</li>
 *   <li>{@code child} — the subset whose patient was under 14 complete years
 *   old at {@code created_at}.</li>
 * </ul>
 * {@code child} is a subset of {@code total}. "Form 10" runs four spans per
 * request (Joriy davr / Yig'ma, current year / previous year), each producing
 * one of these.
 */
public record Form10Counts(long total, long child) {

    public static final Form10Counts EMPTY = new Form10Counts(0, 0);

    /** Component-wise add — the shared hierarchy engine's {@code merge}. */
    public Form10Counts plus(Form10Counts other) {
        return new Form10Counts(this.total + other.total, this.child + other.child);
    }
}
