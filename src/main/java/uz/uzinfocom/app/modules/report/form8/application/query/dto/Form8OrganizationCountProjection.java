package uz.uzinfocom.app.modules.report.form8.application.query.dto;

/**
 * Raw per-organization confirmed-notification count produced by {@code
 * Form8ReportRepository} — one row per organization (grouped query) or a
 * single unattributed total row ({@code organizationId == null}, total
 * query). Internal to the report's query service; the web layer never sees
 * this shape directly, see {@code Form8ReportNodeResponse}.
 */
public record Form8OrganizationCountProjection(Long organizationId, long total) {

    public static Form8OrganizationCountProjection empty(Long organizationId) {
        return new Form8OrganizationCountProjection(organizationId, 0);
    }

    public static Form8OrganizationCountProjection add(
            Form8OrganizationCountProjection a, Form8OrganizationCountProjection b
    ) {
        return new Form8OrganizationCountProjection(a.organizationId(), a.total() + b.total());
    }
}
