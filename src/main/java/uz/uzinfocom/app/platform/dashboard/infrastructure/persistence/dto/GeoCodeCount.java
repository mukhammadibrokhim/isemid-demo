package uz.uzinfocom.app.platform.dashboard.infrastructure.persistence.dto;

/**
 * Internal (non-API) pair — a region/district code with its case count,
 * before {@code HomeDashboardQueryService} attaches the locale-resolved
 * display name (a cached lookup, kept in the application layer rather than
 * duplicating {@code ReferenceLookupService}'s locale-selection logic in SQL).
 */
public record GeoCodeCount(String code, long count) {
}
