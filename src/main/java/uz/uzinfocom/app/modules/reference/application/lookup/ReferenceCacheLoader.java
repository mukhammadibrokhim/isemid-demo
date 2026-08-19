package uz.uzinfocom.app.modules.reference.application.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.GeoReferenceLookupTable;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.ReferenceItem;
import uz.uzinfocom.app.modules.reference.application.lookup.projection.GeoReferenceItemProjection;
import uz.uzinfocom.app.modules.reference.application.lookup.projection.ReferenceItemProjection;
import uz.uzinfocom.app.modules.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.modules.reference.repository.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
    public GeoReferenceLookupTable loadRegions() {
        return toGeoLookupTable(regionRepository.findAllReferenceItems());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheConfig.REF_LOOKUP_DISTRICTS, key = "'all'", sync = true)
    public GeoReferenceLookupTable loadDistricts() {
        return toGeoLookupTable(districtRepository.findAllReferenceItems());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheConfig.REF_LOOKUP_NEIGHBORHOODS, key = "'all'", sync = true)
    public GeoReferenceLookupTable loadNeighborhoods() {
        return toGeoLookupTable(neighborhoodRepository.findAllReferenceItems());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheConfig.REF_CATALOG_BY_TYPE, key = "#type", sync = true)
    public Map<String, ReferenceItem> loadCatalog(String type) {
        return toMap(catalogRepository.findAllProjectedByTypeAndDeletedFalseOrderByNameUzAsc(type));
    }

    private Map<String, ReferenceItem> toMap(List<ReferenceItemProjection> projections) {
        LinkedHashMap<String, ReferenceItem> result = new LinkedHashMap<>();

        for (ReferenceItemProjection projection : projections) {
            String key = projection.getCode().trim().toUpperCase(Locale.ROOT);
            ReferenceItem previous = result.put(key, toItem(projection, null, null, null));

            if (previous != null) {
                throw new IllegalStateException("Duplicate reference code in cache load: " + projection.getCode());
            }
        }

        return Map.copyOf(result);
    }

    /**
     * Builds four indexes off the same snapshot: by {@code code} (unique), by numeric SOATO id,
     * by {@code tin}, and by {@code uzcadRegistryCode} (the latter two neighborhoods only —
     * always empty for regions/districts). A SOATO id of {@code null} or {@code 0} means "not
     * assigned" in the source data (thousands of neighborhoods share it), so those entries are
     * simply left out of the SOATO index rather than treated as a lookup collision. Same
     * treatment for a blank {@code tin}/{@code uzcadRegistryCode}. Unlike {@code byCode}, none of
     * the other three indexes rejects a duplicate key with an exception — see
     * {@code uzcad_registry_code}'s migration comment for why that one specifically is only a
     * soft, not-guaranteed-unique key.
     */
    private GeoReferenceLookupTable toGeoLookupTable(List<GeoReferenceItemProjection> projections) {
        LinkedHashMap<String, ReferenceItem> byCode = new LinkedHashMap<>();
        LinkedHashMap<String, ReferenceItem> bySoatoId = new LinkedHashMap<>();
        LinkedHashMap<String, ReferenceItem> byTin = new LinkedHashMap<>();
        LinkedHashMap<String, ReferenceItem> byUzcadRegistryCode = new LinkedHashMap<>();

        for (GeoReferenceItemProjection projection : projections) {
            ReferenceItem item = toItem(
                    projection, projection.getSoatoId(), projection.getTin(), projection.getUzcadRegistryCode());

            ReferenceItem previous = byCode.put(projection.getCode(), item);
            if (previous != null) {
                throw new IllegalStateException("Duplicate reference code in cache load: " + projection.getCode());
            }

            Integer soatoId = projection.getSoatoId();
            if (soatoId != null && soatoId != 0) {
                bySoatoId.putIfAbsent(String.valueOf(soatoId), item);
            }

            String tin = projection.getTin();
            if (StringUtils.hasText(tin)) {
                byTin.putIfAbsent(tin.trim().toUpperCase(Locale.ROOT), item);
            }

            String uzcadRegistryCode = projection.getUzcadRegistryCode();
            if (StringUtils.hasText(uzcadRegistryCode)) {
                byUzcadRegistryCode.putIfAbsent(uzcadRegistryCode.trim().toUpperCase(Locale.ROOT), item);
            }
        }

        return new GeoReferenceLookupTable(
                Map.copyOf(byCode), Map.copyOf(bySoatoId), Map.copyOf(byTin), Map.copyOf(byUzcadRegistryCode));
    }

    private ReferenceItem toItem(
            ReferenceItemProjection projection, Integer soatoId, String tin, String uzcadRegistryCode
    ) {
        return new ReferenceItem(
                projection.getCode(),
                projection.getParentCode(),
                projection.getNameUz(),
                projection.getNameUzCyril(),
                projection.getNameRu(),
                projection.getNameKaa(),
                soatoId,
                tin,
                uzcadRegistryCode
        );
    }
}
