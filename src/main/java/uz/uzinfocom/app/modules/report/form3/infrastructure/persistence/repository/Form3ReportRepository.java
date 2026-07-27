package uz.uzinfocom.app.modules.report.form3.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * "Form 3" — native SQL aggregation across {@code form058} and {@code
 * form058_1} for the year-over-year comparative report, over an arbitrary
 * caller-supplied {@code [fromInclusive, toExclusive)} range (the caller —
 * {@code Form3ReportQueryService} — calls this once per period: the current
 * period and the same calendar dates one year earlier). Mirrors {@code
 * Form1ReportRepository} and {@code Form2ReportRepository}: a single
 * Criteria-API query can't span two unrelated entity roots, so this UNIONs
 * both tables' rows first — each branch pre-filtered on sender/status/
 * created_at so the planner can use each table's own composite index
 * ({@code idx_form058_outgoing_table}, {@code
 * idx_form0581_sender_status_created}) — then counts in one pass. Only a
 * single {@code count(*)} ever leaves the database per call; no case/
 * patient row is ever materialized in the JVM.
 * <p>
 * Grouped/filtered by {@code sender_organization_id} ("createdOrg" — the
 * institution that created the case), not {@code receiver_organization_id}
 * — see {@code Form1ReportRepository} for the rationale. Only PRIMARY (not
 * yet resolved) notifications are counted, same rule as Form 2.
 * <p>
 * {@code organizationIds} is spliced into the SQL text as literal integers
 * (safe — every element is a {@code Long} from our own scope resolution,
 * never user-supplied text), not bound as a query parameter. See {@code
 * Form1ReportRepository} for why: both a Hibernate {@code IN (?,?,?...)}
 * expansion and a {@code = any(:param)} array bind turned out to be dead
 * ends for a list this size, repeated across multiple UNION branches.
 * <p>
 * Every {@code ::type} cast below wraps its named parameter in parentheses
 * — {@code (:param)::type}, never {@code :param::type} — see {@code
 * Form1ReportRepository} for why (Hibernate's named-parameter scanner reads
 * trailing {@code ::} as part of the parameter's own name otherwise).
 */
@Repository
@RequiredArgsConstructor
public class Form3ReportRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select f.sender_organization_id
            from form058 f
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.sender_organization_id in (%1$s)
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.mkb10_code = (:diagnosisCode)::text or f.final_mkb10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id
            from form058_1 f
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.sender_organization_id in (%1$s)
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.mkb10_code = (:diagnosisCode)::text or f.final_mkb10_code = (:diagnosisCode)::text)
            """;

    private final EntityManager entityManager;

    /** One count per organization id — for a region/district/organization-level breakdown. */
    public Map<Long, Long> countGroupedByOrganization(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Map.of();
        }

        Query query = bindParameters(
                entityManager.createNativeQuery(groupedSql(organizationIds)), fromInclusive, toExclusive, diagnosisCode
        );

        Map<Long, Long> countsByOrganizationId = new HashMap<>();
        for (Object row : query.getResultList()) {
            Object[] columns = (Object[]) row;
            countsByOrganizationId.put(
                    ((Number) columns[0]).longValue(),
                    ((Number) columns[1]).longValue()
            );
        }
        return countsByOrganizationId;
    }

    /** A single, unattributed total across every given organization id — for the report's root node. */
    public long countTotal(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return 0L;
        }

        Query query = bindParameters(
                entityManager.createNativeQuery(totalSql(organizationIds)), fromInclusive, toExclusive, diagnosisCode
        );

        // Single-column native query: Hibernate returns a bare scalar per row, not Object[].
        List<?> rows = query.getResultList();
        if (rows.isEmpty() || rows.getFirst() == null) {
            return 0L;
        }
        return ((Number) rows.getFirst()).longValue();
    }

    private String unionSource(List<Long> organizationIds) {
        String idList = organizationIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        return UNION_SOURCE_TEMPLATE.formatted(idList);
    }

    private String groupedSql(List<Long> organizationIds) {
        return "select t.sender_organization_id as organization_id, count(*) as total from ("
                + unionSource(organizationIds) + ") t group by t.sender_organization_id";
    }

    private String totalSql(List<Long> organizationIds) {
        return "select count(*) as total from (" + unionSource(organizationIds) + ") t";
    }

    private Query bindParameters(
            Query query,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        return query
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive)
                .setParameter("diagnosisCode", diagnosisCode);
    }
}
