package uz.uzinfocom.app.modules.report.forecast.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastBucketUnit;
import uz.uzinfocom.app.modules.report.forecast.infrastructure.persistence.repository.dto.ForecastBucketCountProjection;
import uz.uzinfocom.app.modules.report.forecast.infrastructure.persistence.repository.dto.ForecastOrgBucketCountProjection;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Native-SQL time-bucket aggregation across {@code form058} + {@code
 * form058_1} for the surveillance forecast — counts per {@link
 * ForecastBucketUnit} bucket over a caller-supplied {@code [from, to)} range,
 * an org-id scope, and an optional ICD-10 filter. Two shapes: one grand
 * series ({@link #countByBucket}) for a single resolved node, and one series
 * per organization ({@link #countGroupedByOrganizationAndBucket}) for the
 * geography breakdown, which the shared {@code ReportHierarchyService} folds
 * up region→district→"Jami".
 *
 * <p>Mechanics copied verbatim from {@code Form9ReportRepository}: a {@code
 * (values …)} list join instead of {@code IN (…)}, {@code (:param)::type}
 * casts with the parameter always parenthesised (Hibernate's named-parameter
 * scanner otherwise eats a trailing {@code ::}), and {@code created_at at
 * time zone 'Asia/Tashkent'} for bucketing so week/month edges land on local
 * midnight.
 *
 * <p>Case set is deliberately wider than the confirmed-count reports: every
 * live notification ({@code deleted = false} and {@code status <>
 * 'CANCELED'} — primary and confirmed alike), because a forecast of disease
 * burden must not wait for final approval. The ICD-10 filter therefore
 * matches the initial {@code icd10_code} <b>or</b> the final {@code
 * final_icd10_code}, not the final code alone.
 *
 * <p>{@code date_trunc}'s field argument is interpolated from the {@link
 * ForecastBucketUnit} enum ({@code 'day'} / {@code 'week'} / {@code
 * 'month'}) — a fixed literal set, never caller input.
 */
@Repository
@RequiredArgsConstructor
public class ForecastSeriesRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select f.sender_organization_id as organization_id,
                   date_trunc('%2$s', f.created_at at time zone 'Asia/Tashkent') as bucket_start
            from form058 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status <> 'CANCELED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null
                   or f.icd10_code = (:diagnosisCode)::text
                   or f.final_icd10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id,
                   date_trunc('%2$s', f.created_at at time zone 'Asia/Tashkent')
            from form058_1 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status <> 'CANCELED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null
                   or f.icd10_code = (:diagnosisCode)::text
                   or f.final_icd10_code = (:diagnosisCode)::text)
            """;

    private final EntityManager entityManager;

    /**
     * The grand series across every given organization id, ascending by
     * bucket start. Empty buckets are absent — the caller fills them.
     * Returns an empty list (no query) for an empty org-id scope.
     */
    public List<ForecastBucketCountProjection> countByBucket(
            List<Long> organizationIds,
            ForecastBucketUnit unit,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.bucket_start as bucket_start, count(*) as cnt from ("
                + unionSource(organizationIds, unit) + ") t group by t.bucket_start order by t.bucket_start";

        List<?> rows = bind(sql, fromInclusive, toExclusive, diagnosisCode).getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new ForecastBucketCountProjection(toLocalDate(r[0]), ((Number) r[1]).longValue());
                })
                .toList();
    }

    /**
     * One count row per {@code (organization id, bucket start)} pair — the
     * geography-breakdown feed. Only non-empty pairs are returned; {@code
     * ForecastGeographyCountSource} maps them onto the fixed bucket axis.
     */
    public List<ForecastOrgBucketCountProjection> countGroupedByOrganizationAndBucket(
            List<Long> organizationIds,
            ForecastBucketUnit unit,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.organization_id as organization_id, t.bucket_start as bucket_start, count(*) as cnt from ("
                + unionSource(organizationIds, unit) + ") t group by t.organization_id, t.bucket_start";

        List<?> rows = bind(sql, fromInclusive, toExclusive, diagnosisCode).getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new ForecastOrgBucketCountProjection(
                            ((Number) r[0]).longValue(), toLocalDate(r[1]), ((Number) r[2]).longValue()
                    );
                })
                .toList();
    }

    private String unionSource(List<Long> organizationIds, ForecastBucketUnit unit) {
        String valuesList = organizationIds.stream().map(id -> "(" + id + ")").collect(Collectors.joining(","));
        return UNION_SOURCE_TEMPLATE.formatted(valuesList, unit.sqlTruncField());
    }

    private Query bind(String sql, Instant fromInclusive, Instant toExclusive, String diagnosisCode) {
        return entityManager.createNativeQuery(sql)
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive)
                .setParameter("diagnosisCode", diagnosisCode);
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.time.OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDate();
        }
        throw new IllegalStateException("Unexpected bucket_start type: " + value.getClass());
    }
}
