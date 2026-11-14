package uz.uzinfocom.app.modules.airquality.application.sync;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.integration.aircontrol.client.dto.AirControlMeasurement;
import uz.uzinfocom.app.integration.aircontrol.client.dto.AirControlReading;
import uz.uzinfocom.app.integration.aircontrol.client.dto.AirControlStation;
import uz.uzinfocom.app.modules.airquality.application.AirParameter;
import uz.uzinfocom.app.modules.airquality.domain.AirMeasurement;
import uz.uzinfocom.app.modules.airquality.domain.AirStation;
import uz.uzinfocom.app.modules.airquality.repository.AirMeasurementRepository;
import uz.uzinfocom.app.modules.airquality.repository.AirStationRepository;
import uz.uzinfocom.app.modules.reference.domain.District;
import uz.uzinfocom.app.modules.reference.domain.Region;
import uz.uzinfocom.app.modules.reference.repository.DistrictRepository;
import uz.uzinfocom.app.modules.reference.repository.RegionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The transactional half of the AIRCONTROL sync. HTTP calls happen in
 * {@link AirQualitySyncService} <em>outside</em> any transaction (a slow upstream must not
 * hold a database connection); this bean only writes what was already fetched, one
 * short transaction per call.
 */
@Component
@RequiredArgsConstructor
public class AirQualityWriter {

    /** A station that is active upstream, ready to have its latest measurement fetched. */
    public record StationRef(Long id, String externalId) {
    }

    public record StationUpsertResult(int created, int updated, List<StationRef> active) {
    }

    private final AirStationRepository stationRepository;
    private final AirMeasurementRepository measurementRepository;
    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;

    @Transactional
    public StationUpsertResult upsertStations(List<AirControlStation> received) {
        // The same id twice in one answer: the last one wins, instead of a duplicate-key insert.
        Map<String, AirControlStation> byId = new LinkedHashMap<>();
        received.forEach(station -> byId.put(station.id(), station));

        Map<String, AirStation> existing = stationRepository.findAllByExternalIdIn(byId.keySet()).stream()
                .collect(Collectors.toMap(AirStation::getExternalId, Function.identity()));
        Geo geo = new Geo(regionRepository.findAllByDeletedFalseOrderByNameUzAsc(),
                districtRepository.findAllByDeletedFalseOrderByNameUzAsc());

        int created = 0;
        int updated = 0;
        List<AirStation> toSave = new ArrayList<>();
        for (AirControlStation incoming : byId.values()) {
            AirStation station = existing.get(incoming.id());
            if (station == null) {
                station = AirStation.builder().externalId(incoming.id()).build();
                created++;
            } else {
                updated++;
            }
            apply(station, incoming, geo);
            toSave.add(station);
        }
        List<AirStation> saved = stationRepository.saveAll(toSave);

        List<StationRef> active = saved.stream()
                .filter(station -> Boolean.TRUE.equals(station.getActive()))
                .map(station -> new StationRef(station.getId(), station.getExternalId()))
                .toList();
        return new StationUpsertResult(created, updated, active);
    }

    /**
     * Stores one station's latest measurement unless that moment is already stored.
     *
     * @return the number of pollutant rows stored, {@code 0} when nothing new
     */
    @Transactional
    public int storeMeasurement(Long stationId, AirControlMeasurement measurement) {
        if (measurementRepository.existsByStationIdAndMeasuredAt(stationId, measurement.measuredAt())) {
            return 0;
        }
        // One row per pollutant; a pollutant repeated in one answer keeps its last value.
        Map<String, AirMeasurement> rows = new LinkedHashMap<>();
        for (AirControlReading reading : measurement.readings()) {
            String code = AirParameter.normalize(reading.name());
            if (code == null) {
                continue;
            }
            rows.put(code, AirMeasurement.builder()
                    .stationId(stationId)
                    .measuredAt(measurement.measuredAt())
                    .parameter(code)
                    .value(reading.value())
                    .unit(reading.unit())
                    .build());
        }
        if (rows.isEmpty()) {
            return 0;
        }
        measurementRepository.saveAll(rows.values());

        stationRepository.findById(stationId).ifPresent(station -> {
            LocalDateTime last = station.getLastMeasuredAt();
            if (last == null || measurement.measuredAt().isAfter(last)) {
                station.setLastMeasuredAt(measurement.measuredAt());
            }
            station.setLastSyncedAt(LocalDateTime.now());
        });
        return rows.size();
    }

    private static void apply(AirStation station, AirControlStation incoming, Geo geo) {
        station.setName(incoming.name());
        station.setLatitude(incoming.latitude());
        station.setLongitude(incoming.longitude());
        station.setActive(incoming.active());
        Integer soato = parseSoato(incoming.soatoAreaId());
        station.setSoatoId(soato);
        Geo.Match match = geo.resolve(soato);
        station.setRegionCode(match.regionCode());
        station.setDistrictCode(match.districtCode());
    }

    private static Integer parseSoato(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * SOATO → {@code ref_region}/{@code ref_district}. AIRCONTROL's {@code soato_areaid}
     * is district-level in the agreement's sample (7 digits); when no district matches,
     * the first four digits are tried as the region's SOATO. Anything else stays unresolved.
     */
    static final class Geo {

        record Match(String regionCode, String districtCode) {
            static final Match NONE = new Match(null, null);
        }

        private final Map<Integer, String> regionBySoato = new HashMap<>();
        private final Map<Integer, District> districtBySoato = new HashMap<>();

        Geo(List<Region> regions, List<District> districts) {
            regions.forEach(region -> regionBySoato.putIfAbsent(region.getSoatoId(), region.getCode()));
            districts.forEach(district -> districtBySoato.putIfAbsent(district.getSoatoId(), district));
        }

        Match resolve(Integer soato) {
            if (soato == null) {
                return Match.NONE;
            }
            District district = districtBySoato.get(soato);
            if (district != null) {
                return new Match(district.getParentCode(), district.getCode());
            }
            String region = regionBySoato.get(soato);
            if (region == null && soato >= 1_000_000) {
                region = regionBySoato.get(soato / 1000);
            }
            return region == null ? Match.NONE : new Match(region, null);
        }
    }
}
