package uz.uzinfocom.app.modules.report.form282.application.query.dto;

/**
 * One count row per (organization id, confirmed-diagnosis code) pair — the raw
 * material for the "Form 28.2 by territory" geography breakdown, where every
 * disease is its own column against every territory row. {@code
 * Form282ByTerritoryGeographyCountSource} groups these by organization, then
 * rolls each organization's per-code counts up to one {@link Form282Counts} per
 * {@code FORM_28_2} catalog entry; {@code ReportHierarchyService} then buckets
 * those per-organization rows into region / district totals.
 */
public record Form282OrganizationDiagnosisCountProjection(long organizationId, String code, Form282Counts counts) {
}
