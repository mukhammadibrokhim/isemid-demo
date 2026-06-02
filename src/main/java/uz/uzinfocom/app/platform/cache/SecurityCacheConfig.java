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

                cache(SecurityCacheNames.IAM_ORGANIZATION_BY_UUID, 20_000, Duration.ofMinutes(30)),
                cache(SecurityCacheNames.IAM_ROLE_BY_NAME, 1_000, Duration.ofHours(2))
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
