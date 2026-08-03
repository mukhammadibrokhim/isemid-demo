package uz.uzinfocom.app.platform.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.uzinfocom.app.platform.iam.application.shared.cache.AuditCacheConfig;
import uz.uzinfocom.app.platform.iam.application.shared.cache.OrganizationCacheConfig;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.platform.settings.config.SettingsCacheConfig;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class ApplicationCacheConfig {

    @Bean(name = "securityCacheManager")
    public CacheManager securityCacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();

        manager.setCaches(List.of(
                cache(SecurityCacheNames.JWKS_BY_URI, 256, Duration.ofHours(2)),

                // SSO public-key endpoint normally changes very rarely.
                // 23h is safer than 24h because it leaves a small rotation buffer.
                cache(SecurityCacheNames.PUBLIC_KEY_DECODER_BY_PROVIDER, 32, Duration.ofHours(23)),

                // 6h TTL: long enough to outlive any SSO/DHP-issued token's own
                // lifetime (so a blacklisted token is never accepted again before
                // it would have expired anyway), short enough to bound cache size
                // without a separate cleanup job - see SecurityCacheNames javadoc.
                cache(SecurityCacheNames.REVOKED_TOKEN_BLACKLIST, 100_000, Duration.ofHours(6)),

                cache(SecurityCacheNames.ORGANIZATION_SYNC_BY_PROVIDER_AND_UUID, 20_000, Duration.ofMinutes(30)),
                cache(SecurityCacheNames.ROLE_BY_NAME, 1_000, Duration.ofHours(2)),
                cache(SecurityCacheNames.SECURITY_USER_BY_ID, 20_000, Duration.ofMinutes(15)),
                cache(SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID, 50_000, Duration.ofMinutes(15)),
                cache(SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID, 20_000, Duration.ofMinutes(15)),
                cache(SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS, 5_000, Duration.ofMinutes(30)),
                cache(SecurityCacheNames.SCOPE_ORGANIZATION_IDS, 5_000, Duration.ofDays(1)),
                cache(SecurityCacheNames.FILTER_ORGANIZATION_IDS_BY_REGION_DISTRICT, 5_000, Duration.ofDays(1)),

                cache(AuditCacheConfig.AUDIT_USER_BY_ID, 50_000, Duration.ofHours(1)),
                cache(OrganizationCacheConfig.ORGANIZATION_ID_BY_UUID, 20_000, Duration.ofHours(12)),
                cache(OrganizationCacheConfig.ORGANIZATION_NAME_BY_ID, 20_000, Duration.ofHours(12)),

                cache(ReferenceCacheConfig.REF_COUNTRIES, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_COUNTRY_BY_CODE, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_REGIONS, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_REGION_BY_CODE, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_REGIONS_BY_PARENT_CODE, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_DISTRICTS, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_DISTRICT_BY_CODE, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_DISTRICTS_BY_PARENT_CODE, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_NEIGHBORHOODS, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_NEIGHBORHOOD_BY_CODE, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_NEIGHBORHOODS_BY_PARENT_CODE, 50_000, Duration.ofHours(1)),

                cache(ReferenceCacheConfig.REF_LOOKUP_COUNTRIES, 10, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_LOOKUP_REGIONS, 10, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_LOOKUP_DISTRICTS, 10, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_LOOKUP_NEIGHBORHOODS, 10, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_CATALOG_BY_TYPE, 1_000, Duration.ofHours(1)),

                cache(ReferenceCacheConfig.REF_MANUAL_REPORT_BY_CODE, 5_000, Duration.ofHours(1)),
                cache(ReferenceCacheConfig.REF_MANUAL_REPORTS_BY_ICD10_CODE, 20_000, Duration.ofHours(1)),

                cache(ReferenceCacheConfig.REF_ICD10_BY_CODE, 50_000, Duration.ofHours(2)),
                // Small size: at most one entry per supported locale (uz/uz-Cyrl/ru/kaa).
                cache(ReferenceCacheConfig.REF_ICD10_ROOTS, 20, Duration.ofHours(2)),
                cache(ReferenceCacheConfig.REF_ICD10_CHILDREN_BY_PARENT_ID, 20_000, Duration.ofHours(2)),

                // Short TTL by design: these back runtime-editable configuration
                // (see SystemSettingResolver/RouteAccessPolicyResolver) - a write
                // evicts the affected entry immediately, but the short TTL is a
                // safety net so a value never goes stale for more than ~30s even
                // if an eviction is ever missed.
                cache(SettingsCacheConfig.SYSTEM_SETTING_BY_KEY, 2_000, Duration.ofSeconds(30)),
                cache(SettingsCacheConfig.ROUTE_ACCESS_POLICIES, 4, Duration.ofSeconds(30))
        ));

        return manager;
    }

    private Cache cache(String name, long maximumSize, Duration ttl) {
        return new CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .maximumSize(maximumSize)
                        .expireAfterWrite(ttl)
                        .recordStats()
                        .build()
        );
    }
}
