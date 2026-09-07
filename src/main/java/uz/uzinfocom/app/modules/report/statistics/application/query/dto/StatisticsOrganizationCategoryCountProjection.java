package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

/**
 * One aggregate row per (organization id, {@link StatisticsFormType form},
 * {@code patient.category_code}) triple — the raw material for the report's
 * geography drill-down, grouped by {@code sender_organization_id} then rolled
 * up per organization by {@code StatisticsGeographyCountSource} into one
 * {@link StatisticsCounts overall} total plus a per-known-category breakdown,
 * per form.
 */
public record StatisticsOrganizationCategoryCountProjection(
        long organizationId, StatisticsFormType formType, String categoryCode, StatisticsCounts counts
) {
}
