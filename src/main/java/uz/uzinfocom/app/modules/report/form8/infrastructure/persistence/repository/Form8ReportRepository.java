package uz.uzinfocom.app.modules.report.form8.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form8.application.query.dto.Form8CategoryBreakdownProjection;
import uz.uzinfocom.app.modules.report.form8.application.query.dto.Form8OrganizationCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Form 8" — native SQL aggregation across {@code form058} and {@code
 * form058_1} for the "infectious/parasitic disease by social composition"
 * comparison report, over an arbitrary caller-supplied {@code [fromInclusive,
 * toExclusive)} range. Mirrors {@code Form6ReportRepository} exactly (same
 * {@code VALUES}-list org-id join instead of {@code IN}/{@code = any(...)},
 * same {@code (:param)::type} cast style, same primary-only status filter)
 * but buckets by {@code patient.category_code} instead of age — this report
 * has no CONFIRMED counterpart, so the UNION below has 2 branches, not 4. A
 * form058 rejected by the receiver is stored as {@code CANCELED} (see {@code
 * FormStatus}), so it is already excluded here — there is no separate
 * "rejected" status to account for.
 * <p>
 * Category buckets for {@link #countCategoryBreakdown} are the same set
 * "Form 4" breaks out ({@code Form4ReportRepository}); the seeded {@code
 * TEACHER} category is not a bucket of its own but still counts toward the
 * {@code total}. Grouped/filtered by {@code sender_organization_id} — the
 * institution that <b>created</b> the case — see {@code Form1ReportRepository}
 * for the full rationale.
 * <p>
 * Every {@code ::type} cast below wraps its named parameter in parentheses —
 * {@code (:param)::type}, never {@code :param::type} — see {@code
 * Form1ReportRepository} for why (Hibernate's named-parameter scanner reads
 * trailing {@code ::} as part of the parameter's own name otherwise).
 */
@Repository
@RequiredArgsConstructor
public class Form8ReportRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select f.sender_organization_id, p.category_code
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id, p.category_code
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            """;

    private static final String CATEGORY_BREAKDOWN_COLUMNS = """
            count(*)                                                                as total,
            count(*) filter (where t.category_code = 'NO_ORGANIZED')                as unorganized_preschool,
            count(*) filter (where t.category_code = 'ORGANIZED')                   as organized_preschool,
            count(*) filter (where t.category_code = 'WORKER')                      as workers,
            count(*) filter (where t.category_code = 'NOT_EMPLOYED')               as unemployed,
            count(*) filter (where t.category_code = 'PENSIONER')                   as pensioners,
            count(*) filter (where t.category_code = 'STUDENT_SCHOOL')             as school_students,
            count(*) filter (where t.category_code = 'UNSHELTRED')                 as unsheltered,
            count(*) filter (where t.category_code = 'SEREVANTS')                  as employees,
            count(*) filter (where t.category_code = 'MEDICAL_WORKER')             as medical_staff,
            count(*) filter (where t.category_code = 'MIDDLE_STUDENT')             as vocational_students,
            count(*) filter (where t.category_code = 'STUDENT')                    as university_students
            """;

    private final EntityManager entityManager;

    /** One count row per organization id — for a region/district/organization-level breakdown. */
    public List<Form8OrganizationCountProjection> countGroupedByOrganization(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.sender_organization_id as organization_id, count(*) as total "
                + "from (" + unionSource(organizationIds) + ") t group by t.sender_organization_id";

        List<?> rows = bindParameters(entityManager.createNativeQuery(sql), fromInclusive, toExclusive, diagnosisCode)
                .getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new Form8OrganizationCountProjection(((Number) r[0]).longValue(), count(r, 1));
                })
                .toList();
    }

    /**
     * A single, unattributed total across every given organization id — for
     * the report's root node, avoiding fetching a per-organization
     * breakdown just to sum it in Java.
     */
    public long countTotal(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return 0;
        }

        String sql = "select count(*) from (" + unionSource(organizationIds) + ") t";

        Object result = bindParameters(entityManager.createNativeQuery(sql), fromInclusive, toExclusive, diagnosisCode)
                .getSingleResult();

        return result == null ? 0 : ((Number) result).longValue();
    }

    /**
     * Social-category-bucketed total across every given organization id, for
     * a single node's whole sub-tree (see {@code
     * ReportHierarchyService#resolveNode}) — never grouped by organization,
     * this report's category drill-down shows one table per node.
     */
    public Form8CategoryBreakdownProjection countCategoryBreakdown(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Form8CategoryBreakdownProjection.EMPTY;
        }

        String sql = "select " + CATEGORY_BREAKDOWN_COLUMNS + " from (" + unionSource(organizationIds) + ") t";

        List<?> rows = bindParameters(entityManager.createNativeQuery(sql), fromInclusive, toExclusive, diagnosisCode)
                .getResultList();

        return rows.isEmpty() ? Form8CategoryBreakdownProjection.EMPTY : toCategoryBreakdown((Object[]) rows.getFirst());
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

    private Form8CategoryBreakdownProjection toCategoryBreakdown(Object[] row) {
        return new Form8CategoryBreakdownProjection(
                count(row, 0), count(row, 1), count(row, 2), count(row, 3),
                count(row, 4), count(row, 5), count(row, 6), count(row, 7),
                count(row, 8), count(row, 9), count(row, 10), count(row, 11)
        );
    }

    private long count(Object[] row, int index) {
        Object value = row[index];
        return value == null ? 0L : ((Number) value).longValue();
    }
}
