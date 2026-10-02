package uz.uzinfocom.app.modules.report.forecast.application.query;

import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastBucketUnit;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastSeries;
import uz.uzinfocom.app.modules.report.forecast.infrastructure.persistence.repository.ForecastSeriesRepository;
import uz.uzinfocom.app.modules.report.forecast.infrastructure.persistence.repository.dto.ForecastBucketCountProjection;
import uz.uzinfocom.app.modules.report.forecast.infrastructure.persistence.repository.dto.ForecastOrgBucketCountProjection;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link ReportCountSource} strategy for the forecast report's geography
 * breakdown: every geography node (region, district, organization, "Jami")
 * carries a whole {@link ForecastSeries} — the node's bucketed case counts
 * over the training window — which the shared {@code ReportHierarchyService}
 * builds by element-wise-merging the per-organization series.
 *
 * <p>Deliberately <b>not</b> a Spring bean — constructed per request in
 * {@code ForecastReportQueryService} with the resolved {@link
 * ForecastBucketUnit} and the fixed bucket axis (the training window's
 * bucket-start list), then handed to the hierarchy engine, which is designed
 * to receive a fresh {@link ReportCountSource} per call. Unlike the "Form 12
 * by territory" source, the interface's {@code diagnosisCode} argument <b>is</b>
 * honoured here — it is the report's ICD-10 filter, passed straight through
 * to the repository.
 */
public final class ForecastGeographyCountSource implements ReportCountSource<ForecastSeries> {

    private final ForecastSeriesRepository repository;
    private final ForecastBucketUnit unit;
    private final List<LocalDate> bucketStarts;
    private final Map<LocalDate, Integer> indexByBucketStart;

    public ForecastGeographyCountSource(
            ForecastSeriesRepository repository, ForecastBucketUnit unit, List<LocalDate> bucketStarts
    ) {
        this.repository = repository;
        this.unit = unit;
        this.bucketStarts = bucketStarts;
        this.indexByBucketStart = new HashMap<>(bucketStarts.size() * 2);
        for (int i = 0; i < bucketStarts.size(); i++) {
            indexByBucketStart.put(bucketStarts.get(i), i);
        }
    }

    @Override
    public ForecastSeries total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        long[] counts = new long[bucketStarts.size()];
        for (ForecastBucketCountProjection row : repository.countByBucket(
                organizationIds, unit, range.fromInclusive(), range.toExclusive(), diagnosisCode
        )) {
            Integer i = indexByBucketStart.get(row.bucketStart());
            if (i != null) {
                counts[i] += row.count();
            }
        }
        return new ForecastSeries(counts);
    }

    @Override
    public Map<Long, ForecastSeries> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        Map<Long, long[]> byOrg = new HashMap<>();
        for (ForecastOrgBucketCountProjection row : repository.countGroupedByOrganizationAndBucket(
                organizationIds, unit, range.fromInclusive(), range.toExclusive(), diagnosisCode
        )) {
            Integer i = indexByBucketStart.get(row.bucketStart());
            if (i != null) {
                byOrg.computeIfAbsent(row.organizationId(), k -> new long[bucketStarts.size()])[i] += row.count();
            }
        }

        Map<Long, ForecastSeries> result = new HashMap<>(byOrg.size() * 2);
        byOrg.forEach((organizationId, counts) -> result.put(organizationId, new ForecastSeries(counts)));
        return result;
    }

    @Override
    public ForecastSeries empty() {
        return ForecastSeries.zero(bucketStarts.size());
    }

    @Override
    public ForecastSeries merge(ForecastSeries a, ForecastSeries b) {
        return a.plus(b);
    }
}
