package uz.uzinfocom.app.modules.report.form1.application.query.dto;

/**
 * Raw per-organization aggregate row produced by {@code
 * Form1ReportRepository} — one row per organization (grouped query) or a
 * single unattributed total row ({@code organizationId == null}, total
 * query). {@code organizationId} here identifies the institution that
 * <b>created</b> the case (form058/form058_1's {@code
 * sender_organization_id}), not the SES branch that received it — see
 * {@code Form1ReportRepository} for why. Internal to the report's query
 * service; the web layer never sees this shape directly, see {@link
 * Form1ReportNodeResponse}.
 */
public record Form1OrganizationCountProjection(
        Long organizationId,
        long confirmedTotal,
        long confirmedUnder14,
        long confirmedUnder18,
        long confirmedAdult,
        long confirmedFemale,
        long confirmedDiagnosisChanged,
        long primaryTotal,
        long primaryUnder14,
        long primaryUnder18,
        long primaryAdult,
        long primaryFemale
) {
    public static Form1OrganizationCountProjection empty(Long organizationId) {
        return new Form1OrganizationCountProjection(organizationId, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /** Component-wise sum, used to merge per-organization rows into a per-region/per-district bucket. */
    public static Form1OrganizationCountProjection add(
            Form1OrganizationCountProjection a,
            Form1OrganizationCountProjection b
    ) {
        return new Form1OrganizationCountProjection(
                a.organizationId(),
                a.confirmedTotal() + b.confirmedTotal(),
                a.confirmedUnder14() + b.confirmedUnder14(),
                a.confirmedUnder18() + b.confirmedUnder18(),
                a.confirmedAdult() + b.confirmedAdult(),
                a.confirmedFemale() + b.confirmedFemale(),
                a.confirmedDiagnosisChanged() + b.confirmedDiagnosisChanged(),
                a.primaryTotal() + b.primaryTotal(),
                a.primaryUnder14() + b.primaryUnder14(),
                a.primaryUnder18() + b.primaryUnder18(),
                a.primaryAdult() + b.primaryAdult(),
                a.primaryFemale() + b.primaryFemale()
        );
    }
}
