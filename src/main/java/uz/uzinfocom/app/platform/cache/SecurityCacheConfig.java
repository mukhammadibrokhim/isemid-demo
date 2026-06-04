package uz.uzinfocom.app.platform.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class SecurityCacheConfig {

    @Bean(name = "securityCacheManager")
    public CacheManager securityCacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();

        manager.setCaches(List.of(
                cache(SecurityCacheNames.JWKS_BY_URI, 256, Duration.ofHours(2)),

                // SSO public-key endpoint normally changes very rarely.
                // 23h is safer than 24h because it leaves a small rotation buffer.
                cache(SecurityCacheNames.PUBLIC_KEY_DECODER_BY_PROVIDER, 32, Duration.ofHours(23)),

                cache(SecurityCacheNames.ORGANIZATION_SYNC_BY_PROVIDER_AND_UUID, 20_000, Duration.ofMinutes(30)),
                cache(SecurityCacheNames.ROLE_BY_NAME, 1_000, Duration.ofHours(2)),
                cache(SecurityCacheNames.SECURITY_USER_BY_ID, 20_000, Duration.ofMinutes(15)),
                cache(SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID, 50_000, Duration.ofMinutes(15)),
                cache(SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID, 20_000, Duration.ofMinutes(15)),
                cache(SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS, 5_000, Duration.ofMinutes(30)),

                cache(ReferenceCacheNames.REF_COUNTRIES, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheNames.REF_COUNTRY_BY_CODE, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheNames.REF_REGIONS, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheNames.REF_REGION_BY_CODE, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheNames.REF_REGIONS_BY_PARENT_CODE, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheNames.REF_DISTRICTS, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheNames.REF_DISTRICT_BY_CODE, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheNames.REF_DISTRICTS_BY_PARENT_CODE, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheNames.REF_MAHALLAS, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheNames.REF_MAHALLA_BY_CODE, 50_000, Duration.ofHours(1)),
                cache(ReferenceCacheNames.REF_MAHALLAS_BY_PARENT_CODE, 50_000, Duration.ofHours(1))
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
