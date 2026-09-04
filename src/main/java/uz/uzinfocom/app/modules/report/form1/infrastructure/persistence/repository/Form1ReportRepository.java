package uz.uzinfocom.app.modules.report.form1.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form1.application.query.dto.Form1OrganizationCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Form 1" — native SQL aggregation across {@code form058} and {@code
 * form058_1} for the infectious/parasitic disease monitoring report, over an
 * arbitrary caller-supplied {@code [fromInclusive, toExclusive)} range. A
 * single Criteria-API query can't span two unrelated entity roots, so this
 * UNIONs both tables' relevant rows first — each branch pre-filtered on
 * sender/status/created_at so the planner can still use each table's own
 * composite index — then aggregates with Postgres {@code FILTER (WHERE
 * ...)}. No conditional counting happens on the Java side; every number the
 * caller sees was already computed by the database, and no case/patient row
 * is ever materialized in the JVM — only the final small numeric aggregate
 * per organization.
 * <p>
 * Grouped/filtered by {@code sender_organization_id} — the institution that
 * <b>created</b> the case (form058/form058_1's "createdOrg") — not {@code
 * receiver_organization_id}. The receiver is always the SANEPID_SERVICE
 * branch that processes the case, so grouping by receiver can never produce
 * a meaningful per-institution breakdown (every hospital/clinic would show
 * zero — only the SES branch itself would have a non-zero row). Grouping by
 * sender is what makes the district-level breakdown list every actual
 * medical institution (muassasa) with its own count, and it still rolls up
 * correctly into region/district totals because every {@code Organization}
 * row (hospital or SES alike) carries its own {@code region_code}/{@code
 * district_code} — see {@code ReportHierarchyService}. Indexes used:
 * {@code idx_form058_outgoing_table} (sender_organization_id, status,
 * created_at, id) and {@code idx_form0581_sender_status_created}
 * (sender_organization_id, status, created_at).
 * <p>
 * Age is computed as of the case's own {@code created_at} date from {@code
 * patient.birth_date} — {@code extract(year from age(f.created_at::date,
 * p.birth_date))} — rather than trusting the patient's stored {@code
 * age_years} column, which reflects whatever age was last entered for that
 * patient, not their age at the time this specific case was recorded.
 * Postgres's {@code age()} function counts complete calendar years exactly
 * the way the report defines "under 14"/"under 18" (e.g. 13 years, 11
 * months, 29 days is still under 14).
 * <p>
 * {@code organizationIds} is spliced into the SQL text as a literal {@code
 * VALUES} list joined against — {@code join (values (6),(7),...) as
 * scope_org(id) on scope_org.id = f.sender_organization_id} — not bound as a
 * query parameter, and not filtered via {@code IN}/{@code = any(...)}
 * either. Every element is a {@code Long} produced by our own scope
 * resolution, never user-supplied text, so there is nothing to inject.
 * Three earlier approaches were tried and rejected for a republic-wide
 * caller's list (~1000 organizations, ~95% of the whole table): a Hibernate
 * {@code IN (?,?,?...)} parameter expansion made Postgres unable to
 * type-infer some of the resulting thousands of placeholders; {@code =
 * any(:param)} array binds either silently matched nothing (a plain {@code
 * Long[]}, wrong inferred element type) or failed outright ({@code
 * java.sql.Array}, "No JDBC mapping could be inferred"); and even a
 * correctly-typed inlined {@code sender_organization_id in (id1,id2,...)}
 * or {@code = any('{id1,id2,...}'::bigint[])} condition made Postgres
 * choose an index scan over ~85% of the table (necessarily almost as slow
 * as scanning it outright) instead of recognizing this as "basically
 * everyone." Joining against a tiny {@code VALUES} list instead lets the
 * planner do what it already does well — hash the small side, scan the
 * big table once — cutting this query from ~6s to ~0.1s in testing against
 * ~600k rows.
 * <p>
 * The two "blocks" this report needs, confirmed by the report's author:
 * CONFIRMED = {@code status = 'APPROVED'}, PRIMARY = {@code status NOT IN
 * ('APPROVED', 'CANCELED')} — both bucketed by {@code created_at}, not by
 * any diagnosis/report date field (those differ in shape between form058
 * and form058_1, created_at does not). A form058 rejected by the receiver
 * is stored as {@code CANCELED} (see {@code FormStatus}), so it is already
 * excluded here — there is no separate "rejected" status to account for.
 * <p>
 * The optional {@code diagnosisCode} filter is matched differently per block:
 * the CONFIRMED block matches the <b>final</b> code alone ({@code
 * final_icd10_code = :diagnosisCode}) — every confirmed-only report does, a
 * confirmed case is defined by its final diagnosis — while the PRIMARY block,
 * whose cases usually have no final code yet, still matches "initial OR final".
 * <p>
 * Every {@code ::type} cast below wraps its named parameter in parentheses —
 * {@code (:param)::type}, never {@code :param::type} — because Hibernate's
 * named-parameter scanner reads trailing {@code ::} as part of the
 * parameter's own name otherwise. The parentheses give the scanner a hard
 * stop before the cast.
 */
