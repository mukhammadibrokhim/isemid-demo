package uz.uzinfocom.app.modules.reference.application.population.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.reference.application.population.sync.dto.PopulationSyncResult;
import uz.uzinfocom.app.modules.reference.application.population.sync.dto.SdmxPopulationDataset;
import uz.uzinfocom.app.modules.reference.application.population.sync.dto.SdmxTerritoryRow;
import uz.uzinfocom.app.modules.reference.domain.District;
import uz.uzinfocom.app.modules.reference.domain.Population;
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationGeoType;
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationSource;
import uz.uzinfocom.app.modules.reference.repository.DistrictRepository;
import uz.uzinfocom.app.modules.reference.repository.PopulationRepository;
import uz.uzinfocom.app.modules.reference.repository.RegionRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Upserts the {@code ref_population} table from the stat.uz SDMX feed. One
 * row per {@code (soatoId, year)} for years {@code >= minYear}. Existing
 * {@link PopulationSource#SDMX} rows are overwritten; {@link
 * PopulationSource#MANUAL} rows (admin corrections) are left alone. Runs in a
 * single transaction — a fetch/parse failure touches nothing.
 * <p>
 * When stat.uz publishes a new calendar year it simply appears as another
 * year key in the feed, so a later sync inserts those rows; revised figures
 * for an already-imported year update the matching rows in place.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PopulationSyncService {

    private static final int REPUBLIC_SOATO_ID = 1700;
    private static final double THOUSANDS_TO_PEOPLE = 1000d;

    private final PopulationSdmxClient populationSdmxClient;
    private final PopulationRepository populationRepository;
    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;
    private final PopulationSyncProperties properties;

    @Transactional
    public PopulationSyncResult sync() {
        SdmxPopulationDataset dataset = populationSdmxClient.fetch();
        int minYear = properties.getMinYear();

        Map<Integer, String> regionCodeBySoato = regionRepository.findAllByDeletedFalseOrderByNameUzAsc().stream()
                .collect(Collectors.toMap(
                        region -> region.getSoatoId(), region -> region.getCode(), (a, b) -> a));

        Map<Integer, District> districtBySoato = districtRepository.findAllByDeletedFalseOrderByNameUzAsc().stream()
                .collect(Collectors.toMap(District::getSoatoId, Function.identity(), (a, b) -> a));

        Map<String, Population> existingByKey = populationRepository.findAll().stream()
                .collect(Collectors.toMap(
                        p -> key(p.getSoatoId(), p.getYear()), Function.identity(), (a, b) -> a, HashMap::new));

        int processed = 0;
        int inserted = 0;
        int updated = 0;
        int skippedManual = 0;
        Set<Integer> unmatched = new HashSet<>();
        List<Population> toSave = new ArrayList<>();

        for (SdmxTerritoryRow row : dataset.rows()) {
            Integer soatoId = parseSoatoId(row.code());
            if (soatoId == null) {
                continue;
            }

            Classification classification = classify(soatoId, regionCodeBySoato, districtBySoato);
            if (classification.geoType() == PopulationGeoType.OTHER) {
                unmatched.add(soatoId);
            }

            for (Map.Entry<Integer, Double> yearly : row.yearlyValues().entrySet()) {
                int year = yearly.getKey();
                Double value = yearly.getValue();
                if (year < minYear || value == null) {
                    continue;
                }
                processed++;

                long population = Math.round(value * THOUSANDS_TO_PEOPLE);
                Population entity = existingByKey.get(key(soatoId, year));

                if (entity != null && entity.getSource() == PopulationSource.MANUAL) {
                    skippedManual++;
                    continue;
                }

                if (entity == null) {
                    entity = Population.builder()
                            .soatoId(soatoId)
                            .year(year)
                            .build();
                    inserted++;
                } else {
                    updated++;
                }

                entity.setGeoType(classification.geoType());
                entity.setRegionCode(classification.regionCode());
                entity.setDistrictCode(classification.districtCode());
                entity.setPopulation(population);
                entity.setSource(PopulationSource.SDMX);
                entity.setDeleted(false);

                toSave.add(entity);
            }
        }

        populationRepository.saveAll(toSave);

        PopulationSyncResult result = new PopulationSyncResult(
                processed, inserted, updated, skippedManual, unmatched.size(), minYear,
                dataset.lastModified().orElse(null)
        );
        log.info("Population SDMX sync done: {}", result);
        return result;
    }

    private Classification classify(
            int soatoId,
            Map<Integer, String> regionCodeBySoato,
            Map<Integer, District> districtBySoato
    ) {
        if (soatoId == REPUBLIC_SOATO_ID) {
            return new Classification(PopulationGeoType.REPUBLIC, null, null);
        }
        String regionCode = regionCodeBySoato.get(soatoId);
        if (regionCode != null) {
            return new Classification(PopulationGeoType.REGION, regionCode, null);
        }
        District district = districtBySoato.get(soatoId);
        if (district != null) {
            return new Classification(PopulationGeoType.DISTRICT, district.getParentCode(), district.getCode());
        }
        return new Classification(PopulationGeoType.OTHER, null, null);
    }

    private static Integer parseSoatoId(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(code.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String key(Integer soatoId, Integer year) {
        return soatoId + ":" + year;
    }

    private record Classification(PopulationGeoType geoType, String regionCode, String districtCode) {
    }
}
