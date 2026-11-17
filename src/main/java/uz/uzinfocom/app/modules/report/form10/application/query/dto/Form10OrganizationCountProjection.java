package uz.uzinfocom.app.modules.report.form10.application.query.dto;

/**
 * Raw per-organization count produced by {@code Form10ReportRepository}'s
 * grouped query — one row per {@code sender_organization_id}, carrying the
 * "Form 10" total and its under-14 cut for a single date span. Internal to
 * the report's query service; the web layer never sees this shape directly
 * (see {@code Form10ReportNodeResponse}).
 */
public record Form10OrganizationCountProjection(long organizationId, long total, long child) {
}
