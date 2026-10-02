package uz.uzinfocom.app.modules.report.form13.application.query.dto;

/**
 * One count row per (organization id, confirmed-diagnosis code) pair — the raw
 * material for a "Form 13" geography breakdown. {@code
 * Form13GeographyCountSource} groups these by organization, then rolls each
 * organization's per-code counts up to one {@link Form13Metric} per {@code
 * FORM_13} catalog entry; {@code ReportHierarchyService} then buckets those
 * per-organization rows into region / district totals.
 */
public record Form13OrganizationDiagnosisCountProjection(
        long organizationId, String code, long total, long under14, long under18
) {
}
