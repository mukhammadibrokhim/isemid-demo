package uz.uzinfocom.app.modules.report.form6.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form6.application.query.dto.Form6AgeBreakdownProjection;
import uz.uzinfocom.app.modules.report.form6.application.query.dto.Form6OrganizationCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Form 6" — native SQL aggregation across {@code form058} and {@code
 * form058_1} for the infectious/parasitic disease age-structure report, over
 * an arbitrary caller-supplied {@code [fromInclusive, toExclusive)} range.
 * Mirrors {@code Form1ReportRepository} (same {@code VALUES}-list org-id
 * join instead of {@code IN}/{@code = any(...)}, same {@code (:param)::type}
 * cast style, same {@code extract(year from age(f.created_at::date,
 * p.birth_date))::int} age calculation) but only ever counts the "primary"
 * bucket — {@code status NOT IN ('APPROVED', 'CANCELED')} — this report has
 * no CONFIRMED counterpart, so the UNION below has 2 branches instead of 4.
 * A form058 rejected by the receiver is stored as {@code CANCELED} (see
 * {@code FormStatus}), so it is already excluded here — there is no
 * separate "rejected" status to account for.
 * <p>
 * Age brackets for {@link #countAgeBreakdown}, all in complete calendar
 * years as of the case's own {@code created_at} (matches how Form 1 already
 * defines under14/under18): newborn (&lt;1), 1-2, 3-5, 6-14, 15-17, under18
 * (subtotal of the previous five), then adult brackets starting at 18 —
 * confirmed with the report's author that a patient exactly 18 years old
 * falls into the "19-25" bracket, not the under-18 subtotal, so that
 * bracket's actual SQL condition is {@code >= 18 and < 26} despite its
 * display label.
 */
@Repository
@RequiredArgsConstructor
public class Form6ReportRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select f.sender_organization_id,
                   extract(year from age(f.created_at::date, p.birth_date))::int as age_years
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id,
                   extract(year from age(f.created_at::date, p.birth_date))::int
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            """;

    private static final String AGE_BREAKDOWN_COLUMNS = """
            count(*)                                             as total,
            count(*) filter (where t.age_years < 1)               as newborn,
            count(*) filter (where t.age_years >= 1 and t.age_years < 3)   as age_1_2,
            count(*) filter (where t.age_years >= 3 and t.age_years < 6)   as age_3_5,
            count(*) filter (where t.age_years >= 6 and t.age_years < 15)  as age_6_14,
            count(*) filter (where t.age_years >= 15 and t.age_years < 18) as age_15_17,
            count(*) filter (where t.age_years < 18)               as under_18,
            count(*) filter (where t.age_years >= 18 and t.age_years < 26) as age_19_25,
            count(*) filter (where t.age_years >= 26 and t.age_years < 41) as age_26_40,
            count(*) filter (where t.age_years >= 41 and t.age_years < 56) as age_41_55,
            count(*) filter (where t.age_years >= 56 and t.age_years < 71) as age_56_70,
            count(*) filter (where t.age_years >= 71)              as over_70
            """;

    private final EntityManager entityManager;

    /** One count row per organization id — for a region/district/organization-level breakdown. */
    public List<Form6OrganizationCountProjection> countGroupedByOrganization(
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
                    return new Form6OrganizationCountProjection(((Number) r[0]).longValue(), count(r, 1));
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
     * Age-bucketed total across every given organization id, for a single
     * node's whole sub-tree (see {@code ReportHierarchyService#resolveNode})
     * — never grouped by organization, this report's age drill-down shows
     * one table per node, not per organization.
     */
    public Form6AgeBreakdownProjection countAgeBreakdown(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Form6AgeBreakdownProjection.EMPTY;
        }

        String sql = "select " + AGE_BREAKDOWN_COLUMNS + " from (" + unionSource(organizationIds) + ") t";

        List<?> rows = bindParameters(entityManager.createNativeQuery(sql), fromInclusive, toExclusive, diagnosisCode)
                .getResultList();

        return rows.isEmpty() ? Form6AgeBreakdownProjection.EMPTY : toAgeBreakdown((Object[]) rows.getFirst());
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

    private Form6AgeBreakdownProjection toAgeBreakdown(Object[] row) {
        return new Form6AgeBreakdownProjection(
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
