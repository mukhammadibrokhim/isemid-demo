package uz.uzinfocom.app.platform.devpanel.application.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevCacheClearResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.DevCacheQueryService;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
public class DevCacheCommandService {

    private static final Logger log = LoggerFactory.getLogger(DevCacheCommandService.class);

    private final CacheManager cacheManager;
    private final DevCacheQueryService queryService;

    public DevCacheCommandService(
            @Qualifier("securityCacheManager") CacheManager cacheManager,
            DevCacheQueryService queryService
    ) {
        this.cacheManager = cacheManager;
        this.queryService = queryService;
    }

    public DevCacheClearResponse clear(String name) {
        Cache cache = cacheManager.getCache(name);
        if (cache == null) {
            throw new NotFoundException("cache.not-found", name);
        }
        if (DevCacheQueryService.PROTECTED_CACHES.contains(name)) {
            return new DevCacheClearResponse(List.of(), List.of(name), 0);
        }
        long removed = queryService.sizeOf(cache);
        cache.clear();
        log.info("Dev panel cleared cache {} ({} entries)", name, removed);
        return new DevCacheClearResponse(List.of(name), List.of(), removed);
    }

    public DevCacheClearResponse clearAll() {
        List<String> cleared = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        long removed = 0;
        for (String name : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(name);
            if (cache == null) {
                continue;
            }
            if (DevCacheQueryService.PROTECTED_CACHES.contains(name)) {
                skipped.add(name);
                continue;
            }
            removed += queryService.sizeOf(cache);
            cache.clear();
            cleared.add(name);
        }
        log.info("Dev panel cleared all caches: {} caches, {} entries, skipped {}", cleared.size(), removed, skipped);
        return new DevCacheClearResponse(cleared, skipped, removed);
    }
}
