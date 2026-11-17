package uz.uzinfocom.app.modules.report.airhealth.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.airquality.application.AirParameter;
import uz.uzinfocom.app.modules.airquality.application.AirQualityNorms;
import uz.uzinfocom.app.modules.airquality.application.AirStatus;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirBucketStat;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirTerritoryStat;
import uz.uzinfocom.app.modules.airquality.repository.AirAggregateRepository;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.lookup.PopulationLookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.report.airhealth.application.query.dto.AirHealthBucket;
import uz.uzinfocom.app.modules.report.airhealth.application.query.dto.AirHealthSeriesResponse;
import uz.uzinfocom.app.modules.report.airhealth.application.query.dto.AirHealthTerritoriesResponse;
import uz.uzinfocom.app.modules.report.airhealth.application.query.dto.AirHealthTerritoryResponse;
import uz.uzinfocom.app.modules.report.airhealth.infrastructure.persistence.repository.AirHealthCaseRepository;
import uz.uzinfocom.app.modules.report.airhealth.infrastructure.persistence.repository.AirHealthCaseRepository.BucketCases;
import uz.uzinfocom.app.modules.report.airhealth.infrastructure.persistence.repository.AirHealthCaseRepository.TerritoryCases;
import uz.uzinfocom.app.modules.report.shared.PearsonCorrelation;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.report.shared.ResolvedReportNode;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ScopeGeoFilter;
import uz.uzinfocom.app.orchestration.scope.ScopeGeoFilterResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * "Air and respiratory diseases": AIR_CONTROL pollutant levels set against confirmed respiratory
 * cases, in two views - per territory (a table) and over time for one territory (a series with
 * the correlation between the two).
 * <p>
 * The cases side follows the project's confirmed-count rule: {@code form058} + {@code form058_1},
 * {@code status = 'APPROVED'}, matched on {@code final_icd10_code} only, and is scoped through
 * {@link ReportHierarchyService#resolveNode} exactly like every other report (an out-of-scope
 * {@code regionCode}/{@code districtCode} is rejected). The air side is keyed by the stations'
 * territory codes, not by organization, so it follows the same explicit-or-caller-scope territory.
 * The default diagnosis prefix {@code J} is ICD-10 chapter X, "diseases of the respiratory system".
 */
@Service
@RequiredArgsConstructor
public class AirHealthReportQueryService {

    public static final String DEFAULT_PARAMETER = "PM2.5";
    public static final String DEFAULT_DIAGNOSIS_PREFIX = "J";
    public static final int DEFAULT_KOEF = 100_000;
    public static final int MAX_LAG_DAYS = 30;
    public static final int MAX_SERIES_BUCKETS = 800;

    private static final int DEFAULT_SERIES_DAYS = 90;
    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");
    /** Stands in for "no norm" so the exceedance comparison matches nothing. */
    private static final BigDecimal NO_NORM = BigDecimal.valueOf(Long.MAX_VALUE);

    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final AirAggregateRepository airAggregateRepository;
    private final AirHealthCaseRepository caseRepository;
    private final AirQualityNorms norms;
    private final ReferenceLookupService referenceLookupService;
    private final PopulationLookupService populationLookupService;

    public AirHealthTerritoriesResponse getTerritories(
            String regionCode, String districtCode, String parameterOrNull, String diagnosisPrefixOrNull,
            LocalDate from, LocalDate to, Integer koefOrNull
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        ResolvedReportNode node = reportHierarchyService.resolveNode(currentOrganization, regionCode, districtCode);
        ScopeGeoFilter geo = geoFilter(currentOrganization, regionCode, districtCode);
        // One level down from wherever the caller is looking; a republic-wide caller sees regions.
        boolean byDistrict = geo.regionCode() != null || geo.districtCode() != null;

        String parameter = parameter(parameterOrNull);
        String prefix = diagnosisPrefix(diagnosisPrefixOrNull);
        int koef = koefOrNull != null && koefOrNull > 0 ? koefOrNull : DEFAULT_KOEF;
        ReportDateRange range = reportDateRangeResolver.resolve(from, to);
        Optional<BigDecimal> norm = norms.limit(parameter);
        int populationYear = (to != null ? to : LocalDate.now(APPLICATION_ZONE)).getYear();

        Map<String, AirTerritoryStat> air = new HashMap<>();
        airAggregateRepository.territoryStats(
                byDistrict, geo.regionCode(), geo.districtCode(), parameter,
                local(range.fromInclusive()), local(range.toExclusive()), norm.orElse(NO_NORM)
        ).forEach(stat -> air.put(stat.territoryCode(), stat));

        Map<String, Long> cases = new HashMap<>();
        for (TerritoryCases row : caseRepository.countByTerritory(
                node.organizationIds(), byDistrict, prefix, range.fromInclusive(), range.toExclusive())) {
            cases.merge(row.territoryCode(), row.cases(), Long::sum);
        }

        Set<String> codes = new LinkedHashSet<>(cases.keySet());
        codes.addAll(air.keySet());
        List<AirHealthTerritoryResponse> rows = new ArrayList<>(codes.size());
        for (String code : codes) {
            String name = byDistrict ? referenceLookupService.getDistrictName(code) : referenceLookupService.getRegionName(code);
            long population = populationLookupService.resolveByNodeCode(code, populationYear);
            rows.add(row(code, name, air.get(code), cases.getOrDefault(code, 0L), population, koef, norm));
        }
        rows.sort(Comparator.comparingLong(AirHealthTerritoryResponse::confirmedCases).reversed()
                .thenComparing(AirHealthTerritoryResponse::name, Comparator.nullsLast(String::compareToIgnoreCase)));

        return new AirHealthTerritoriesResponse(
                parameter, AirQualityNorms.UNIT, norm.orElse(null), prefix, koef, from, to,
                rows, total(node, rows, koef, norm));
    }

