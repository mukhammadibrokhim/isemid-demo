package uz.uzinfocom.app.modules.report.airhealth.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import uz.uzinfocom.app.modules.report.shared.ReportCaseAttribution;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.airhealth.application.query.dto.AirHealthBucket;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Native-SQL confirmed-case counts for the "air and respiratory diseases" report: {@code form058}
 * + {@code form058_1} cases that are {@code status = 'APPROVED'} and whose {@code final_icd10_code}
 * starts with a prefix (no fallback to the initial code - the diagnosis-code rule every APPROVED
 * count follows). Same mechanics as the other report repositories: organization scope through a
 * {@code (values ...)} join of our own resolved ids, {@code (:param)::type} casts, bucketing on
 * {@code created_at at time zone 'Asia/Tashkent'}, and {@code date_trunc}'s field interpolated only
 * from {@link AirHealthBucket}'s fixed literals.
 */
@Repository
@RequiredArgsConstructor
public class AirHealthCaseRepository {

    public record TerritoryCases(String territoryCode, long cases) {
    }

    public record BucketCases(LocalDate bucketStart, long cases) {
    }

    private static final String TERRITORY_UNION = """
            select %2$s as code
            from form058 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            join organization o on o.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and upper(f.final_icd10_code) like (:prefix)
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select %2$s
            from form058_1 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            join organization o on o.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and upper(f.final_icd10_code) like (:prefix)
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    private static final String BUCKET_UNION = """
            select date_trunc('%2$s', (f.created_at at time zone 'Asia/Tashkent') - ((:lagDays)::int * interval '1 day')) as bucket_start
            from form058 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and upper(f.final_icd10_code) like (:prefix)
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select date_trunc('%2$s', (f.created_at at time zone 'Asia/Tashkent') - ((:lagDays)::int * interval '1 day'))
            from form058_1 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and upper(f.final_icd10_code) like (:prefix)
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    private final EntityManager entityManager;
    private final ReportCaseAttribution reportCaseAttribution;

    /**
     * Cases per sender organization's district ({@code byDistrict}) or region; organizations with no
     * such code are left out. Empty list (no query) for an empty organization scope.
     */
    public List<TerritoryCases> countByTerritory(
            List<Long> organizationIds, boolean byDistrict, String icd10Prefix,
            Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }
        String column = byDistrict ? "o.district_code" : "o.region_code";
        String sql = "select t.code as code, count(*) as cnt from ("
                + TERRITORY_UNION.formatted(valuesList(organizationIds), column)
                + ") t where t.code is not null group by t.code";

        Query query = entityManager.createNativeQuery(reportCaseAttribution.apply(sql))
                .setParameter("prefix", icd10Prefix + "%")
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive);
        List<?> rows = query.getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new TerritoryCases((String) r[0], ((Number) r[1]).longValue());
                })
                .toList();
    }

    /**
     * Cases per time bucket. Each case is counted in the bucket {@code lagDays} days <em>before</em>
     * its creation date, so a bucket lines up with the air readings that preceded it by that lag;
     * {@code fromInclusive}/{@code toExclusive} bound the case dates themselves (the caller has
     * already shifted them by the lag).
     */
    public List<BucketCases> countByBucket(
            List<Long> organizationIds, AirHealthBucket bucket, int lagDays, String icd10Prefix,
            Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }
        String sql = "select t.bucket_start as bucket_start, count(*) as cnt from ("
                + BUCKET_UNION.formatted(valuesList(organizationIds), bucket.sqlTruncField())
                + ") t group by t.bucket_start";

        Query query = entityManager.createNativeQuery(reportCaseAttribution.apply(sql))
                .setParameter("prefix", icd10Prefix + "%")
                .setParameter("lagDays", lagDays)
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive);
        List<?> rows = query.getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new BucketCases(toLocalDate(r[0]), ((Number) r[1]).longValue());
                })
                .toList();
    }

    private String valuesList(List<Long> organizationIds) {
        return organizationIds.stream().map(id -> "(" + id + ")").collect(Collectors.joining(","));
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
