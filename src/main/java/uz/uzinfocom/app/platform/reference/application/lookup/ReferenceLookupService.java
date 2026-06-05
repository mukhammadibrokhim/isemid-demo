package uz.uzinfocom.app.platform.reference.application.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.reference.application.lookup.dto.ReferenceItem;
import uz.uzinfocom.app.platform.reference.domain.enums.CatalogType;

import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReferenceLookupService {

    private final ReferenceCacheLoader cacheLoader;
    private final ReferenceNameResolver nameResolver;

    public ReferenceItem findCountry(String code) {
        return find(cacheLoader.loadCountries(), code);
    }

    public ReferenceItem findRegion(String code) {
        return find(cacheLoader.loadRegions(), code);
    }

    public ReferenceItem findDistrict(String code) {
        return find(cacheLoader.loadDistricts(), code);
    }

    public ReferenceItem findNeighborhood(String code) {
        return find(cacheLoader.loadNeighborhoods(), code);
    }

    public ReferenceItem findCatalog(CatalogType type, String code) {
        if (type == null) {
            return null;
        }
        return find(cacheLoader.loadCatalog(type), code);
    }

    public String getCountryName(String code) {
        return nameOrCode(code, findCountry(code));
    }

    public String getRegionName(String code) {
        return nameOrCode(code, findRegion(code));
    }

    public String getDistrictName(String code) {
        return nameOrCode(code, findDistrict(code));
    }

    public String getNeighborhoodName(String code) {
        return nameOrCode(code, findNeighborhood(code));
    }

    public String getCatalogName(CatalogType type, String code) {
        return nameOrCode(code, findCatalog(type, code));
    }

    private ReferenceItem find(Map<String, ReferenceItem> items, String code) {
        String normalizedCode = normalize(code);
        if (normalizedCode == null) {
            return null;
        }
        return items.get(normalizedCode);
    }

    private String nameOrCode(String code, ReferenceItem item) {
        String normalizedCode = normalize(code);
        if (normalizedCode == null) {
            return null;
        }

        String name = nameResolver.resolve(item);
        return StringUtils.hasText(name) ? name : normalizedCode;
    }

    private String normalize(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
