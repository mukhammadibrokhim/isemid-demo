package uz.uzinfocom.app.modules.report.form11.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11Counts;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11OrganizationCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Form 11" — native SQL aggregation across {@code form058} and {@code
 * form058_1} for the infectious/parasitic disease morbidity-indicator report,
 * over an arbitrary caller-supplied {@code [fromInclusive, toExclusive)}
 * range. Mirrors {@code Form9ReportRepository} exactly (same {@code VALUES}-list
 * org-id join instead of {@code IN}/{@code = any(...)}, same {@code
 * (:param)::type} cast style) but joins {@code patient} (like {@code
 * Form6ReportRepository}), counts confirmed cases only ({@code status =
 * 'APPROVED'}) rather than primary notifications, and produces four metrics
 * per row:
 * <ul>
 *   <li>{@code total} — every confirmed notification;</li>
 *   <li>{@code city} — the subset with {@code patient.population_type_code =
 *   'CITY_RESIDENT'};</li>
 *   <li>{@code rural} — the subset with {@code 'VILLAGE_RESIDENT'};</li>
 *   <li>{@code child} — the subset under 18 complete calendar years at {@code
 *   created_at} (same age expression as Form 6/Form 1).</li>
 * </ul>
 * Grouped/filtered by {@code
 * sender_organization_id} — the institution that <b>created</b> the case — see
 * {@code Form1ReportRepository} for the full rationale.
 * <p>
 * Every {@code ::type} cast below wraps its named parameter in parentheses —
 * {@code (:param)::type}, never {@code :param::type} — see {@code
 * Form1ReportRepository} for why (Hibernate's named-parameter scanner reads
 * trailing {@code ::} as part of the parameter's own name otherwise).
 */
@Repository
@RequiredArgsConstructor
public class Form11ReportRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select f.sender_organization_id,
                   p.population_type_code as pop_type,
                   extract(year from age(f.created_at::date, p.birth_date))::int as age_years
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.final_icd10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id,
                   p.population_type_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.final_icd10_code = (:diagnosisCode)::text)
            """;

    private static final String METRIC_COLUMNS = """
            count(*)                                                        as total,
            count(*) filter (where t.pop_type = 'CITY_RESIDENT')            as city,
            count(*) filter (where t.pop_type = 'VILLAGE_RESIDENT')         as rural,
            count(*) filter (where t.age_years >= 0 and t.age_years < 18)   as child
            """;

    private final EntityManager entityManager;

    /** One count row per organization id — for a region/district/organization-level breakdown. */
    public List<Form11OrganizationCountProjection> countGroupedByOrganization(
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
                    return new Form11OrganizationCountProjection(
                            ((Number) r[0]).longValue(), count(r, 1), count(r, 2), count(r, 3), count(r, 4)
                    );
                })
                .toList();
    }

    /**
     * A single, unattributed set of totals across every given organization id
     * — for the report's root node, avoiding fetching a per-organization
     * breakdown just to sum it in Java.
     */
    public Form11Counts countTotal(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Form11Counts.EMPTY;
        }

        String sql = "select " + METRIC_COLUMNS + " from (" + unionSource(organizationIds) + ") t";

        Object row = bindParameters(entityManager.createNativeQuery(sql), fromInclusive, toExclusive, diagnosisCode)
                .getSingleResult();

        Object[] r = (Object[]) row;
        return new Form11Counts(count(r, 0), count(r, 1), count(r, 2), count(r, 3));
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
