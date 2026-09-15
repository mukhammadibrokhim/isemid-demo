package uz.uzinfocom.app.modules.report.form13.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13DiagnosisCountProjection;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13Metric;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13OrganizationDiagnosisCountProjection;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Form 13" — native SQL aggregation across {@code form058} and {@code
 * form058_1}, structurally close to {@code Form12ReportRepository}
 * (confirmed cases only, {@code status = 'APPROVED'}; age is {@code extract(year
 * from age(f.created_at::date, p.birth_date))}; scoped and grouped by {@code
 * sender_organization_id}). Unlike Form 12, the diagnosis of a case here is the
 * <b>confirmed final code alone</b> — {@code f.final_icd10_code}, with <b>no</b>
 * fallback to the initial {@code f.icd10_code}: a case that never got a final
 * diagnosis recorded does not appear in this report at all. The difference from
 * Form 12 in shape is the grain: "Form 13" needs the
 * counts split by <b>diagnosis code AND organization</b> at once, because the
 * report puts every disease on its own column against every territory row — so
 * the query service can roll a whole row of per-disease numbers up the
 * geography hierarchy in one pass.
 * <p>
 * Both queries are fully static SQL. Nothing caller-influenced is spliced into
 * the SQL text: {@code organizationIds} is an inlined {@code VALUES} join of
 * our own {@code Long}s (see {@code Form1ReportRepository} for the rationale),
 * and the ICD-10 code sets of the {@code FORM_13} catalog entries are applied
 * in Java, not in the query. Every {@code ::type} cast wraps its named
 * parameter in parentheses — {@code (:param)::type} — see {@code
 * Form1ReportRepository} for why.
 */
@Repository
@RequiredArgsConstructor
public class Form13ReportRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select upper(f.final_icd10_code) as diagnosis_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int as age_years
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.final_icd10_code is not null
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select upper(f.final_icd10_code),
                   extract(year from age(f.created_at::date, p.birth_date))::int
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.final_icd10_code is not null
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    private static final String UNION_SOURCE_WITH_ORG_TEMPLATE = """
            select f.sender_organization_id,
                   upper(f.final_icd10_code) as diagnosis_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int as age_years
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.final_icd10_code is not null
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select f.sender_organization_id,
                   upper(f.final_icd10_code),
                   extract(year from age(f.created_at::date, p.birth_date))::int
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.final_icd10_code is not null
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    private static final String METRIC_COLUMNS = """
            count(*)                                                        as total,
            count(*) filter (where t.age_years >= 0 and t.age_years < 14)   as under_14,
            count(*) filter (where t.age_years >= 0 and t.age_years < 18)   as under_18
            """;

    private final EntityManager entityManager;

    /**
     * One aggregate row per distinct confirmed-diagnosis code across the whole
     * scope (no organization attribution) — the raw material for the drill-down
     * root ("Jami") node.
     */
    public List<Form13DiagnosisCountProjection> countByDiagnosisCode(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.diagnosis_code as diagnosis_code, " + METRIC_COLUMNS
                + " from (" + unionSource(organizationIds) + ") t"
                + " where t.diagnosis_code is not null group by t.diagnosis_code";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive).getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new Form13DiagnosisCountProjection(
                            (String) r[0], count(r, 1), count(r, 2), count(r, 3)
                    );
                })
                .toList();
    }

    /**
     * One aggregate row per (organization id, confirmed-diagnosis code) pair —
     * the raw material for every geography breakdown level.
     */
    public List<Form13OrganizationDiagnosisCountProjection> countGroupedByOrganizationAndDiagnosisCode(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.sender_organization_id as organization_id, t.diagnosis_code as diagnosis_code, "
                + METRIC_COLUMNS
                + " from (" + unionSourceWithOrg(organizationIds) + ") t"
                + " where t.diagnosis_code is not null group by t.sender_organization_id, t.diagnosis_code";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive).getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new Form13OrganizationDiagnosisCountProjection(
                            ((Number) r[0]).longValue(), (String) r[1], count(r, 2), count(r, 3), count(r, 4)
                    );
                })
                .toList();
    }

    /**
     * One count row per organization id, restricted to one {@code FORM_13}
     * catalog entry's ICD-10 set — the "Form 13 by disease" geography
     * drill-down, structurally identical to {@code
     * Form12ReportRepository#countGroupedByOrganizationForCodes}.
     */
    public List<Form13OrganizationCountProjection> countGroupedByOrganizationForCodes(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive, Collection<String> codes
    ) {
        if (organizationIds == null || organizationIds.isEmpty() || codes == null || codes.isEmpty()) {
            return List.of();
        }

        String sql = "select t.sender_organization_id as organization_id, " + METRIC_COLUMNS
                + " from (" + unionSourceWithOrg(organizationIds) + ") t"
                + " where t.diagnosis_code in (:codes) group by t.sender_organization_id";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive)
                .setParameter("codes", codes)
                .getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new Form13OrganizationCountProjection(
                            ((Number) r[0]).longValue(), count(r, 1), count(r, 2), count(r, 3)
                    );
                })
                .toList();
    }

    /**
     * A single, unattributed total for one {@code FORM_13} catalog entry's
     * ICD-10 set — a "Form 13 by disease" drill-down root node.
     */
    public Form13Metric countTotalForCodes(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive, Collection<String> codes
    ) {
        if (organizationIds == null || organizationIds.isEmpty() || codes == null || codes.isEmpty()) {
            return Form13Metric.EMPTY;
        }

        String sql = "select " + METRIC_COLUMNS
                + " from (" + unionSource(organizationIds) + ") t where t.diagnosis_code in (:codes)";

        Object row = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive)
                .setParameter("codes", codes)
                .getSingleResult();

        Object[] r = (Object[]) row;
        return new Form13Metric(count(r, 0), count(r, 1), count(r, 2));
    }

    private String unionSource(List<Long> organizationIds) {
        return UNION_SOURCE_TEMPLATE.formatted(valuesList(organizationIds));
    }

    private String unionSourceWithOrg(List<Long> organizationIds) {
        return UNION_SOURCE_WITH_ORG_TEMPLATE.formatted(valuesList(organizationIds));
    }

    private String valuesList(List<Long> organizationIds) {
        return organizationIds.stream().map(id -> "(" + id + ")").collect(Collectors.joining(","));
    }

    private Query bindRange(Query query, Instant fromInclusive, Instant toExclusive) {
        return query
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive);
    }

    private long count(Object[] row, int index) {
        Object value = row[index];
        return value == null ? 0L : ((Number) value).longValue();
    }
}
