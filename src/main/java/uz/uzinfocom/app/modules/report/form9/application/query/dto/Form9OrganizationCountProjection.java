package uz.uzinfocom.app.modules.report.form9.application.query.dto;

/**
 * Raw per-organization count produced by {@code Form9ReportRepository}'s
 * grouped query — one row per {@code sender_organization_id}, carrying both
 * "Form 9" metrics. Internal to the report's query service; the web layer
 * never sees this shape directly, see {@code Form9ReportNodeResponse}.
 */
public record Form9OrganizationCountProjection(long organizationId, long registered, long hospitalized) {
}
