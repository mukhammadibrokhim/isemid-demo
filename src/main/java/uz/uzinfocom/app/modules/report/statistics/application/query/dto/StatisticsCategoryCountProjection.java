package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

/**
 * One aggregate row per {@code patient.category_code} value (including
 * {@code null}, for patients with no category assigned) across a whole
 * organization scope — the raw material for the report's root node. {@code
 * categoryCode} is the raw column value; {@code
 * StatisticsGeographyCountSource} decides which of these belong to a known
 * {@code ref_catalog} {@code CATEGORY} entry and which only ever contribute
 * to the node's {@link StatisticsCounts overall} total.
 */
public record StatisticsCategoryCountProjection(String categoryCode, StatisticsCounts counts) {
}
