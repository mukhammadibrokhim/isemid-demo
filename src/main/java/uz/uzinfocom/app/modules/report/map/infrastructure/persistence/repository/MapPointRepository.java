package uz.uzinfocom.app.modules.report.map.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.map.application.query.dto.MapPointProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Native-SQL point list for the "case map" report — a flat list of
 * {@code form058} cases that carry coordinates ({@code fm058_location}),
 * scoped to an organization-id list, an optional exact {@code status}, and
 * an optional diagnosis code match. Structurally the same idiom as {@code
 * Form13ReportRepository} (org scope via an inlined {@code VALUES} join of
 * our own resolved {@code Long}s, {@code (:param)::type} casts), but no
 * aggregation — one row per matching case.
 * <p>
 * {@code form058_1} has no coordinate column anywhere in the schema, so it
 * is intentionally not part of this query.
 * <p>
 * Unbounded (no {@code from}/{@code to}, republic-wide scope) this table
 * has 600k+ matching rows — returning and serializing all of them is what
 * actually makes the endpoint "slow", not a missing index. Every call is
 * therefore capped with {@code order by created_at desc, id desc limit},
 * so the DB and the response body stay bounded regardless of scope/date
 * width; see {@code MapPointQueryService} for the default/max values.
 */
@Repository
@RequiredArgsConstructor
public class MapPointRepository {

    private static final String QUERY_TEMPLATE = """
            select f.id as id,
                   loc.latitude as latitude,
                   loc.longitude as longitude,
                   loc.location as address,
                   coalesce(upper(f.final_icd10_code), upper(f.icd10_code)) as diagnosis_code,
                   f.status as status,
                   f.created_at as created_at
            from form058 f
            join fm058_location loc on loc.id = f.location_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    private static final String ORDER_AND_LIMIT = " order by f.created_at desc, f.id desc limit (:limit)";

    private final EntityManager entityManager;

    public List<MapPointProjection> findPoints(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String status,
            String diagnosisCode,
            int limit
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        StringBuilder sql = new StringBuilder(QUERY_TEMPLATE.formatted(valuesList(organizationIds)));
        if (status != null) {
            sql.append(" and f.status = (:status)");
        }
        if (diagnosisCode != null) {
            sql.append(" and coalesce(upper(f.final_icd10_code), upper(f.icd10_code)) = (:diagnosisCode)");
        }
        sql.append(ORDER_AND_LIMIT);

        Query query = entityManager.createNativeQuery(sql.toString())
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive)
                .setParameter("limit", limit);
        if (status != null) {
            query.setParameter("status", status);
        }
        if (diagnosisCode != null) {
            query.setParameter("diagnosisCode", diagnosisCode);
        }

        List<?> rows = query.getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new MapPointProjection(
                            ((Number) r[0]).longValue(),
                            (Double) r[1],
                            (Double) r[2],
                            (String) r[3],
                            (String) r[4],
                            (String) r[5],
                            (Instant) r[6]
                    );
                })
                .toList();
    }

    private String valuesList(List<Long> organizationIds) {
        return organizationIds.stream().map(id -> "(" + id + ")").collect(Collectors.joining(","));
    }
}
