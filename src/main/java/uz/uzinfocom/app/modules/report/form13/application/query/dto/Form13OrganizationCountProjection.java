package uz.uzinfocom.app.modules.report.form13.application.query.dto;

/**
 * One count row per organization id — for the "Form 13 by disease" geography
 * drill-down (a single {@code FORM_13} catalog entry broken down
 * region→district→organization via {@code ReportHierarchyService}). Same
 * three metrics as {@link Form13Metric}.
 */
public record Form13OrganizationCountProjection(long organizationId, long total, long under14, long under18) {
}
