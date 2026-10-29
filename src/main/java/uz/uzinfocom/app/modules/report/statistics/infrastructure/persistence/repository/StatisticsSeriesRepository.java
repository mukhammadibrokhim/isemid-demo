package uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsFormType;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsSeriesBucket;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsSeriesCaseBucketProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsSeriesForm129BucketProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsTopDiseaseCountProjection;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Statistika" — native-SQL time-bucket and disease-ranking aggregation over
 * {@code form058} + {@code form058_1} (+ {@code form_129} for the series
 * total). Companion to {@link StatisticsReportRepository} (the geography
 * breakdown) for the two dimensions the geography drill-down cannot express:
 * time dynamics ({@code /series}) and the ICD-10 ranking ({@code
 * /top-diseases}).
 * <p>
 * Mechanics match every other report repository: a {@code (values …)} list
 * join instead of {@code IN (…)}, {@code (:param)::type} casts with the
 * parameter always parenthesised, {@code created_at at time zone
 * 'Asia/Tashkent'} for bucketing, and {@code date_trunc}'s field argument
 * interpolated only from the {@link StatisticsSeriesBucket} enum's fixed
 * literal set. Grouped/filtered by {@code sender_organization_id}, the
 * institution that created the case.
 * <p>
 * The series keeps the report's confirmed / primary split ({@code status =
 * 'APPROVED'} vs {@code status NOT IN ('APPROVED', 'CANCELED')}); the ranking
 * counts confirmed only and matches {@code final_icd10_code} alone (no
 * {@code coalesce} fallback to the initial code), the diagnosis-code rule
 * every APPROVED-count report block follows.
 */
@Repository
@RequiredArgsConstructor
public class StatisticsSeriesRepository {

    private static final String CASE_UNION_TEMPLATE = """
            select 'FORM058'::text as form_type, f.status as status,
                   date_trunc('%2$s', f.created_at at time zone 'Asia/Tashkent') as bucket_start
            from form058 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select 'FORM0581'::text, f.status,
                   date_trunc('%2$s', f.created_at at time zone 'Asia/Tashkent')
            from form058_1 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    private static final String DISEASE_UNION_TEMPLATE = """
            select f.final_icd10_code as diagnosis_code
            from form058 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.final_icd10_code is not null
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select f.final_icd10_code
            from form058_1 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.final_icd10_code is not null
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    private final EntityManager entityManager;

    /**
     * One row per ({@link StatisticsFormType form}, bucket start) with the
     * confirmed/primary case counts of {@code form058} / {@code form058_1}.
     * Only non-empty buckets. Empty list (no query) for an empty org scope.
     */
    public List<StatisticsSeriesCaseBucketProjection> countCasesByBucket(
            List<Long> organizationIds, StatisticsSeriesBucket bucket, Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String source = CASE_UNION_TEMPLATE.formatted(valuesList(organizationIds), bucket.sqlTruncField());
        String sql = "select t.form_type as form_type, t.bucket_start as bucket_start, "
                + "count(*) filter (where t.status = 'APPROVED') as confirmed, "
                + "count(*) filter (where t.status not in ('APPROVED', 'CANCELED')) as primary_count "
                + "from (" + source + ") t group by t.form_type, t.bucket_start";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive).getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new StatisticsSeriesCaseBucketProjection(
                            StatisticsFormType.valueOf((String) r[0]), toLocalDate(r[1]),
                            count(r[2]), count(r[3])
                    );
                })
                .toList();
    }

    /** One row per bucket start with the total {@code form_129} notification count. Only non-empty buckets. */
    public List<StatisticsSeriesForm129BucketProjection> countForm129ByBucket(
            List<Long> organizationIds, StatisticsSeriesBucket bucket, Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String source = "select date_trunc('" + bucket.sqlTruncField()
                + "', f.created_at at time zone 'Asia/Tashkent') as bucket_start "
                + "from form_129 f "
                + "join (values " + valuesList(organizationIds) + ") as scope_org(id) on scope_org.id = f.sender_organization_id "
                + "where f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz";
        String sql = "select t.bucket_start as bucket_start, count(*) as total from ("
                + source + ") t group by t.bucket_start";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive).getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new StatisticsSeriesForm129BucketProjection(toLocalDate(r[0]), count(r[1]));
                })
                .toList();
    }

    /**
     * One row per {@code final_icd10_code} with its confirmed ({@code status =
     * 'APPROVED'}) {@code form058} + {@code form058_1} case count over the
     * period and scope, descending by count. The query service resolves names
     * and truncates to {@code limit}.
     */
    public List<StatisticsTopDiseaseCountProjection> countConfirmedByFinalDiagnosis(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String source = DISEASE_UNION_TEMPLATE.formatted(valuesList(organizationIds));
        String sql = "select t.diagnosis_code as diagnosis_code, count(*) as cnt from ("
                + source + ") t group by t.diagnosis_code order by cnt desc";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive).getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new StatisticsTopDiseaseCountProjection((String) r[0], count(r[1]));
                })
                .toList();
    }

    private String valuesList(List<Long> organizationIds) {
        return organizationIds.stream().map(id -> "(" + id + ")").collect(Collectors.joining(","));
    }

    private Query bindRange(Query query, Instant fromInclusive, Instant toExclusive) {
        return query
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive);
    }

    private long count(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
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
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDate();
        }
        throw new IllegalStateException("Unexpected bucket_start type: " + value.getClass());
    }
}
