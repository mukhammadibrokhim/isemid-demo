package uz.uzinfocom.app.modules.report.form8.application.query.dto;

/**
 * Raw social-category-bucketed confirmed-notification counts produced by {@code
 * Form8ReportRepository#countCategoryBreakdown} — a single aggregate row over
 * an arbitrary organization-id subtree (a node's whole scope, not grouped by
 * organization). Buckets follow {@code patient.category_code} against the
 * {@code ref_catalog} {@code CATEGORY} type, exactly the set "Form 4" breaks
 * out (see {@code Form4OrganizationCountProjection}); the seeded {@code
 * TEACHER} category is not a bucket of its own but still counts toward {@link
 * #total()}. Internal to the report's query service; the web layer never sees
 * this shape directly, see {@code Form8CategoryRowResponse}.
 */
public record Form8CategoryBreakdownProjection(
        long total,
        long unorganizedPreschool,
        long organizedPreschool,
        long workers,
        long unemployed,
        long pensioners,
        long schoolStudents,
        long unsheltered,
        long employees,
        long medicalStaff,
        long vocationalStudents,
        long universityStudents
) {
    public static final Form8CategoryBreakdownProjection EMPTY =
            new Form8CategoryBreakdownProjection(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
}
