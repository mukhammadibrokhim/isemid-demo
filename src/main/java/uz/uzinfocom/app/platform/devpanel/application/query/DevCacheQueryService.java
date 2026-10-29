package uz.uzinfocom.app.platform.devpanel.application.query;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevCacheResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class DevCacheQueryService {

    /**
     * Never cleared from the dev panel: emptying the revoked-token blacklist
     * would make already-revoked (logged-out) tokens valid again until they expire.
     */
    public static final Set<String> PROTECTED_CACHES = Set.of(SecurityCacheNames.REVOKED_TOKEN_BLACKLIST);

    private final CacheManager cacheManager;

    public DevCacheQueryService(@Qualifier("securityCacheManager") CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public List<DevCacheResponse> findAll() {
        return cacheManager.getCacheNames().stream()
                .sorted(Comparator.naturalOrder())
                .map(cacheManager::getCache)
                .filter(java.util.Objects::nonNull)
                .map(this::toResponse)
                .toList();
    }

    private DevCacheResponse toResponse(Cache cache) {
        long size = 0;
        CacheStats stats = CacheStats.empty();
        if (cache instanceof CaffeineCache caffeine) {
            size = caffeine.getNativeCache().estimatedSize();
            stats = caffeine.getNativeCache().stats();
        }
        return new DevCacheResponse(
                cache.getName(),
                size,
                stats.hitCount(),
                stats.missCount(),
                stats.hitRate(),
                stats.evictionCount(),
                PROTECTED_CACHES.contains(cache.getName())
        );
    }

    public long sizeOf(Cache cache) {
        return cache instanceof CaffeineCache caffeine ? caffeine.getNativeCache().estimatedSize() : 0;
    }
}