    public AirHealthSeriesResponse getSeries(
            String regionCode, String districtCode, String parameterOrNull, String diagnosisPrefixOrNull,
            AirHealthBucket bucketOrNull, Integer lagDaysOrNull, LocalDate from, LocalDate to
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        ResolvedReportNode node = reportHierarchyService.resolveNode(currentOrganization, regionCode, districtCode);
        ScopeGeoFilter geo = geoFilter(currentOrganization, regionCode, districtCode);

        String parameter = parameter(parameterOrNull);
        String prefix = diagnosisPrefix(diagnosisPrefixOrNull);
        AirHealthBucket bucket = bucketOrNull != null ? bucketOrNull : AirHealthBucket.DAY;
        int lag = lagDaysOrNull == null ? 0 : Math.max(0, Math.min(lagDaysOrNull, MAX_LAG_DAYS));
        LocalDate end = to != null ? to : LocalDate.now(APPLICATION_ZONE);
        LocalDate start = from != null ? from : end.minusDays(DEFAULT_SERIES_DAYS - 1L);
        if (start.isAfter(end)) {
            throw AirHealthRequestException.invalidRange();
        }

        List<LocalDate> starts = bucketStarts(bucket, start, end);
        Optional<BigDecimal> norm = norms.limit(parameter);

        Map<LocalDate, AirBucketStat> air = new HashMap<>();
        airAggregateRepository.bucketSeries(
                geo.regionCode(), geo.districtCode(), parameter, bucket.sqlTruncField(),
                start.atStartOfDay(), end.plusDays(1).atStartOfDay()
        ).forEach(stat -> air.put(stat.bucketStart(), stat));

        Map<LocalDate, Long> cases = new HashMap<>();
        for (BucketCases row : caseRepository.countByBucket(
                node.organizationIds(), bucket, lag, prefix,
                instant(start.plusDays(lag)), instant(end.plusDays(1L + lag)))) {
            cases.merge(row.bucketStart(), row.cases(), Long::sum);
        }

        List<AirHealthSeriesResponse.Point> points = new ArrayList<>(starts.size());
        List<Double> airValues = new ArrayList<>();
        List<Double> caseValues = new ArrayList<>();
        for (LocalDate bucketStart : starts) {
            AirBucketStat stat = air.get(bucketStart);
            long count = cases.getOrDefault(bucketStart, 0L);
            boolean above = stat != null && norm.isPresent() && stat.average() > norm.get().doubleValue();
            points.add(new AirHealthSeriesResponse.Point(
                    bucketStart,
                    stat == null ? null : round2(stat.average()),
                    stat == null ? null : round2(stat.maximum()),
                    stat == null ? 0 : stat.readings(),
                    above, count));
            if (stat != null) {
                airValues.add(stat.average());
                caseValues.add((double) count);
            }
        }

        PearsonCorrelation.Result result = PearsonCorrelation.compute(toArray(airValues), toArray(caseValues));
        AirHealthSeriesResponse.Correlation correlation = new AirHealthSeriesResponse.Correlation(
                result.coefficient() == null ? null : round4(result.coefficient()),
                result.pairs(), result.strength(), result.reliable());

        return new AirHealthSeriesResponse(
                node.code(), node.name(), parameter, AirQualityNorms.UNIT, norm.orElse(null), prefix,
                bucket, lag, start, end, points, correlation);
    }

    private AirHealthTerritoryResponse row(
            String code, String name, AirTerritoryStat air, long cases, long population, int koef, Optional<BigDecimal> norm
    ) {
        Double average = air == null ? null : round2(air.average());
        Double maximum = air == null ? null : round2(air.maximum());
        return new AirHealthTerritoryResponse(
                code, name,
                air == null ? 0 : air.stationCount(),
                air == null ? 0 : air.readings(),
                average, maximum,
                air == null ? 0 : air.exceedanceDays(),
                air == null ? 0 : air.daysWithData(),
                status(air == null ? null : air.average(), norm),
                cases, population, rate(cases, population, koef));
    }

