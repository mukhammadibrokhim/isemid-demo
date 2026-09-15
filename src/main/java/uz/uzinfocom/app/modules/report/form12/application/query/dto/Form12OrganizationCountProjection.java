package uz.uzinfocom.app.modules.report.form12.application.query.dto;

/**
 * One count row per organization id — for the "Form 12" geography drill-down
 * (a single nosological form broken down region→district→organization via
 * {@code ReportHierarchyService}). Same three metrics as {@link Form12Counts}.
 */
public record Form12OrganizationCountProjection(long organizationId, long total, long under14, long under18) {
}
