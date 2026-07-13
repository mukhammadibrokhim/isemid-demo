package uz.uzinfocom.isemid.features.report.service.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.isemid.features.classifier.repositories.Mkb10Repository;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.report.dtos.MonitoringResponse;
import uz.uzinfocom.isemid.features.report.dtos.request.MonitoringFilter;
import uz.uzinfocom.isemid.features.report.mapper.MonitoringResponseMapper;
import uz.uzinfocom.isemid.features.report.projection.MonitoringDiseaseRow;
import uz.uzinfocom.isemid.features.report.projection.MonitoringLocationRow;
import uz.uzinfocom.isemid.features.report.repository.MonitoringQueryRepository;
import uz.uzinfocom.isemid.features.report.service.MonitoringService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {

    private final Form058Repository form058Repository;
    private final Mkb10Repository mkb10Repository;
    private final CatalogService catalogService;
    private final MonitoringQueryRepository monitoringQueryRepository;
    private final MonitoringResponseMapper monitoringResponseMapper;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    @Transactional(
            readOnly = true,
            timeout = 60
    )
    public MonitoringResponse getMonitoring(
            LocalDate fromDate,
            LocalDate toDate,
            String regionCode,
            String mkb10Code
    ) {
        validateDates(fromDate, toDate);

        MonitoringFilter filter = new MonitoringFilter(
                fromDate,
                toDate,
                normalizeRegionCode(regionCode),
                normalizeMkb10Code(mkb10Code)
        );

        List<MonitoringLocationRow> locationRows =
                monitoringQueryRepository.findLocationStatistics(filter);

        List<MonitoringDiseaseRow> diseaseRows =
                monitoringQueryRepository.findDiseaseStatistics(filter);

        MonitoringResponse response = monitoringResponseMapper.toResponse(
                toDate,
                locationRows,
                diseaseRows
        );

        log.info(
                "Monitoring report generated: fromDate={}, toDate={}, "
                        + "regionCode={}, mkb10Code={}, locations={}, diseaseClasses={}",
                filter.fromDate(),
                filter.toDate(),
                filter.regionCode(),
                filter.mkb10Code(),
                locationRows.size(),
                diseaseRows.size()
        );

        return response;
    }

    private void validateDates(
            LocalDate fromDate,
            LocalDate toDate
    ) {
        if (fromDate == null) {
            throw new IllegalArgumentException(
                    "fromDate is required"
            );
        }

        if (toDate == null) {
            throw new IllegalArgumentException(
                    "toDate is required"
            );
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "fromDate cannot be after toDate"
            );
        }
    }

    private String normalizeRegionCode(String value) {
        return hasText(value)
                ? value.trim()
                : null;
    }

    private String normalizeMkb10Code(String value) {
        return hasText(value)
                ? value.trim().toUpperCase(Locale.ROOT)
                : null;
    }
}