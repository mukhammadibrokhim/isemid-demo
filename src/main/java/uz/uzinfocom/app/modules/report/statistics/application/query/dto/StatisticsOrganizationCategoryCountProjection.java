package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

/**
 * One aggregate row per (organization id, {@code patient.category_code})
 * pair — the raw material for the report's geography drill-down, grouped by
 * {@code sender_organization_id} then rolled up per organization by {@code
 * StatisticsGeographyCountSource} into one {@link StatisticsCounts overall}
 * total plus a per-known-category breakdown.
 */
public record StatisticsOrganizationCategoryCountProjection(long organizationId, String categoryCode, StatisticsCounts counts) {
}
