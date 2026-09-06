package uz.uzinfocom.app.modules.report.forecast.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.lookup.Icd10LookupService;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastBucketUnit;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastDiseaseRiskResponse;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastHistoryPointResponse;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastMethod;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastNodeResponse;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastPredictionPointResponse;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastResponse;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastRiskLevel;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastSeries;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastSummaryResponse;
import uz.uzinfocom.app.modules.report.forecast.application.query.forecasting.EndemicChannel;
import uz.uzinfocom.app.modules.report.forecast.application.query.forecasting.TimeSeriesForecaster;
import uz.uzinfocom.app.modules.report.forecast.infrastructure.persistence.repository.ForecastSeriesRepository;
import uz.uzinfocom.app.modules.report.forecast.infrastructure.persistence.repository.dto.ForecastBucketCountProjection;
import uz.uzinfocom.app.modules.report.forecast.infrastructure.persistence.repository.dto.ForecastDiseaseBucketCountProjection;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyNode;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.report.shared.ResolvedReportNode;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * "Forecast" — surveillance forecasting over {@code form058} + {@code
 * form058_1}. Two shapes, the same drill-down convention every other report
 * uses:
 *
 * <ul>
 *   <li><b>Geography breakdown</b> ({@link #getRoot} / {@link #getChildren})
 *   — the caller's whole access scope broken down one hierarchy level at a
 *   time (regions → districts → organizations) plus a "Jami" row, each row a
 *   compact per-node forecast summary. Delegated to the shared {@link
 *   ReportHierarchyService} via {@link ForecastGeographyCountSource}, whose
 *   count aggregate {@code C} is a whole {@link ForecastSeries}. This is the
 *   first screen — no params → the whole republic broken down by region.</li>
 *   <li><b>Node series</b> ({@link #getSeries}) — the full history +
 *   horizon-ahead forecast + endemic channel for <b>one</b> node (the
 *   caller's scope, or an explicit {@code regionCode}/{@code districtCode}
 *   inside it), for the chart.</li>
 * </ul>
 *
 * <p>Filters are identical across both: geography, ICD-10 ({@code
 * diagnosisCode}, initial or final code), and the training window ({@code
 * from}/{@code to}; {@code from} omitted → a unit-appropriate look-back).
 * {@code bucket} / {@code horizon} / {@code method} shape the extrapolation.
 */
@Service
@RequiredArgsConstructor
public class ForecastReportQueryService {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");
    private static final int DEFAULT_HORIZON = 8;
    private static final int MIN_HORIZON = 1;
    private static final int MAX_TRAINING_BUCKETS = 520;
    private static final int DEFAULT_TOP_DISEASES = 10;
    private static final int MAX_TOP_DISEASES = 50;
    private static final long DEFAULT_MIN_TRAINING_CASES = 3;

    private final ForecastSeriesRepository forecastSeriesRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final Icd10LookupService icd10LookupService;

    public List<ForecastNodeResponse> getRoot(
            String diagnosisCode,
            ForecastBucketUnit bucketOrNull,
            Integer horizonOrNull,
            ForecastMethod methodOrNull,
            LocalDate from,
            LocalDate to
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        Request request = request(diagnosisCode, bucketOrNull, horizonOrNull, methodOrNull, from, to);

        ForecastGeographyCountSource countSource = new ForecastGeographyCountSource(
                forecastSeriesRepository, request.unit(), request.window().bucketStarts()
        );
        List<ReportHierarchyNode<ForecastSeries>> nodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, countSource, request.window().range(), request.diagnosisCode()
        );

        return nodes.stream().map(node -> nodeRow(node, request)).toList();
    }

    public List<ForecastNodeResponse> getChildren(
            String regionCode,
            String districtCode,
            String diagnosisCode,
            ForecastBucketUnit bucketOrNull,
            Integer horizonOrNull,
            ForecastMethod methodOrNull,
            LocalDate from,
            LocalDate to
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        Request request = request(diagnosisCode, bucketOrNull, horizonOrNull, methodOrNull, from, to);

        ForecastGeographyCountSource countSource = new ForecastGeographyCountSource(
                forecastSeriesRepository, request.unit(), request.window().bucketStarts()
        );
        List<ReportHierarchyNode<ForecastSeries>> nodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, countSource,
                request.window().range(), request.diagnosisCode()
        );

        return nodes.stream().map(node -> nodeRow(node, request)).toList();
    }

    public ForecastResponse getSeries(
            String regionCode,
            String districtCode,
            String diagnosisCode,
            ForecastBucketUnit bucketOrNull,
            Integer horizonOrNull,
            ForecastMethod methodOrNull,
            LocalDate from,
            LocalDate to
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        ResolvedReportNode node = reportHierarchyService.resolveNode(currentOrganization, regionCode, districtCode);
        Request request = request(diagnosisCode, bucketOrNull, horizonOrNull, methodOrNull, from, to);

        long[] counts = seriesFor(node.organizationIds(), request);
        List<LocalDate> bucketStarts = request.window().bucketStarts();
        Computation computation = compute(counts, bucketStarts, request);

        List<ForecastHistoryPointResponse> history = new ArrayList<>(bucketStarts.size());
        long trainingTotal = 0;
        for (int i = 0; i < bucketStarts.size(); i++) {
            LocalDate start = bucketStarts.get(i);
            history.add(new ForecastHistoryPointResponse(start, bucketEnd(request.unit(), start), counts[i]));
            trainingTotal += counts[i];
        }

        LocalDate lastStart = bucketStarts.get(bucketStarts.size() - 1);
        ForecastSummaryResponse summary = new ForecastSummaryResponse(
                node.code(),
                node.name(),
                request.diagnosisCode(),
                request.unit(),
                bucketStarts.get(0),
                bucketEnd(request.unit(), lastStart),
                bucketStarts.size(),
                trainingTotal,
                round2((double) trainingTotal / bucketStarts.size()),
                computation.result().method(),
                round2(computation.trendPerBucket()),
                computation.forecastTotal(),
                computation.alertBuckets(),
                computation.peakPeriodStart()
        );

        return new ForecastResponse(summary, history, computation.points());
    }

    /**
     * "Which diseases might rise" for one geography node: every ICD-10 code
     * seen in the node's training window gets its own independent forecast
     * (same models/endemic channel as {@link #getSeries}), and the result is
     * a risk-ranked top {@code limit} list — {@link ForecastRiskLevel#HIGH}
     * first (by {@code alertBuckets}), then {@link ForecastRiskLevel#MEDIUM}
     * (by rising trend), then the rest by {@code forecastTotal}. Codes with
     * fewer than {@code minCases} total training-window notifications are
     * dropped before forecasting — too sparse a series to say anything about,
     * and the dominant cost of this endpoint (one query total, then one
     * forecast per surviving code).
     */
    public List<ForecastDiseaseRiskResponse> getTopDiseases(
            String regionCode,
            String districtCode,
            ForecastBucketUnit bucketOrNull,
            Integer horizonOrNull,
            ForecastMethod methodOrNull,
            LocalDate from,
            LocalDate to,
            Integer limitOrNull,
            Long minCasesOrNull
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        ResolvedReportNode node = reportHierarchyService.resolveNode(currentOrganization, regionCode, districtCode);
        Request request = request(null, bucketOrNull, horizonOrNull, methodOrNull, from, to);
        int limit = limitOrNull != null ? Math.max(1, Math.min(limitOrNull, MAX_TOP_DISEASES)) : DEFAULT_TOP_DISEASES;
        long minCases = minCasesOrNull != null ? Math.max(0, minCasesOrNull) : DEFAULT_MIN_TRAINING_CASES;

        List<LocalDate> bucketStarts = request.window().bucketStarts();
        Map<LocalDate, Integer> indexByStart = new HashMap<>(bucketStarts.size() * 2);
        for (int i = 0; i < bucketStarts.size(); i++) {
            indexByStart.put(bucketStarts.get(i), i);
        }

        Map<String, long[]> countsByCode = new HashMap<>();
        for (ForecastDiseaseBucketCountProjection row : forecastSeriesRepository.countByBucketGroupedByDiagnosis(
                node.organizationIds(), request.unit(),
                request.window().range().fromInclusive(), request.window().range().toExclusive()
        )) {
            Integer i = indexByStart.get(row.bucketStart());
            if (i == null) {
                continue;
            }
            countsByCode.computeIfAbsent(row.diagnosisCode(), k -> new long[bucketStarts.size()])[i] += row.count();
        }

        Map<String, String> diagnosisNames = icd10LookupService.resolveNames(countsByCode.keySet());

        List<ForecastDiseaseRiskResponse> rows = new ArrayList<>(countsByCode.size());
        for (Map.Entry<String, long[]> entry : countsByCode.entrySet()) {
            long[] counts = entry.getValue();
            long trainingTotal = Arrays.stream(counts).sum();
            if (trainingTotal < minCases) {
                continue;
            }

            String code = entry.getKey();
            Computation computation = compute(counts, bucketStarts, request);
            rows.add(new ForecastDiseaseRiskResponse(
                    code,
                    diagnosisNames.getOrDefault(code, code),
                    riskLevel(computation.alertBuckets(), computation.trendPerBucket()),
                    computation.result().method(),
                    trainingTotal,
                    counts[counts.length - 1],
                    computation.points().isEmpty() ? 0L : computation.points().get(0).predicted(),
                    computation.forecastTotal(),
                    round2(computation.trendPerBucket()),
                    computation.alertBuckets(),
                    computation.peakPeriodStart()
            ));
        }

        rows.sort(Comparator
                .comparingInt((ForecastDiseaseRiskResponse r) -> riskRank(r.riskLevel())).reversed()
                .thenComparing(Comparator.comparingInt(ForecastDiseaseRiskResponse::alertBuckets).reversed())
                .thenComparing(Comparator.comparingDouble(ForecastDiseaseRiskResponse::trendPerBucket).reversed())
                .thenComparing(Comparator.comparingLong(ForecastDiseaseRiskResponse::forecastTotal).reversed()));

        return rows.size() > limit ? rows.subList(0, limit) : rows;
    }

    private ForecastRiskLevel riskLevel(int alertBuckets, double trendPerBucket) {
        if (alertBuckets > 0) {
            return ForecastRiskLevel.HIGH;
        }
        return trendPerBucket > 0 ? ForecastRiskLevel.MEDIUM : ForecastRiskLevel.LOW;
    }

    private int riskRank(ForecastRiskLevel level) {
        return switch (level) {
            case HIGH -> 2;
            case MEDIUM -> 1;
            case LOW -> 0;
        };
    }

    private ForecastNodeResponse nodeRow(ReportHierarchyNode<ForecastSeries> node, Request request) {
        long[] counts = node.counts().counts();
        Computation computation = compute(counts, request.window().bucketStarts(), request);
        long trainingTotal = node.counts().total();

        return new ForecastNodeResponse(
                node.code(),
                node.name(),
                node.hasChildren(),
                computation.result().method(),
                trainingTotal,
                counts.length > 0 ? counts[counts.length - 1] : 0L,
                computation.points().isEmpty() ? 0L : computation.points().get(0).predicted(),
                computation.forecastTotal(),
                round2(computation.trendPerBucket()),
                computation.alertBuckets(),
                computation.peakPeriodStart()
        );
    }

    private Computation compute(long[] counts, List<LocalDate> bucketStarts, Request request) {
        double[] series = new double[counts.length];
        for (int i = 0; i < counts.length; i++) {
            series[i] = counts[i];
        }

        TimeSeriesForecaster.Result result =
                TimeSeriesForecaster.forecast(series, request.horizon(), request.unit().seasonLength(), request.method());
        EndemicChannel channel = EndemicChannel.from(groupBySeasonIndex(request.unit(), bucketStarts, series));

        LocalDate lastStart = bucketStarts.get(bucketStarts.size() - 1);
        List<ForecastPredictionPointResponse> points = new ArrayList<>(request.horizon());
        LocalDate cursor = request.unit().next(lastStart);
        long forecastTotal = 0;
        int alertBuckets = 0;
        long peakPredicted = -1;
        LocalDate peakStart = null;
        for (int h = 0; h < request.horizon(); h++) {
            long predicted = Math.round(result.point()[h]);
            long lower = Math.round(result.lower()[h]);
            long upper = Math.round(result.upper()[h]);
            long threshold = (long) Math.ceil(channel.thresholdFor(request.unit().seasonIndex(cursor)));
            boolean alert = predicted > threshold;

            points.add(new ForecastPredictionPointResponse(
                    cursor, bucketEnd(request.unit(), cursor), predicted, lower, upper, threshold, alert
            ));

            forecastTotal += predicted;
            if (alert) {
                alertBuckets++;
            }
            if (predicted > peakPredicted) {
                peakPredicted = predicted;
                peakStart = cursor;
            }
            cursor = request.unit().next(cursor);
        }

        return new Computation(result, points, forecastTotal, alertBuckets, peakStart,
                trendPerBucket(result.point(), series));
    }

    private long[] seriesFor(List<Long> organizationIds, Request request) {
        List<LocalDate> bucketStarts = request.window().bucketStarts();
        Map<LocalDate, Integer> indexByStart = new HashMap<>(bucketStarts.size() * 2);
        for (int i = 0; i < bucketStarts.size(); i++) {
            indexByStart.put(bucketStarts.get(i), i);
        }

        long[] counts = new long[bucketStarts.size()];
        for (ForecastBucketCountProjection row : forecastSeriesRepository.countByBucket(
                organizationIds, request.unit(),
                request.window().range().fromInclusive(), request.window().range().toExclusive(),
                request.diagnosisCode()
        )) {
            Integer i = indexByStart.get(row.bucketStart());
            if (i != null) {
                counts[i] += row.count();
            }
        }
        return counts;
    }

    private Request request(
            String diagnosisCode,
            ForecastBucketUnit bucketOrNull,
            Integer horizonOrNull,
            ForecastMethod methodOrNull,
            LocalDate from,
            LocalDate to
    ) {
        ForecastBucketUnit unit = bucketOrNull != null ? bucketOrNull : ForecastBucketUnit.WEEK;
        ForecastMethod method = methodOrNull != null ? methodOrNull : ForecastMethod.AUTO;
        int horizon = Math.max(MIN_HORIZON, Math.min(horizonOrNull != null ? horizonOrNull : DEFAULT_HORIZON, unit.maxHorizon()));
        return new Request(unit, method, horizon, normalizeDiagnosis(diagnosisCode), resolveTrainingWindow(unit, from, to));
    }

    private TrainingWindow resolveTrainingWindow(ForecastBucketUnit unit, LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now(APPLICATION_ZONE);
        LocalDate effectiveTo = to != null ? to : today;
        LocalDate lastBucketStart = unit.truncate(effectiveTo);

        LocalDate effectiveFrom = from != null
                ? from
                : unit.minusBuckets(lastBucketStart, unit.defaultLookbackBuckets());
        LocalDate firstBucketStart = unit.truncate(effectiveFrom);

        if (firstBucketStart.isAfter(lastBucketStart)) {
            firstBucketStart = lastBucketStart;
        }

        List<LocalDate> starts = new ArrayList<>();
        for (LocalDate cursor = firstBucketStart; !cursor.isAfter(lastBucketStart); cursor = unit.next(cursor)) {
            starts.add(cursor);
        }
        if (starts.size() > MAX_TRAINING_BUCKETS) {
            starts = new ArrayList<>(starts.subList(starts.size() - MAX_TRAINING_BUCKETS, starts.size()));
        }

        Instant fromInclusive = starts.get(0).atStartOfDay(APPLICATION_ZONE).toInstant();
        Instant toExclusive = unit.next(lastBucketStart).atStartOfDay(APPLICATION_ZONE).toInstant();
        return new TrainingWindow(new ReportDateRange(fromInclusive, toExclusive), starts);
    }

    private Map<Integer, List<Double>> groupBySeasonIndex(
            ForecastBucketUnit unit, List<LocalDate> bucketStarts, double[] series
    ) {
        Map<Integer, List<Double>> grouped = new HashMap<>();
        for (int i = 0; i < bucketStarts.size(); i++) {
            grouped.computeIfAbsent(unit.seasonIndex(bucketStarts.get(i)), k -> new ArrayList<>()).add(series[i]);
        }
        return grouped;
    }

    private String normalizeDiagnosis(String diagnosisCode) {
        if (diagnosisCode == null || diagnosisCode.isBlank()) {
            return null;
        }
        return diagnosisCode.trim().toUpperCase(Locale.ROOT);
    }

    private LocalDate bucketEnd(ForecastBucketUnit unit, LocalDate start) {
        return unit.next(start).minusDays(1);
    }

    private double trendPerBucket(double[] point, double[] history) {
        if (point.length >= 2) {
            return (point[point.length - 1] - point[0]) / (point.length - 1);
        }
        double lastActual = history.length > 0 ? history[history.length - 1] : 0.0;
        return point.length == 1 ? point[0] - lastActual : 0.0;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }

    private record Request(
            ForecastBucketUnit unit,
            ForecastMethod method,
            int horizon,
            String diagnosisCode,
            TrainingWindow window
    ) {
    }

    private record TrainingWindow(ReportDateRange range, List<LocalDate> bucketStarts) {
    }

    private record Computation(
            TimeSeriesForecaster.Result result,
            List<ForecastPredictionPointResponse> points,
            long forecastTotal,
            int alertBuckets,
            LocalDate peakPeriodStart,
            double trendPerBucket
    ) {
    }
}
