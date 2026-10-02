package uz.uzinfocom.app.modules.report.form2.application.query.dto;

/**
 * Raw per-organization aggregate row produced by {@code
 * Form2ReportRepository} — one row per organization (grouped query) or a
 * single unattributed total row ({@code organizationId == null}, total
 * query). Every count here is computed by the database (Postgres {@code
 * count(*) filter (where ...)}); this record only carries the already-
 * aggregated numbers back to Java, never a raw case/patient row — so memory
 * use per call is fixed-size (11 longs), independent of how many form058/
 * form058_1 rows matched. Bucket columns follow {@code patient.category_code}
 * against the {@code ref_catalog} {@code CATEGORY} type; the three seeded
 * categories not broken out on this report (PENSIONER, TEACHER, UNSHELTRED)
 * still count toward {@link #total()}. Internal to {@code
 * Form2ManualEntryQueryService}; the web layer never sees this shape
 * directly.
 */
public record Form2OrganizationCountProjection(
        Long organizationId,
        long total,
        long unorganizedPreschool,
        long organizedPreschool,
        long schoolStudents,
        long vocationalStudents,
        long universityStudents,
        long employees,
        long workers,
        long medicalStaff,
        long unemployed
) {
    public static Form2OrganizationCountProjection empty(Long organizationId) {
        return new Form2OrganizationCountProjection(organizationId, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
