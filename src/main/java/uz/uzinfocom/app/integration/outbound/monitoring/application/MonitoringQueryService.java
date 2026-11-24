package uz.uzinfocom.app.integration.outbound.monitoring.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.integration.outbound.monitoring.infrastructure.MonitoringQueryRepository;
import uz.uzinfocom.app.integration.outbound.monitoring.web.dto.MonitoringDynamicsResponse;
import uz.uzinfocom.app.integration.outbound.monitoring.web.dto.MonitoringResponse;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringQueryService {

    /** Upper bound on {@code [from, to]}, inclusive of both ends - keeps one call from scanning years of cases. */
    static final long MAX_RANGE_DAYS = 366;

    private final MonitoringQueryRepository monitoringQueryRepository;
    private final MonitoringResponseMapper monitoringResponseMapper;
    private final MonitoringDynamicsResponseMapper monitoringDynamicsResponseMapper;

    @Transactional(readOnly = true, timeout = 60)
    public MonitoringResponse getMonitoring(LocalDate from, LocalDate to, String regionCode, String diagnosisCode) {
        validateDates(from, to);

        MonitoringFilter filter = new MonitoringFilter(
                from, to, normalizeRegionCode(regionCode), normalizeDiagnosisCode(diagnosisCode));

        List<MonitoringLocationRow> locationRows = monitoringQueryRepository.findLocationStatistics(filter);
        List<MonitoringDiseaseRow> diseaseRows = monitoringQueryRepository.findDiseaseStatistics(filter);

        locationRows.stream()
                .filter(row -> !row.hasConsistentTotals())
                .forEach(row -> log.warn("Monitoring totals do not add up: location={}, row={}",
                        row.locationCode(), row));

        log.info("Monitoring report generated: from={}, to={}, regionCode={}, diagnosisCode={}, "
                        + "locations={}, diseaseRows={}",
                filter.from(), filter.to(), filter.regionCode(), filter.diagnosisCode(),
                locationRows.size(), diseaseRows.size());

        return monitoringResponseMapper.toResponse(filter, locationRows, diseaseRows);
    }

    @Transactional(readOnly = true, timeout = 60)
    public MonitoringDynamicsResponse getDynamics(
            LocalDate from, LocalDate to, String regionCode, String districtCode, String diagnosisCode
    ) {
        validateDates(from, to);

        MonitoringDynamicsQuery query = new MonitoringDynamicsQuery(
                MonitoringDynamicsWindow.of(from, to, ZonedDateTime.now(MonitoringDynamicsWindow.ZONE)),
                normalizeRegionCode(regionCode),
                normalizeRegionCode(districtCode),
                normalizeDiagnosisCode(diagnosisCode)
        );

        List<MonitoringDynamicsRow> rows = monitoringQueryRepository.findDynamics(query);

        log.info("Monitoring dynamics generated: from={}, to={}, regionCode={}, districtCode={}, "
                        + "diagnosisCode={}, granularity={}, rows={}",
                from, to, query.regionCode(), query.districtCode(), query.diagnosisCode(),
                query.window().granularity(), rows.size());

        return monitoringDynamicsResponseMapper.toResponse(to, rows);
    }

    private void validateDates(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from cannot be after to");
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException("date range cannot exceed " + MAX_RANGE_DAYS + " days");
        }
    }

    /** Region and district codes are upper case ({@code UZ-AN}, {@code AN-202}); {@code uz-an} filters alike. */
    private String normalizeRegionCode(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    /** {@code " (a00-a09) "} and {@code "A00-A09"} filter alike, matching the SQL-side normalization. */
    private String normalizeDiagnosisCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String code = value.trim();
        while (code.startsWith("(")) {
            code = code.substring(1).trim();
        }
        while (code.endsWith(")")) {
            code = code.substring(0, code.length() - 1).trim();
        }
        return code.isEmpty() ? null : code.toUpperCase(Locale.ROOT);
    }
}
