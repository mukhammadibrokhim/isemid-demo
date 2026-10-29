package uz.uzinfocom.app.modules.report.form282.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282Counts;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282DiagnosisCountProjection;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282OrganizationDiagnosisCountProjection;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Form 28.2" — native SQL aggregation across {@code form058} and {@code
 * form058_1} for the «Kasalxona ichki infeksiyalari haqida ma'lumotlar»
 * reference form. A structural clone of {@code Form281ReportRepository} /
 * {@code Form12ReportRepository}: confirmed cases only ({@code status =
 * 'APPROVED'}, {@code deleted = false}), the "diagnosis" of a case is its
 * confirmed final code alone — {@code f.final_icd10_code}, with <b>no</b>
 * fallback to the initial {@code f.icd10_code}, and nothing caller-influenced
 * is ever spliced into the SQL text ({@code organizationIds} inlined {@code
 * VALUES} join, ICD-10 set a bound {@code IN (:codes)} parameter).
 * <p>
 * The difference from Form 28.1 is the column set — the varaqa of this form:
 * total / under-18 (years) / under-1-month / 1-month-to-under-1-year. So the
 * source keeps both a year-granularity age ({@code age_years}) and a
 * month-component age ({@code age_months}, the {@code month} field of {@code
 * age(...)}, 0–11 — meaningful only while {@code age_years = 0}).
 * <p>
 * Every {@code ::type} cast wraps its named parameter in parentheses —
 * {@code (:param)::type} — see {@code Form1ReportRepository} for why.
 */
@Repository
@RequiredArgsConstructor
public class Form282ReportRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select upper(f.final_icd10_code) as diagnosis_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int as age_years,
                   extract(month from age(f.created_at::date, p.birth_date))::int as age_months
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.final_icd10_code is not null
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select upper(f.final_icd10_code),
                   extract(year from age(f.created_at::date, p.birth_date))::int,
                   extract(month from age(f.created_at::date, p.birth_date))::int
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
                   extract(year from age(f.created_at::date, p.birth_date))::int as age_years,
                   extract(month from age(f.created_at::date, p.birth_date))::int as age_months
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
                   extract(year from age(f.created_at::date, p.birth_date))::int,
                   extract(month from age(f.created_at::date, p.birth_date))::int
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.final_icd10_code is not null
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    /**
     * The 4 varaqa columns, in the order {@link #readCounts} reads them back.
     * «1 oygacha» = 0 complete years and 0 complete months; «1 oy 1 yoshgacha» =
     * 0 complete years and at least 1 complete month (i.e. 1–11 months).
     */
    private static final String METRIC_COLUMNS = """
            count(*)                                                                  as total,
            count(*) filter (where t.age_years >= 0 and t.age_years < 18)             as under_18,
            count(*) filter (where t.age_years = 0 and t.age_months = 0)              as under_1_month,
            count(*) filter (where t.age_years = 0 and t.age_months >= 1)             as month1_to_year1
            """;

    private final EntityManager entityManager;

    /**
     * One aggregate row per distinct confirmed-diagnosis code across the whole
     * scope — the raw material for the report's root level. Static SQL; the
     * query service maps codes to {@code FORM_28_2} catalog entries in memory.
     */
    public List<Form282DiagnosisCountProjection> countByDiagnosisCode(
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
                    return new Form282DiagnosisCountProjection((String) r[0], readCounts(r, 1));
                })
                .toList();
    }

    /** One count row per organization id, restricted to one nosological form's ICD-10 set — geography drill-down. */
    public List<Form282OrganizationCountProjection> countGroupedByOrganizationForCodes(
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
                    return new Form282OrganizationCountProjection(((Number) r[0]).longValue(), readCounts(r, 1));
                })
                .toList();
    }

    /**
     * One count row per (organization id, confirmed-diagnosis code) pair, with
     * no code filter — the raw material for the "Form 28.2 by territory" view,
     * where every disease is its own column against every territory row (unlike
     * {@link #countGroupedByOrganizationForCodes}, which restricts to a single
     * nosological form's code set).
     */
    public List<Form282OrganizationDiagnosisCountProjection> countGroupedByOrganizationAndDiagnosisCode(
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
                    return new Form282OrganizationDiagnosisCountProjection(
                            ((Number) r[0]).longValue(), (String) r[1], readCounts(r, 2)
                    );
                })
                .toList();
    }

    /** A single, unattributed total for one nosological form's ICD-10 set — a drill-down root node. */
    public Form282Counts countTotalForCodes(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive, Collection<String> codes
    ) {
        if (organizationIds == null || organizationIds.isEmpty() || codes == null || codes.isEmpty()) {
            return Form282Counts.EMPTY;
        }

        String sql = "select " + METRIC_COLUMNS
                + " from (" + unionSource(organizationIds) + ") t where t.diagnosis_code in (:codes)";

        Object row = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive)
                .setParameter("codes", codes)
                .getSingleResult();

        return readCounts((Object[]) row, 0);
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

    private Form282Counts readCounts(Object[] row, int offset) {
        return new Form282Counts(
                count(row, offset),
                count(row, offset + 1),
                count(row, offset + 2),
                count(row, offset + 3)
        );
    }

    private long count(Object[] row, int index) {
        Object value = row[index];
        return value == null ? 0L : ((Number) value).longValue();
    }
}
