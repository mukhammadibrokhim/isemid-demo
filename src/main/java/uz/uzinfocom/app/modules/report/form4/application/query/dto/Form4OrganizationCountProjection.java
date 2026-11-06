package uz.uzinfocom.app.modules.report.form4.application.query.dto;

/**
 * Raw per-organization aggregate row produced by {@code
 * Form4ReportRepository} — one row per organization (grouped query) or a
 * single unattributed total row ({@code organizationId == null}, total
 * query). Every count here is computed by the database (Postgres {@code
 * count(*) filter (where ...)}); this record only carries the already-
 * aggregated numbers back to Java, never a raw case/patient row. Bucket
 * columns follow {@code patient.category_code} against the {@code
 * ref_catalog} {@code CATEGORY} type, split by the same CONFIRMED ({@code
 * status = 'APPROVED'}) / PRIMARY ({@code status NOT IN ('APPROVED',
 * 'CANCELED')}) metrics as {@code Form1OrganizationCountProjection}. The
 * seeded {@code TEACHER} category is not broken out on this report but
 * still counts toward each metric's {@code total}.
 */
public record Form4OrganizationCountProjection(
        Long organizationId,

        long confirmedTotal,
        long confirmedUnorganizedPreschool,
        long confirmedOrganizedPreschool,
        long confirmedSchoolStudents,
        long confirmedVocationalStudents,
        long confirmedUniversityStudents,
        long confirmedEmployees,
        long confirmedWorkers,
        long confirmedMedicalStaff,
        long confirmedUnemployed,
        long confirmedPensioners,
        long confirmedUnsheltered,

        long primaryTotal,
        long primaryUnorganizedPreschool,
        long primaryOrganizedPreschool,
        long primarySchoolStudents,
        long primaryVocationalStudents,
        long primaryUniversityStudents,
        long primaryEmployees,
        long primaryWorkers,
        long primaryMedicalStaff,
        long primaryUnemployed,
        long primaryPensioners,
        long primaryUnsheltered
) {
    public static Form4OrganizationCountProjection empty(Long organizationId) {
        return new Form4OrganizationCountProjection(
                organizationId,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        );
    }

    /** Component-wise sum, used to merge per-organization rows into a per-region/per-district bucket. */
    public static Form4OrganizationCountProjection add(
            Form4OrganizationCountProjection a, Form4OrganizationCountProjection b
    ) {
        return new Form4OrganizationCountProjection(
                a.organizationId(),
                a.confirmedTotal() + b.confirmedTotal(),
                a.confirmedUnorganizedPreschool() + b.confirmedUnorganizedPreschool(),
                a.confirmedOrganizedPreschool() + b.confirmedOrganizedPreschool(),
                a.confirmedSchoolStudents() + b.confirmedSchoolStudents(),
                a.confirmedVocationalStudents() + b.confirmedVocationalStudents(),
                a.confirmedUniversityStudents() + b.confirmedUniversityStudents(),
                a.confirmedEmployees() + b.confirmedEmployees(),
                a.confirmedWorkers() + b.confirmedWorkers(),
                a.confirmedMedicalStaff() + b.confirmedMedicalStaff(),
                a.confirmedUnemployed() + b.confirmedUnemployed(),
                a.confirmedPensioners() + b.confirmedPensioners(),
                a.confirmedUnsheltered() + b.confirmedUnsheltered(),
                a.primaryTotal() + b.primaryTotal(),
                a.primaryUnorganizedPreschool() + b.primaryUnorganizedPreschool(),
                a.primaryOrganizedPreschool() + b.primaryOrganizedPreschool(),
                a.primarySchoolStudents() + b.primarySchoolStudents(),
                a.primaryVocationalStudents() + b.primaryVocationalStudents(),
                a.primaryUniversityStudents() + b.primaryUniversityStudents(),
                a.primaryEmployees() + b.primaryEmployees(),
                a.primaryWorkers() + b.primaryWorkers(),
                a.primaryMedicalStaff() + b.primaryMedicalStaff(),
                a.primaryUnemployed() + b.primaryUnemployed(),
                a.primaryPensioners() + b.primaryPensioners(),
                a.primaryUnsheltered() + b.primaryUnsheltered()
        );
    }
}
