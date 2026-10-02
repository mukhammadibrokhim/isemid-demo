package uz.uzinfocom.app.modules.report.form281.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281Counts;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281DiagnosisCountProjection;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281OrganizationDiagnosisCountProjection;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Form 28.1" — native SQL aggregation across {@code form058} and {@code
 * form058_1} for the «Ayrim yuqumli va parazitar kasalliklar haqida
 * ma'lumotlar» reference form, over an arbitrary caller-supplied {@code
 * [fromInclusive, toExclusive)} range. Structurally a clone of {@code
 * Form12ReportRepository}: confirmed cases only ({@code status = 'APPROVED'},
 * {@code deleted = false}), the "diagnosis" of a case is its confirmed final
 * code alone — {@code f.final_icd10_code}, with <b>no</b> fallback to the
 * initial {@code f.icd10_code} (a case whose final diagnosis was never recorded
 * does not appear), and nothing caller-influenced is ever spliced into the SQL
 * text: {@code organizationIds} is an inlined {@code VALUES} join of our own
 * {@code Long}s (see {@code Form1ReportRepository} for the rationale) and the
 * ICD-10 code set is a bound {@code IN (:codes)} collection parameter.
 * <p>
 * The difference from Form 12 is the column set — the reference form's varaqa:
 * total / female / under-18 / under-15 / under-1 / 1–2 / 3–5, and the same age
 * cuts again restricted to the rural population ({@code
 * patient.population_type_code = 'VILLAGE_RESIDENT'}). There is no
 * year-over-year comparison — one period only.
 * <p>
 * Age is {@code extract(year from age(f.created_at::date, p.birth_date))} —
 * complete calendar years at the case's own {@code created_at}. Grouped/filtered
 * by {@code sender_organization_id} (the institution that <b>created</b> the
 * case). Every {@code ::type} cast wraps its named parameter in parentheses —
 * {@code (:param)::type} — see {@code Form1ReportRepository} for why.
 */
@Repository
@RequiredArgsConstructor
public class Form281ReportRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select upper(f.final_icd10_code) as diagnosis_code,
                   p.gender_code as gender_code,
                   p.population_type_code as pop_type,
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
                   p.gender_code,
                   p.population_type_code,
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
                   p.gender_code as gender_code,
                   p.population_type_code as pop_type,
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
                   p.gender_code,
                   p.population_type_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.final_icd10_code is not null
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    /**
     * The 13 varaqa columns, in the order {@link #readCounts} reads them back.
     * The rural cuts add {@code and t.pop_type = 'VILLAGE_RESIDENT'} to their
     * age predicate; «Ayollarda» is not repeated for the rural block, matching
     * the reference form.
     */
    private static final String METRIC_COLUMNS = """
            count(*)                                                                        as total,
            count(*) filter (where t.gender_code = 'FEMALE')                                as female,
            count(*) filter (where t.age_years >= 0 and t.age_years < 18)                   as under_18,
            count(*) filter (where t.age_years >= 0 and t.age_years < 15)                   as under_15,
            count(*) filter (where t.age_years >= 0 and t.age_years < 1)                    as under_1,
            count(*) filter (where t.age_years >= 1 and t.age_years <= 2)                   as age_1_2,
            count(*) filter (where t.age_years >= 3 and t.age_years <= 5)                   as age_3_5,
            count(*) filter (where t.pop_type = 'VILLAGE_RESIDENT')                         as rural_total,
            count(*) filter (where t.pop_type = 'VILLAGE_RESIDENT'
                                   and t.age_years >= 0 and t.age_years < 18)               as rural_under_18,
            count(*) filter (where t.pop_type = 'VILLAGE_RESIDENT'
                                   and t.age_years >= 0 and t.age_years < 15)               as rural_under_15,
            count(*) filter (where t.pop_type = 'VILLAGE_RESIDENT'
                                   and t.age_years >= 0 and t.age_years < 1)                as rural_under_1,
            count(*) filter (where t.pop_type = 'VILLAGE_RESIDENT'
                                   and t.age_years >= 1 and t.age_years <= 2)               as rural_age_1_2,
            count(*) filter (where t.pop_type = 'VILLAGE_RESIDENT'
                                   and t.age_years >= 3 and t.age_years <= 5)               as rural_age_3_5
            """;

    private final EntityManager entityManager;

    /**
     * One aggregate row per distinct confirmed-diagnosis code across the whole
     * scope — the raw material for the report's root level. Static SQL; the
     * query service maps codes to {@code FORM_28_1} catalog entries in memory.
     */
    public List<Form281DiagnosisCountProjection> countByDiagnosisCode(
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
                    return new Form281DiagnosisCountProjection((String) r[0], readCounts(r, 1));
                })
                .toList();
    }

    /** One count row per organization id, restricted to one nosological form's ICD-10 set — geography drill-down. */
    public List<Form281OrganizationCountProjection> countGroupedByOrganizationForCodes(
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
                    return new Form281OrganizationCountProjection(((Number) r[0]).longValue(), readCounts(r, 1));
                })
                .toList();
    }

    /**
     * One count row per (organization id, confirmed-diagnosis code) pair, with
     * no code filter — the raw material for the "Form 28.1 by territory" view,
     * where every disease is its own column against every territory row (unlike
     * {@link #countGroupedByOrganizationForCodes}, which restricts to a single
     * nosological form's code set).
     */
    public List<Form281OrganizationDiagnosisCountProjection> countGroupedByOrganizationAndDiagnosisCode(
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
                    return new Form281OrganizationDiagnosisCountProjection(
                            ((Number) r[0]).longValue(), (String) r[1], readCounts(r, 2)
                    );
                })
                .toList();
    }

    /** A single, unattributed total for one nosological form's ICD-10 set — a drill-down root node. */
    public Form281Counts countTotalForCodes(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive, Collection<String> codes
    ) {
        if (organizationIds == null || organizationIds.isEmpty() || codes == null || codes.isEmpty()) {
            return Form281Counts.EMPTY;
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

    private Form281Counts readCounts(Object[] row, int offset) {
        return new Form281Counts(
                count(row, offset),
                count(row, offset + 1),
                count(row, offset + 2),
                count(row, offset + 3),
                count(row, offset + 4),
                count(row, offset + 5),
                count(row, offset + 6),
                count(row, offset + 7),
                count(row, offset + 8),
                count(row, offset + 9),
                count(row, offset + 10),
                count(row, offset + 11),
                count(row, offset + 12)
        );
    }

    private long count(Object[] row, int index) {
        Object value = row[index];
        return value == null ? 0L : ((Number) value).longValue();
    }
}