    /**
     * The "Jami" row: cases and population are sums, the level is the readings-weighted average,
     * the maximum is the largest, and the day counts are the largest of any territory (summing days
     * across territories would count the same calendar day many times). The rate is left out when any
     * territory with cases has no population figure, since it would be inflated.
     */
    private AirHealthTerritoryResponse total(
            ResolvedReportNode node, List<AirHealthTerritoryResponse> rows, int koef, Optional<BigDecimal> norm
    ) {
        long stations = 0;
        long readings = 0;
        double weighted = 0;
        Double maximum = null;
        long exceedance = 0;
        long daysWithData = 0;
        long cases = 0;
        long population = 0;
        boolean populationComplete = true;
        for (AirHealthTerritoryResponse row : rows) {
            stations += row.stationCount();
            readings += row.readingCount();
            if (row.average() != null) {
                weighted += row.average() * row.readingCount();
            }
            if (row.maximum() != null) {
                maximum = maximum == null ? row.maximum() : Math.max(maximum, row.maximum());
            }
            exceedance = Math.max(exceedance, row.exceedanceDays());
            daysWithData = Math.max(daysWithData, row.daysWithData());
            cases += row.confirmedCases();
            population += row.population();
            if (row.confirmedCases() > 0 && row.population() <= 0) {
                populationComplete = false;
            }
        }
        Double average = readings == 0 ? null : round2(weighted / readings);
        return new AirHealthTerritoryResponse(
                node.code(), node.name(), stations, readings, average, maximum, exceedance, daysWithData,
                status(average, norm), cases, population,
                populationComplete ? rate(cases, population, koef) : null);
    }

    private static AirStatus status(Double averageMicrograms, Optional<BigDecimal> norm) {
        if (averageMicrograms == null || norm.isEmpty()) {
            return AirStatus.NO_DATA;
        }
        return AirStatus.fromRatio(BigDecimal.valueOf(averageMicrograms).divide(norm.get(), 4, RoundingMode.HALF_UP));
    }

    private static BigDecimal rate(long cases, long population, int koef) {
        if (population <= 0) {
            return null;
        }
        return BigDecimal.valueOf(cases).multiply(BigDecimal.valueOf(koef))
                .divide(BigDecimal.valueOf(population), 2, RoundingMode.HALF_UP);
    }

    private ScopeGeoFilter geoFilter(Organization currentOrganization, String regionCode, String districtCode) {
        if (StringUtils.hasText(districtCode)) {
            return new ScopeGeoFilter(null, districtCode.trim());
        }
        if (StringUtils.hasText(regionCode)) {
            return new ScopeGeoFilter(regionCode.trim(), null);
        }
        return ScopeGeoFilterResolver.of(organizationScopeResolver.resolve(currentOrganization));
    }

    private static List<LocalDate> bucketStarts(AirHealthBucket bucket, LocalDate start, LocalDate end) {
        List<LocalDate> starts = new ArrayList<>();
        for (LocalDate cursor = bucket.start(start); !cursor.isAfter(end); cursor = bucket.next(cursor)) {
            if (starts.size() >= MAX_SERIES_BUCKETS) {
                throw AirHealthRequestException.tooManyBuckets(MAX_SERIES_BUCKETS);
            }
            starts.add(cursor);
        }
        return starts;
    }

    private static String parameter(String value) {
        String code = AirParameter.normalize(value);
        return code != null ? code : DEFAULT_PARAMETER;
    }

    /** Upper-cased and stripped to the characters an ICD-10 prefix can hold, so it is safe inside a LIKE. */
    private static String diagnosisPrefix(String value) {
        if (!StringUtils.hasText(value)) {
            return DEFAULT_DIAGNOSIS_PREFIX;
        }
        String cleaned = value.trim().toUpperCase(java.util.Locale.ROOT).replaceAll("[^A-Z0-9.]", "");
        return cleaned.isEmpty() ? DEFAULT_DIAGNOSIS_PREFIX : cleaned;
    }

    private static LocalDateTime local(Instant instant) {
        return LocalDateTime.ofInstant(instant, APPLICATION_ZONE);
    }

    private static Instant instant(LocalDate date) {
        return date.atStartOfDay(APPLICATION_ZONE).toInstant();
    }

    private static double[] toArray(List<Double> values) {
        double[] array = new double[values.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = values.get(i);
        }
        return array;
    }

    private static double round2(double value) {
        return Math.round(value * 100d) / 100d;
    }

    private static double round4(double value) {
        return Math.round(value * 10_000d) / 10_000d;
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
