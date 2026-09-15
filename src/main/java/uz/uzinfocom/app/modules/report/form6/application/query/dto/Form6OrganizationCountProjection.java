package uz.uzinfocom.app.modules.report.form6.application.query.dto;

/**
 * Raw per-organization primary-notification count produced by {@code
 * Form6ReportRepository} — one row per organization (grouped query) or a
 * single unattributed total row ({@code organizationId == null}, total
 * query). Internal to the report's query service; the web layer never sees
 * this shape directly, see {@code Form6ReportNodeResponse}.
 */
public record Form6OrganizationCountProjection(Long organizationId, long total) {

    public static Form6OrganizationCountProjection empty(Long organizationId) {
        return new Form6OrganizationCountProjection(organizationId, 0);
    }

    public static Form6OrganizationCountProjection add(
            Form6OrganizationCountProjection a, Form6OrganizationCountProjection b
    ) {
        return new Form6OrganizationCountProjection(a.organizationId(), a.total() + b.total());
    }
}
