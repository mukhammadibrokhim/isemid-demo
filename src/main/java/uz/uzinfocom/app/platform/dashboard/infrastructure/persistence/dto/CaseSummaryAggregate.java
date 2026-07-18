package uz.uzinfocom.app.platform.dashboard.infrastructure.persistence.dto;

/**
 * Internal (non-API) aggregate — the four numbers {@code
 * CaseStatsAggregateRepository.caseSummary} computes in a single DB round
 * trip across form058+form058_1. {@code HomeDashboardQueryService} turns
 * this into the public {@code CaseSummaryResponse}.
 */
public record CaseSummaryAggregate(
        long form058Total,
        long form0581Total,
        long activeTotal,
        long todayTotal
) {
}
