package uz.uzinfocom.app.modules.report.form281.application.query.dto;

/**
 * One count row per (organization id, confirmed-diagnosis code) pair — the raw
 * material for the "Form 28.1 by territory" geography breakdown, where every
 * disease is its own column against every territory row. {@code
 * Form281ByTerritoryGeographyCountSource} groups these by organization, then
 * rolls each organization's per-code counts up to one {@link Form281Counts} per
 * {@code FORM_28_1} catalog entry; {@code ReportHierarchyService} then buckets
 * those per-organization rows into region / district totals.
 */
public record Form281OrganizationDiagnosisCountProjection(long organizationId, String code, Form281Counts counts) {
}
