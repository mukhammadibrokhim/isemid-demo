package uz.uzinfocom.app.modules.reference.application.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.reference.domain.Population;
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationGeoType;
import uz.uzinfocom.app.modules.reference.repository.PopulationRepository;

/**
 * Read-only per-territory permanent-population lookup over {@code
 * ref_population} ({@link Population}), for statistical reports that show a
 * per-capita (intensive) indicator. Keyed by the same geography codes the
 * reports drill through — the republic root code {@code "UZ"}, a {@code
 * ref_region} code, or a {@code ref_district} code — plus a calendar year.
 * <p>
 * A missing figure resolves to {@code 0}; callers treat a zero denominator as
 * "no rate" rather than dividing by it. Organization-level report nodes have
 * no population of their own — callers pass the organization's parent
 * district code instead.
 */
@Service
@RequiredArgsConstructor
public class PopulationLookupService {

    /** Republic root node code used by {@code report.shared.ReportHierarchyService}. */
    private static final String REPUBLIC_CODE = "UZ";

    private final PopulationRepository populationRepository;

    /**
     * Population for the geography node identified by {@code code} in {@code
     * year}: {@code "UZ"} → the republic figure, otherwise a region figure by
     * region code, falling back to a district figure by district code (the
     * two code spaces are disjoint). Returns {@code 0} when nothing matches.
     */
    public long resolveByNodeCode(String code, int year) {
        if (!StringUtils.hasText(code)) {
            return 0L;
        }
        if (REPUBLIC_CODE.equalsIgnoreCase(code)) {
            return populationRepository
                    .findFirstByYearAndGeoTypeAndDeletedFalse(year, PopulationGeoType.REPUBLIC)
                    .map(Population::getPopulation)
                    .orElse(0L);
        }
        return populationRepository
                .findFirstByRegionCodeAndYearAndGeoTypeAndDeletedFalse(code, year, PopulationGeoType.REGION)
                .or(() -> populationRepository
                        .findFirstByDistrictCodeAndYearAndGeoTypeAndDeletedFalse(code, year, PopulationGeoType.DISTRICT))
                .map(Population::getPopulation)
                .orElse(0L);
    }
}
