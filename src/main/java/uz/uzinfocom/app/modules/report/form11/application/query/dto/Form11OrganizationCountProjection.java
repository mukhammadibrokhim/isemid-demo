package uz.uzinfocom.app.modules.report.form11.application.query.dto;

/**
 * Raw per-organization count produced by {@code Form11ReportRepository}'s
 * grouped query — one row per {@code sender_organization_id}, carrying the
 * "Form 11" total plus its urban / rural / under-18 cuts. Internal to the
 * report's query service; the web layer never sees this shape directly, see
 * {@code Form11ReportNodeResponse}.
 */
public record Form11OrganizationCountProjection(
        long organizationId,
        long total,
        long city,
        long rural,
        long child
) {
}
