package uz.uzinfocom.app.modules.report.form6.application.query.dto;

/**
 * One organization's age-group breakdown, from {@code
 * Form6ReportRepository#countAgeBreakdownGroupedByOrganization} — the same buckets as
 * {@link Form6AgeBreakdownProjection}, just attributed to a single organization id instead of
 * aggregated across a whole region/district subtree. Internal to the report's query service;
 * the web layer never sees this shape directly.
 */
public record Form6AgeBreakdownByOrganizationProjection(
        Long organizationId,
        Form6AgeBreakdownProjection breakdown
) {
}
