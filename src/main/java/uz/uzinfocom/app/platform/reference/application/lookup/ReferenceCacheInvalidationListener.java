package uz.uzinfocom.app.platform.reference.application.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uz.uzinfocom.app.platform.reference.application.common.event.CatalogChangedEvent;
import uz.uzinfocom.app.platform.reference.application.common.event.CountryChangedEvent;
import uz.uzinfocom.app.platform.reference.application.common.event.DistrictChangedEvent;
import uz.uzinfocom.app.platform.reference.application.common.event.NeighborhoodChangedEvent;
import uz.uzinfocom.app.platform.reference.application.common.event.RegionChangedEvent;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;

@Component
@RequiredArgsConstructor
public class ReferenceCacheInvalidationListener {

    @Qualifier("securityCacheManager")
    private final CacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CountryChangedEvent event) {
        evictAll(ReferenceCacheConfig.REF_COUNTRIES);
        evictAll(ReferenceCacheConfig.REF_COUNTRY_BY_CODE);
        evictAll(ReferenceCacheConfig.REF_LOOKUP_COUNTRIES);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(RegionChangedEvent event) {
        evictAll(ReferenceCacheConfig.REF_REGIONS);
        evictAll(ReferenceCacheConfig.REF_REGION_BY_CODE);
        evictAll(ReferenceCacheConfig.REF_REGIONS_BY_PARENT_CODE);
        evictAll(ReferenceCacheConfig.REF_LOOKUP_REGIONS);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(DistrictChangedEvent event) {
        evictAll(ReferenceCacheConfig.REF_DISTRICTS);
        evictAll(ReferenceCacheConfig.REF_DISTRICT_BY_CODE);
        evictAll(ReferenceCacheConfig.REF_DISTRICTS_BY_PARENT_CODE);
        evictAll(ReferenceCacheConfig.REF_LOOKUP_DISTRICTS);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(NeighborhoodChangedEvent event) {
        evictAll(ReferenceCacheConfig.REF_NEIGHBORHOODS);
        evictAll(ReferenceCacheConfig.REF_NEIGHBORHOOD_BY_CODE);
        evictAll(ReferenceCacheConfig.REF_NEIGHBORHOODS_BY_PARENT_CODE);
        evictAll(ReferenceCacheConfig.REF_LOOKUP_NEIGHBORHOODS);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CatalogChangedEvent event) {
        Cache cache = cacheManager.getCache(ReferenceCacheConfig.REF_CATALOG_BY_TYPE);
        if (cache != null && event.type() != null) {
            cache.evict(event.type().name());
        }
    }

    private void evictAll(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
