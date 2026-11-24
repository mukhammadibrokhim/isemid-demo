package uz.uzinfocom.app.modules.report.form281.application.query.dto;

/**
 * One count row per organization id, restricted to one nosological form's
 * ICD-10 set — the raw material for the "Form 28.1" geography drill-down (a
 * single nosological form broken down region→district→organization via {@code
 * ReportHierarchyService}). Same column set as {@link Form281Counts}.
 */
public record Form281OrganizationCountProjection(long organizationId, Form281Counts counts) {
}
