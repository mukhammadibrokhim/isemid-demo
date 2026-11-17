package uz.uzinfocom.app.modules.airquality.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.airquality.application.AirParameter;
import uz.uzinfocom.app.modules.airquality.application.AirQualityNorms;
import uz.uzinfocom.app.modules.airquality.application.AirStatus;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirLatestResponse;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirReadingResponse;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirReadingStatusResponse;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirStationResponse;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirStationStatusResponse;
import uz.uzinfocom.app.modules.airquality.domain.AirMeasurement;
import uz.uzinfocom.app.modules.airquality.domain.AirStation;
import uz.uzinfocom.app.modules.airquality.repository.AirMeasurementRepository;
import uz.uzinfocom.app.modules.airquality.repository.AirStationRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Read side of the stored AIR_CONTROL data. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirQualityQueryService {

    /** Hard cap on one series answer; 10 pollutants x hourly is ~240 rows a day, so ~20 days fit. */
    public static final int MAX_SERIES_ROWS = 5000;
    private static final int DEFAULT_SERIES_ROWS = 1000;

    private final AirStationRepository stationRepository;
    private final AirMeasurementRepository measurementRepository;
    private final AirQualityNorms norms;

    public List<AirStationResponse> stations(String regionCode, String districtCode, boolean activeOnly) {
        return stationRepository.findAllByOrderByNameAscIdAsc().stream()
                .filter(station -> !StringUtils.hasText(regionCode) || regionCode.equals(station.getRegionCode()))
                .filter(station -> !StringUtils.hasText(districtCode) || districtCode.equals(station.getDistrictCode()))
                .filter(station -> !activeOnly || Boolean.TRUE.equals(station.getActive()))
                .map(AirQualityQueryService::toResponse)
                .toList();
    }

    /**
     * Stations with their latest measurement judged against the norms, worst first (then by
     * name) - the shape a map and a dashboard need. Two queries however many stations there are.
     */
    public List<AirStationStatusResponse> stationStatuses(String regionCode, String districtCode, boolean activeOnly) {
        List<AirStationResponse> stations = stations(regionCode, districtCode, activeOnly);
        if (stations.isEmpty()) {
            return List.of();
        }
        Map<Long, List<AirMeasurement>> latestByStation = measurementRepository
                .findLatestOfStations(stations.stream().map(AirStationResponse::id).toList()).stream()
                .collect(Collectors.groupingBy(AirMeasurement::getStationId));

        return stations.stream()
                .map(station -> toStatus(station, latestByStation.getOrDefault(station.id(), List.of())))
                .sorted(Comparator.comparingInt((AirStationStatusResponse s) -> s.status().severity()).reversed()
                        .thenComparing(s -> s.station().name(), Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private AirStationStatusResponse toStatus(AirStationResponse station, List<AirMeasurement> latest) {
        List<AirReadingStatusResponse> readings = latest.stream()
                .map(measurement -> {
                    Optional<BigDecimal> ratio =
                            norms.ratio(measurement.getParameter(), measurement.getValue(), measurement.getUnit());
                    return new AirReadingStatusResponse(
                            measurement.getParameter(), measurement.getValue(), measurement.getUnit(),
                            norms.limit(measurement.getParameter()).orElse(null),
                            ratio.orElse(null), AirStatus.fromRatio(ratio.orElse(null)));
                })
                .toList();

        AirReadingStatusResponse worst = readings.stream()
                .filter(reading -> reading.ratio() != null)
                .max(Comparator.comparing(AirReadingStatusResponse::ratio))
                .orElse(null);
        return new AirStationStatusResponse(
                station,
                latest.isEmpty() ? null : latest.get(0).getMeasuredAt(),
                worst == null ? AirStatus.NO_DATA : worst.status(),
                worst == null ? null : worst.parameter(),
                worst == null ? null : worst.ratio(),
                readings);
    }

    public AirLatestResponse latest(Long stationId) {
        AirStation station = requireStation(stationId);
        LocalDateTime measuredAt = station.getLastMeasuredAt();
        List<AirReadingResponse> readings = measuredAt == null
                ? List.of()
                : measurementRepository.findAllByStationIdAndMeasuredAtOrderByParameterAsc(stationId, measuredAt).stream()
                        .map(AirQualityQueryService::toReading)
                        .toList();
        return new AirLatestResponse(toResponse(station), measuredAt, readings);
    }

    /** Newest first. {@code parameter} accepts any spelling ({@code "PM 2.5"}, {@code "pm2.5"}). */
    public List<AirReadingResponse> series(
            Long stationId, LocalDateTime from, LocalDateTime to, String parameter, Integer limit) {
        requireStation(stationId);
        int size = limit == null ? DEFAULT_SERIES_ROWS : Math.min(Math.max(limit, 1), MAX_SERIES_ROWS);
        PageRequest page = PageRequest.of(0, size);
        String code = AirParameter.normalize(parameter);

        List<AirMeasurement> rows = code == null
                ? measurementRepository.findSeries(stationId, from, to, page)
                : measurementRepository.findSeriesByParameter(stationId, code, from, to, page);
        return rows.stream().map(AirQualityQueryService::toReading).toList();
    }

    private AirStation requireStation(Long stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new NotFoundException("air-quality.station.not_found"));
    }

    private static AirStationResponse toResponse(AirStation station) {
        return new AirStationResponse(
                station.getId(), station.getExternalId(), station.getName(),
                station.getLatitude(), station.getLongitude(), station.getSoatoId(),
                station.getRegionCode(), station.getDistrictCode(),
                Boolean.TRUE.equals(station.getActive()), station.getLastMeasuredAt());
    }

    private static AirReadingResponse toReading(AirMeasurement measurement) {
        return new AirReadingResponse(
                measurement.getParameter(), measurement.getValue(), measurement.getUnit(), measurement.getMeasuredAt());
    }
}
