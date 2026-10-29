package uz.uzinfocom.app.modules.report.form9.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9Counts;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9MonthlyCountProjection;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9OrganizationCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Form 9" — native SQL aggregation across {@code form058} and {@code
 * form058_1} for the comparative infectious-disease report, over an arbitrary
 * caller-supplied {@code [fromInclusive, toExclusive)} range. Mirrors {@code
 * Form8ReportRepository} exactly (same {@code VALUES}-list org-id join instead
 * of {@code IN}/{@code = any(...)}, same {@code (:param)::type} cast style,
 * same primary-only status filter {@code status NOT IN ('APPROVED',
 * 'CANCELED')}) but produces two metrics per row instead of one:
 * <ul>
 *   <li>{@code registered} — every primary/not-yet-decided notification;</li>
 *   <li>{@code hospitalized} — the subset with a hospitalization reference
 *   ({@code form058.hospital_place_id IS NOT NULL} /
 *   {@code form058_1.hospitalized_at IS NOT NULL}).</li>
 * </ul>
 * A form058 rejected by the receiver is stored as {@code CANCELED} (see {@code
 * FormStatus}), so it is already excluded here — there is no separate
 * "rejected" status to account for. No {@code patient} join: neither metric
 * reads patient columns. Grouped/filtered by {@code sender_organization_id} —
 * the institution that <b>created</b> the case — see {@code
 * Form1ReportRepository} for the full rationale.
 * <p>
 * Every {@code ::type} cast below wraps its named parameter in parentheses —
 * {@code (:param)::type}, never {@code :param::type} — see {@code
 * Form1ReportRepository} for why (Hibernate's named-parameter scanner reads
 * trailing {@code ::} as part of the parameter's own name otherwise).
 */
@Repository
@RequiredArgsConstructor
public class Form9ReportRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select f.sender_organization_id,
                   (f.hospital_place_id is not null) as hospitalized,
                   extract(month from f.created_at at time zone 'Asia/Tashkent')::int as month
            from form058 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id,
                   (f.hospitalized_at is not null) as hospitalized,
                   extract(month from f.created_at at time zone 'Asia/Tashkent')::int
            from form058_1 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            """;

    private static final String METRIC_COLUMNS = """
            count(*)                                  as registered,
            count(*) filter (where t.hospitalized)    as hospitalized
            """;

    private final EntityManager entityManager;

    /** One count row per organization id — for a region/district/organization-level breakdown. */
    public List<Form9OrganizationCountProjection> countGroupedByOrganization(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.sender_organization_id as organization_id, " + METRIC_COLUMNS
                + " from (" + unionSource(organizationIds) + ") t group by t.sender_organization_id";

        List<?> rows = bindParameters(entityManager.createNativeQuery(sql), fromInclusive, toExclusive, diagnosisCode)
                .getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new Form9OrganizationCountProjection(((Number) r[0]).longValue(), count(r, 1), count(r, 2));
                })
                .toList();
    }

    /**
     * A single, unattributed pair of totals across every given organization id
     * — for the report's root node, avoiding fetching a per-organization
     * breakdown just to sum it in Java.
     */
    public Form9Counts countTotal(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Form9Counts.EMPTY;
        }

        String sql = "select " + METRIC_COLUMNS + " from (" + unionSource(organizationIds) + ") t";

        Object row = bindParameters(entityManager.createNativeQuery(sql), fromInclusive, toExclusive, diagnosisCode)
                .getSingleResult();

        Object[] r = (Object[]) row;
        return new Form9Counts(count(r, 0), count(r, 1));
    }

    /**
     * Calendar-month-bucketed pair of totals across every given organization
     * id, for a single node's whole sub-tree (see {@code
     * ReportHierarchyService#resolveNode}) — never grouped by organization,
     * this report's month drill-down shows one table per node. Only months
     * with data are returned; the query service fills 1..12.
     */
    public List<Form9MonthlyCountProjection> countMonthlyBreakdown(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.month as month, " + METRIC_COLUMNS
                + " from (" + unionSource(organizationIds) + ") t group by t.month";

        List<?> rows = bindParameters(entityManager.createNativeQuery(sql), fromInclusive, toExclusive, diagnosisCode)
                .getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new Form9MonthlyCountProjection(((Number) r[0]).intValue(), count(r, 1), count(r, 2));
                })
                .toList();
    }

    private String unionSource(List<Long> organizationIds) {
        String valuesList = organizationIds.stream().map(id -> "(" + id + ")").collect(Collectors.joining(","));
        return UNION_SOURCE_TEMPLATE.formatted(valuesList);
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

    private long count(Object[] row, int index) {
        Object value = row[index];
        return value == null ? 0L : ((Number) value).longValue();
    }
}
