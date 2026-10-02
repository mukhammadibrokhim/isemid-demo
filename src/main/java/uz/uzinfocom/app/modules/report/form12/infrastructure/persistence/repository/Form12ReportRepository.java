package uz.uzinfocom.app.modules.report.form12.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12Counts;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12DiagnosisCountProjection;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12OrganizationDiagnosisCountProjection;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Form 12" — native SQL aggregation across {@code form058} and {@code
 * form058_1} for the by-nosological-form infectious/parasitic disease report,
 * over an arbitrary caller-supplied {@code [fromInclusive, toExclusive)}
 * range. Confirmed cases only: {@code status = 'APPROVED'} (unlike Form
 * 6/8/9/11, which count primary/not-yet-decided notifications) — Form 12 is a
 * final-diagnosis statistical form.
 * <p>
 * The "diagnosis" of a case is its confirmed final code alone — {@code
 * f.final_icd10_code}, with <b>no</b> fallback to the initial {@code
 * f.icd10_code}: a case whose final diagnosis was never recorded ({@code
 * final_icd10_code is null}) does not appear in this report at all. Matching on
 * that single value (rather than "initial OR final is in the set", as {@code
 * Form1ReportRepository}'s CONFIRMED block does) keeps a case from landing in
 * two different nosological-form rows at once.
 * <p>
 * {@link #countByDiagnosisCode} is deliberately <b>fully static SQL</b> — it
 * groups by the confirmed code and returns one row per distinct code; the
 * query service rolls those up per catalog entry in Java. Nothing
 * caller-influenced is ever spliced into the SQL text: {@code organizationIds}
 * is an inlined {@code VALUES} join of our own {@code Long}s (see {@code
 * Form1ReportRepository} for the full rationale — index-friendly and
 * injection-free), and the ICD-10 code set used by the drill-down queries is a
 * bound {@code IN (:codes)} collection parameter.
 * <p>
 * Age is {@code extract(year from age(f.created_at::date, p.birth_date))} —
 * complete calendar years at the case's own {@code created_at}, same
 * expression as {@code Form1ReportRepository} / {@code Form6ReportRepository}.
 * Grouped/filtered by {@code sender_organization_id} (the institution that
 * <b>created</b> the case) — see {@code Form1ReportRepository}.
 * <p>
 * Every {@code ::type} cast wraps its named parameter in parentheses —
 * {@code (:param)::type}, never {@code :param::type} — see {@code
 * Form1ReportRepository} for why.
 */
@Repository
@RequiredArgsConstructor
public class Form12ReportRepository {

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

    /** Same as {@link #UNION_SOURCE_TEMPLATE} but also keeps {@code sender_organization_id} for the org grouping. */
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
     * scope — the raw material for the report's root level. Static SQL; the
     * query service maps codes to {@code FORM_12} catalog entries in memory.
     */
    public List<Form12DiagnosisCountProjection> countByDiagnosisCode(
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
                    return new Form12DiagnosisCountProjection(
                            (String) r[0], count(r, 1), count(r, 2), count(r, 3)
                    );
                })
                .toList();
    }

    /** One count row per organization id, restricted to one nosological form's ICD-10 set — geography drill-down. */
    public List<Form12OrganizationCountProjection> countGroupedByOrganizationForCodes(
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
                    return new Form12OrganizationCountProjection(
                            ((Number) r[0]).longValue(), count(r, 1), count(r, 2), count(r, 3)
                    );
                })
                .toList();
    }

    /**
     * One count row per (organization id, confirmed-diagnosis code) pair, with
     * no code filter — the raw material for the "Form 12 by territory" view,
     * where every nosological form is its own column against every territory
     * row (unlike {@link #countGroupedByOrganizationForCodes}, which restricts
     * to a single nosological form's code set).
     */
    public List<Form12OrganizationDiagnosisCountProjection> countGroupedByOrganizationAndDiagnosisCode(
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
                    return new Form12OrganizationDiagnosisCountProjection(
                            ((Number) r[0]).longValue(), (String) r[1],
                            new Form12Counts(count(r, 2), count(r, 3), count(r, 4))
                    );
                })
                .toList();
    }

    /** A single, unattributed total for one nosological form's ICD-10 set — a drill-down root node. */
    public Form12Counts countTotalForCodes(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive, Collection<String> codes
    ) {
        if (organizationIds == null || organizationIds.isEmpty() || codes == null || codes.isEmpty()) {
            return Form12Counts.EMPTY;
        }

        String sql = "select " + METRIC_COLUMNS
                + " from (" + unionSource(organizationIds) + ") t where t.diagnosis_code in (:codes)";

        Object row = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive)
                .setParameter("codes", codes)
                .getSingleResult();

        Object[] r = (Object[]) row;
        return new Form12Counts(count(r, 0), count(r, 1), count(r, 2));
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
