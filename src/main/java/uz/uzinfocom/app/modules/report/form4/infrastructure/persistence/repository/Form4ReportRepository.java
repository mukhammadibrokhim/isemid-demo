package uz.uzinfocom.app.modules.report.form4.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form4.application.query.dto.Form4OrganizationCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Form 4" — native SQL aggregation across {@code form058} and {@code
 * form058_1} for the social/occupation composition report, over an
 * arbitrary caller-supplied {@code [fromInclusive, toExclusive)} range.
 * Mirrors {@code Form1ReportRepository}: a single Criteria-API query can't
 * span two unrelated entity roots, so this UNIONs both tables' relevant rows
 * first — each branch pre-filtered on sender/status/created_at so the
 * planner can still use each table's own composite index ({@code
 * idx_form058_outgoing_table}, {@code idx_form0581_sender_status_created})
 * — then aggregates with Postgres {@code count(*) filter (where ...)}. No
 * per-row data ever leaves the database — only the final small numeric
 * aggregate per organization does.
 * <p>
 * Grouped/filtered by {@code sender_organization_id} — the institution that
 * <b>created</b> the case — not {@code receiver_organization_id} — see
 * {@code Form1ReportRepository} for the full rationale.
 * <p>
 * {@code organizationIds} is spliced into the SQL text as a literal {@code
 * VALUES} list joined against, not filtered via {@code IN}/{@code =
 * any(...)} and not bound as a query parameter — see {@code
 * Form1ReportRepository} for why (every earlier alternative either broke or
 * made Postgres treat "basically every organization" as if it were a
 * selective index condition).
 * <p>
 * The two "blocks" this report needs, same as Form 1: CONFIRMED = {@code
 * status = 'APPROVED'}, PRIMARY = {@code status NOT IN ('APPROVED',
 * 'CANCELED')} — both bucketed by {@code created_at}. A form058 rejected by
 * the receiver is stored as {@code CANCELED} (see {@code FormStatus}), so
 * it is already excluded here — there is no separate "rejected" status to
 * account for. Category buckets are {@code patient.category_code} against
 * the {@code ref_catalog} {@code CATEGORY} type (see
 * {@code Form4OrganizationCountProjection}); the seeded {@code TEACHER}
 * category is not broken out but still counts toward each metric's total.
 * <p>
 * Every {@code ::type} cast below wraps its named parameter in parentheses
 * — {@code (:param)::type}, never {@code :param::type} — see {@code
 * Form1ReportRepository} for why (Hibernate's named-parameter scanner reads
 * trailing {@code ::} as part of the parameter's own name otherwise).
 */
@Repository
@RequiredArgsConstructor
public class Form4ReportRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select f.sender_organization_id, p.category_code, 'CONFIRMED' as metric
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id, p.category_code, 'PRIMARY'
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id, p.category_code, 'CONFIRMED'
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id, p.category_code, 'PRIMARY'
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            """;

    private static final String AGGREGATE_COLUMNS = """
            count(*) filter (where t.metric = 'CONFIRMED')                                          as confirmed_total,
            count(*) filter (where t.metric = 'CONFIRMED' and t.category_code = 'NO_ORGANIZED')      as confirmed_unorganized_preschool,
            count(*) filter (where t.metric = 'CONFIRMED' and t.category_code = 'ORGANIZED')         as confirmed_organized_preschool,
            count(*) filter (where t.metric = 'CONFIRMED' and t.category_code = 'STUDENT_SCHOOL')    as confirmed_school_students,
            count(*) filter (where t.metric = 'CONFIRMED' and t.category_code = 'MIDDLE_STUDENT')    as confirmed_vocational_students,
            count(*) filter (where t.metric = 'CONFIRMED' and t.category_code = 'STUDENT')           as confirmed_university_students,
            count(*) filter (where t.metric = 'CONFIRMED' and t.category_code = 'SEREVANTS')         as confirmed_employees,
            count(*) filter (where t.metric = 'CONFIRMED' and t.category_code = 'WORKER')            as confirmed_workers,
            count(*) filter (where t.metric = 'CONFIRMED' and t.category_code = 'MEDICAL_WORKER')    as confirmed_medical_staff,
            count(*) filter (where t.metric = 'CONFIRMED' and t.category_code = 'NOT_EMPLOYED')      as confirmed_unemployed,
            count(*) filter (where t.metric = 'CONFIRMED' and t.category_code = 'PENSIONER')         as confirmed_pensioners,
            count(*) filter (where t.metric = 'CONFIRMED' and t.category_code = 'UNSHELTRED')        as confirmed_unsheltered,
            count(*) filter (where t.metric = 'PRIMARY')                                             as primary_total,
            count(*) filter (where t.metric = 'PRIMARY' and t.category_code = 'NO_ORGANIZED')        as primary_unorganized_preschool,
            count(*) filter (where t.metric = 'PRIMARY' and t.category_code = 'ORGANIZED')           as primary_organized_preschool,
            count(*) filter (where t.metric = 'PRIMARY' and t.category_code = 'STUDENT_SCHOOL')      as primary_school_students,
            count(*) filter (where t.metric = 'PRIMARY' and t.category_code = 'MIDDLE_STUDENT')      as primary_vocational_students,
            count(*) filter (where t.metric = 'PRIMARY' and t.category_code = 'STUDENT')             as primary_university_students,
            count(*) filter (where t.metric = 'PRIMARY' and t.category_code = 'SEREVANTS')           as primary_employees,
            count(*) filter (where t.metric = 'PRIMARY' and t.category_code = 'WORKER')              as primary_workers,
            count(*) filter (where t.metric = 'PRIMARY' and t.category_code = 'MEDICAL_WORKER')      as primary_medical_staff,
            count(*) filter (where t.metric = 'PRIMARY' and t.category_code = 'NOT_EMPLOYED')        as primary_unemployed,
            count(*) filter (where t.metric = 'PRIMARY' and t.category_code = 'PENSIONER')           as primary_pensioners,
            count(*) filter (where t.metric = 'PRIMARY' and t.category_code = 'UNSHELTRED')          as primary_unsheltered
            """;

    private final EntityManager entityManager;

    /** One aggregate row per organization id — for a region/district/organization-level breakdown. */
    public List<Form4OrganizationCountProjection> countGroupedByOrganization(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        Query query = bindParameters(
                entityManager.createNativeQuery(groupedSql(organizationIds)), fromInclusive, toExclusive, diagnosisCode
        );

        List<?> rows = query.getResultList();
        return rows.stream().map(row -> toProjection((Object[]) row, true)).toList();
    }

    /**
     * A single, unattributed total row across every given organization id —
     * for the report's root node, avoiding fetching a per-organization
     * breakdown just to sum it in Java.
     */
    public Form4OrganizationCountProjection countTotal(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Form4OrganizationCountProjection.empty(null);
        }

        Query query = bindParameters(
                entityManager.createNativeQuery(totalSql(organizationIds)), fromInclusive, toExclusive, diagnosisCode
        );

        List<?> rows = query.getResultList();
        return rows.isEmpty()
                ? Form4OrganizationCountProjection.empty(null)
                : toProjection((Object[]) rows.getFirst(), false);
    }

    private String unionSource(List<Long> organizationIds) {
        String valuesList = organizationIds.stream().map(id -> "(" + id + ")").collect(Collectors.joining(","));
        return UNION_SOURCE_TEMPLATE.formatted(valuesList);
    }

    private String groupedSql(List<Long> organizationIds) {
        return "select t.sender_organization_id as organization_id, " + AGGREGATE_COLUMNS
                + " from (" + unionSource(organizationIds) + ") t group by t.sender_organization_id";
    }

    private String totalSql(List<Long> organizationIds) {
        return "select " + AGGREGATE_COLUMNS + " from (" + unionSource(organizationIds) + ") t";
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

    private Form4OrganizationCountProjection toProjection(Object[] row, boolean hasOrganizationId) {
        int offset = hasOrganizationId ? 1 : 0;
        Long organizationId = hasOrganizationId ? ((Number) row[0]).longValue() : null;

        return new Form4OrganizationCountProjection(
                organizationId,
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
                count(row, offset + 12),
                count(row, offset + 13),
                count(row, offset + 14),
                count(row, offset + 15),
                count(row, offset + 16),
                count(row, offset + 17),
                count(row, offset + 18),
                count(row, offset + 19),
                count(row, offset + 20),
                count(row, offset + 21),
                count(row, offset + 22),
                count(row, offset + 23)
        );
    }

    private long count(Object[] row, int index) {
        Object value = row[index];
        return value == null ? 0L : ((Number) value).longValue();
    }
}