@Repository
@RequiredArgsConstructor
public class Form1ReportRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select f.sender_organization_id,
                   extract(year from age(f.created_at::date, p.birth_date))::int as age_years,
                   p.gender_code,
                   (f.final_icd10_code is not null and f.final_icd10_code <> f.icd10_code) as diagnosis_changed,
                   'CONFIRMED' as metric
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.final_icd10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id,
                   extract(year from age(f.created_at::date, p.birth_date))::int,
                   p.gender_code, false, 'PRIMARY'
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id,
                   extract(year from age(f.created_at::date, p.birth_date))::int,
                   p.gender_code,
                   (f.final_icd10_code is not null and f.final_icd10_code <> f.icd10_code), 'CONFIRMED'
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.final_icd10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id,
                   extract(year from age(f.created_at::date, p.birth_date))::int,
                   p.gender_code, false, 'PRIMARY'
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.icd10_code = (:diagnosisCode)::text or f.final_icd10_code = (:diagnosisCode)::text)
            """;

    private static final String AGGREGATE_COLUMNS = """
            count(*) filter (where t.metric = 'CONFIRMED')                              as confirmed_total,
            count(*) filter (where t.metric = 'CONFIRMED' and t.age_years < 14)         as confirmed_under_14,
            count(*) filter (where t.metric = 'CONFIRMED' and t.age_years < 18)         as confirmed_under_18,
            count(*) filter (where t.metric = 'CONFIRMED' and t.age_years >= 18)        as confirmed_adult,
            count(*) filter (where t.metric = 'CONFIRMED' and t.gender_code = 'FEMALE') as confirmed_female,
            count(*) filter (where t.metric = 'CONFIRMED' and t.diagnosis_changed)      as confirmed_diagnosis_changed,
            count(*) filter (where t.metric = 'PRIMARY')                                as primary_total,
            count(*) filter (where t.metric = 'PRIMARY' and t.age_years < 14)           as primary_under_14,
            count(*) filter (where t.metric = 'PRIMARY' and t.age_years < 18)           as primary_under_18,
            count(*) filter (where t.metric = 'PRIMARY' and t.age_years >= 18)          as primary_adult,
            count(*) filter (where t.metric = 'PRIMARY' and t.gender_code = 'FEMALE')   as primary_female
            """;

    private final EntityManager entityManager;

    /** One aggregate row per organization id — for a region/district/organization-level breakdown. */
    public List<Form1OrganizationCountProjection> countGroupedByOrganization(
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
     * breakdown (potentially ~1000 rows countrywide) just to sum it in Java.
     */
    public Form1OrganizationCountProjection countTotal(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Form1OrganizationCountProjection.empty(null);
        }

        Query query = bindParameters(
                entityManager.createNativeQuery(totalSql(organizationIds)), fromInclusive, toExclusive, diagnosisCode
        );

        List<?> rows = query.getResultList();
        return rows.isEmpty()
                ? Form1OrganizationCountProjection.empty(null)
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

    private Form1OrganizationCountProjection toProjection(Object[] row, boolean hasOrganizationId) {
        int offset = hasOrganizationId ? 1 : 0;
        Long organizationId = hasOrganizationId ? ((Number) row[0]).longValue() : null;

        return new Form1OrganizationCountProjection(
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
                count(row, offset + 10)
        );
    }

    private long count(Object[] row, int index) {
        Object value = row[index];
        return value == null ? 0L : ((Number) value).longValue();
    }
}
