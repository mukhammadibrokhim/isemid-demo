package uz.uzinfocom.app.platform.reference.application.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.lookup.dto.ReferenceItem;
import uz.uzinfocom.app.platform.reference.application.lookup.projection.ReferenceItemProjection;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.platform.reference.repository.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class ReferenceCacheLoader {

    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final CatalogRepository catalogRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheConfig.REF_LOOKUP_COUNTRIES, key = "'all'", sync = true)
    public Map<String, ReferenceItem> loadCountries() {
        return toMap(countryRepository.findAllReferenceItems());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheConfig.REF_LOOKUP_REGIONS, key = "'all'", sync = true)
    public Map<String, ReferenceItem> loadRegions() {
        return toMap(regionRepository.findAllReferenceItems());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheConfig.REF_LOOKUP_DISTRICTS, key = "'all'", sync = true)
    public Map<String, ReferenceItem> loadDistricts() {
        return toMap(districtRepository.findAllReferenceItems());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheConfig.REF_LOOKUP_NEIGHBORHOODS, key = "'all'", sync = true)
    public Map<String, ReferenceItem> loadNeighborhoods() {
        return toMap(neighborhoodRepository.findAllReferenceItems());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheConfig.REF_CATALOG_BY_TYPE, key = "#type", sync = true)
    public Map<String, ReferenceItem> loadCatalog(String type) {
        return toMap(catalogRepository.findAllProjectedByTypeAndDeletedFalseOrderByNameUzAsc(type));
    }

    private Map<String, ReferenceItem> toMap(List<ReferenceItemProjection> projections) {
        LinkedHashMap<String, ReferenceItem> result = new LinkedHashMap<>();

        for (ReferenceItemProjection projection : projections) {
            ReferenceItem previous = result.put(
                    projection.getCode(),
                    new ReferenceItem(
                            projection.getCode(),
                            projection.getParentCode(),
                            projection.getNameUz(),
                            projection.getNameUzCyril(),
                            projection.getNameRu(),
                            projection.getNameKaa()
                    )
            );

            if (previous != null) {
                throw new IllegalStateException("Duplicate reference code in cache load: " + projection.getCode());
            }
        }

        return Map.copyOf(result);
    }
}
