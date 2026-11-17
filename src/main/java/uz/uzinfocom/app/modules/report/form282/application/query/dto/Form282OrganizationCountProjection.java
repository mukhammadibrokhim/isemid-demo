package uz.uzinfocom.app.modules.report.form282.application.query.dto;

/**
 * One count row per organization id, restricted to one nosological form's
 * ICD-10 set — the raw material for the "Form 28.2" geography drill-down (a
 * single nosological form broken down region→district→organization via {@code
 * ReportHierarchyService}). Same column set as {@link Form282Counts}.
 */
public record Form282OrganizationCountProjection(long organizationId, Form282Counts counts) {
}
