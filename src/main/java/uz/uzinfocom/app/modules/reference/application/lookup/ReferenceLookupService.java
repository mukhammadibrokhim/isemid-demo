package uz.uzinfocom.app.modules.reference.application.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.GeoReferenceLookupTable;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.ReferenceItem;

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

    /**
     * Resolves a region by its {@code code} (e.g. "UZ-AN") or, if the value is purely numeric,
     * by its SOATO id (e.g. "1703") instead — whichever an upstream caller happens to send.
     */
    public ReferenceItem findRegion(String codeOrSoatoId) {
        return findGeo(cacheLoader.loadRegions(), codeOrSoatoId);
    }

    public ReferenceItem findDistrict(String codeOrSoatoId) {
        return findGeo(cacheLoader.loadDistricts(), codeOrSoatoId);
    }

    /**
     * Resolves a neighborhood by its {@code code}, its numeric SOATO id, its {@code tin}, or its
     * {@code uzcadRegistryCode} — whichever an upstream caller happens to store/send as the
     * neighborhood reference value (the last one is what api2's v3/citizenAddress sends as a
     * Maxalla's {@code Guid} — see CitizenAddressMapper).
     */
    public ReferenceItem findNeighborhood(String codeOrSoatoIdOrTinOrUzcadRegistryCode) {
        return findGeo(cacheLoader.loadNeighborhoods(), codeOrSoatoIdOrTinOrUzcadRegistryCode);
    }

    public ReferenceItem findCatalog(String type, String code) {
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

    /**
     * Resolves the region name for a neighborhood via its district, since Neighborhood only
     * stores its district code (parentCode) and has no direct region code of its own.
     */
    public String getRegionNameByDistrictCode(String districtCode) {
        ReferenceItem district = findDistrict(districtCode);
        if (district == null) {
            return nameOrCode(districtCode, null);
        }
        return getRegionName(district.parentCode());
    }

    public String getNeighborhoodName(String code) {
        return nameOrCode(code, findNeighborhood(code));
    }

    public String getCatalogName(String type, String code) {
        return nameOrCode(code, findCatalog(type, code));
    }

    private ReferenceItem find(Map<String, ReferenceItem> items, String code) {
        String normalizedCode = normalize(code);
        if (normalizedCode == null) {
            return null;
        }
        return items.get(normalizedCode);
    }

    private ReferenceItem findGeo(GeoReferenceLookupTable table, String codeOrSoatoIdOrTinOrUzcadRegistryCode) {
        String normalized = normalize(codeOrSoatoIdOrTinOrUzcadRegistryCode);
        if (normalized == null) {
            return null;
        }

        ReferenceItem byCode = table.byCode().get(normalized);
        if (byCode != null) {
            return byCode;
        }

        if (isDigits(normalized)) {
            ReferenceItem bySoatoId = table.bySoatoId().get(normalized);
            if (bySoatoId != null) {
                return bySoatoId;
            }
        }

        ReferenceItem byTin = table.byTin().get(normalized);
        if (byTin != null) {
            return byTin;
        }

        return table.byUzcadRegistryCode().get(normalized);
    }

    private boolean isDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
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
