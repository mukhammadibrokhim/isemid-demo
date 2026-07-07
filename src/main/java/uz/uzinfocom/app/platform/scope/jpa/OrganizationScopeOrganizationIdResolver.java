package uz.uzinfocom.app.platform.scope.jpa;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class OrganizationScopeOrganizationIdResolver {

    private final OrganizationRepository organizationRepository;

    @Cacheable(
            cacheManager = "securityCacheManager",
            cacheNames = SecurityCacheNames.SCOPE_ORGANIZATION_IDS,
            key = "'scope:' + #mode.name() + ':' + (#regionCode == null ? '' : #regionCode) + ':' + (#districtCode == null ? '' : #districtCode)"
    )
    public List<Long> resolveScopeOrganizationIds(
            OrganizationScopeMode mode,
            String regionCode,
            String districtCode
    ) {
        if (mode == null) {
            throw new ScopeViolationException("organization.scope_violation");
        }

        return switch (mode) {
            case ALL -> List.of();

            case ORGANIZATION -> List.of();

            case REGION -> {
                String normalizedRegionCode = normalizeRequired(regionCode);
                yield organizationRepository.findActiveIdsByStateCode(normalizedRegionCode);
            }

            case DISTRICT -> {
                String normalizedDistrictCode = normalizeRequired(districtCode);
                yield organizationRepository.findActiveIdsByCityCode(normalizedDistrictCode);
            }
        };
    }

    /**
     * Resolves active organization ids matching an ad-hoc region/district filter
     * (as opposed to the mandatory SANEPID scope above). Materialized and cached
     * so callers can use `IN (:ids)` instead of `IN (subquery)` — Postgres estimates
     * a literal id list far more accurately than a correlated subquery, which
     * otherwise causes a full backward scan of large tables like form058.
     */
    @Cacheable(
            cacheManager = "securityCacheManager",
            cacheNames = SecurityCacheNames.FILTER_ORGANIZATION_IDS_BY_REGION_DISTRICT,
            key = "(#regionCode == null ? '' : #regionCode) + ':' + (#districtCode == null ? '' : #districtCode)"
    )
    public List<Long> resolveFilterOrganizationIds(String regionCode, String districtCode) {
        String normalizedRegionCode = StringUtils.hasText(regionCode) ? normalize(regionCode) : null;
        String normalizedDistrictCode = StringUtils.hasText(districtCode) ? normalize(districtCode) : null;

        return organizationRepository.findActiveIdsByRegionAndDistrict(normalizedRegionCode, normalizedDistrictCode);
    }

    private String normalizeRequired(String value) {
        if (!StringUtils.hasText(value)) {
            throw new ScopeViolationException("organization.scope_violation");
        }

        return normalize(value);
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}