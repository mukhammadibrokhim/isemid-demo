package uz.uzinfocom.app.modules.report.form12.application.query.dto;

/**
 * One count row per (organization id, confirmed-diagnosis code) pair — the raw
 * material for the "Form 12 by territory" geography breakdown, where every
 * nosological form is its own column against every territory row. {@code
 * Form12ByTerritoryGeographyCountSource} groups these by organization, then
 * rolls each organization's per-code counts up to one {@link Form12Counts} per
 * {@code FORM_12} catalog entry; {@code ReportHierarchyService} then buckets
 * those per-organization rows into region / district totals.
 */
public record Form12OrganizationDiagnosisCountProjection(long organizationId, String code, Form12Counts counts) {
}
