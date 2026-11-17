package uz.uzinfocom.app.modules.report.form8.application.query.dto;

/**
 * One organization's social-category breakdown, from {@code
 * Form8ReportRepository#countCategoryBreakdownGroupedByOrganization} — the same buckets as
 * {@link Form8CategoryBreakdownProjection}, just attributed to a single organization id instead
 * of aggregated across a whole region/district subtree. Internal to the report's query service;
 * the web layer never sees this shape directly.
 */
public record Form8CategoryBreakdownByOrganizationProjection(
        Long organizationId,
        Form8CategoryBreakdownProjection breakdown
) {
}
