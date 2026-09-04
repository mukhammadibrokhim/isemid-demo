package uz.uzinfocom.app.modules.report.form10.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form10.application.query.dto.Form10Counts;
import uz.uzinfocom.app.modules.report.form10.application.query.dto.Form10OrganizationCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Form 10" — native SQL aggregation across {@code form058} and {@code
 * form058_1} for the infectious-disease morbidity report, over one
 * caller-supplied half-open {@code [fromInclusive, toExclusive)} span. Mirrors
 * {@code Form11ReportRepository} (same {@code VALUES}-list org-id join, same
 * {@code (:param)::type} cast style, same {@code patient} join for the age
 * cut, same confirmed-only {@code status = 'APPROVED'} filter) but produces
 * just two metrics per row — {@code total} and the <b>under-14</b> cut (Form
 * 11 cuts at 18 and also carries urban/rural, which Form 10 does not need).
 * <p>
 * Grouped/filtered by {@code sender_organization_id} — the institution that
 * <b>created</b> the case — see {@code Form1ReportRepository} for the full
 * rationale. Every {@code ::type} cast wraps its named parameter in
 * parentheses — {@code (:param)::type}, never {@code :param::type} — again see
 * {@code Form1ReportRepository} for why.
 */
@Repository
@RequiredArgsConstructor
public class Form10ReportRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select f.sender_organization_id,
                   extract(year from age(f.created_at::date, p.birth_date))::int as age_years
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id,
                   extract(year from age(f.created_at::date, p.birth_date))::int
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            """;

    private static final String METRIC_COLUMNS = """
            count(*)                                                       as total,
            count(*) filter (where t.age_years >= 0 and t.age_years < 14)  as child
            """;

    private final EntityManager entityManager;

    /** One count row per organization id — for a region/district/organization-level breakdown. */
    public List<Form10OrganizationCountProjection> countGroupedByOrganization(
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
                    return new Form10OrganizationCountProjection(
                            ((Number) r[0]).longValue(), count(r, 1), count(r, 2)
                    );
                })
                .toList();
    }

    /** A single, unattributed total across every given organization id — for the report's root node. */
    public Form10Counts countTotal(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Form10Counts.EMPTY;
        }

        String sql = "select " + METRIC_COLUMNS + " from (" + unionSource(organizationIds) + ") t";

        Object row = bindParameters(entityManager.createNativeQuery(sql), fromInclusive, toExclusive, diagnosisCode)
                .getSingleResult();

        Object[] r = (Object[]) row;
        return new Form10Counts(count(r, 0), count(r, 1));
    }

    private String unionSource(List<Long> organizationIds) {
        String valuesList = organizationIds.stream().map(id -> "(" + id + ")").collect(Collectors.joining(","));
        return UNION_SOURCE_TEMPLATE.formatted(valuesList);
    }

    private Query bindParameters(Query query, Instant fromInclusive, Instant toExclusive, String diagnosisCode) {
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
