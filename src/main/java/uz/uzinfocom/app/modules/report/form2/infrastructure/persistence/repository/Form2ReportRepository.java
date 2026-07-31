package uz.uzinfocom.app.modules.report.form2.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form2.application.query.dto.Form2OrganizationCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "Form 2" — native SQL aggregation across {@code form058} and {@code
 * form058_1} for the social/occupation composition report, over an arbitrary
 * caller-supplied {@code [fromInclusive, toExclusive)} range. Mirrors {@code
 * Form1ReportRepository}: a single Criteria-API query can't span two
 * unrelated entity roots, so this UNIONs both tables' rows first — each
 * branch pre-filtered on sender/status/created_at so the planner can use
 * each table's own composite index ({@code idx_form058_outgoing_table},
 * {@code idx_form0581_sender_status_created}) to avoid a full scan — then
 * aggregates every bucket with Postgres {@code count(*) filter (where ...)}
 * in a single pass over the already-narrowed row set. No per-row data
 * (patient identity, diagnosis text, etc.) ever leaves the database — only
 * the final small numeric aggregate does, keeping this report O(1) in JVM
 * heap regardless of how many cases matched.
 * <p>
 * Grouped/filtered by {@code sender_organization_id} — the institution that
 * <b>created</b> the case ("createdOrg") — not {@code
 * receiver_organization_id} (always the SANEPID_SERVICE branch that
 * processes the case, never the actual medical institution), so the
 * district-level breakdown lists every institution (muassasa) with its own
 * count instead of only the SES branch. See {@code Form1ReportRepository}
 * for the full rationale.
 * <p>
 * {@code organizationIds} is spliced into the SQL text as a literal {@code
 * VALUES} list joined against, not filtered via {@code IN}/{@code =
 * any(...)} and not bound as a query parameter — see {@code
 * Form1ReportRepository} for why (every earlier alternative either broke or
 * made Postgres treat "basically every organization" as if it were a
 * selective index condition, ~60x slower in testing).
 * <p>
 * Only PRIMARY (not yet resolved) notifications are counted here — {@code
 * status NOT IN ('APPROVED', 'CANCELED')} — matching the report's own title
 * ("Birlamchi shoshilinch xabarnomalar bo'yicha ro'yxatga olingan bemorlar
 * soni"); unlike Form 1 there is no confirmed/primary split. Category
 * buckets are {@code patient.category_code} against the {@code ref_catalog}
 * {@code CATEGORY} type (see {@code Form2OrganizationCountProjection}).
 * <p>
 * Every {@code ::type} cast below wraps its named parameter in parentheses
 * — {@code (:param)::type}, never {@code :param::type} — see {@code
 * Form1ReportRepository} for why (Hibernate's named-parameter scanner reads
 * trailing {@code ::} as part of the parameter's own name otherwise).
 */
@Repository
@RequiredArgsConstructor
public class Form2ReportRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select f.sender_organization_id, p.category_code
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.mkb10_code = (:diagnosisCode)::text or f.final_mkb10_code = (:diagnosisCode)::text)
            union all
            select f.sender_organization_id, p.category_code
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and ((:diagnosisCode)::text is null or f.mkb10_code = (:diagnosisCode)::text or f.final_mkb10_code = (:diagnosisCode)::text)
            """;

    private static final String AGGREGATE_COLUMNS = """
            count(*)                                                       as total,
            count(*) filter (where t.category_code = 'NO_ORGANIZED')       as unorganized_preschool,
            count(*) filter (where t.category_code = 'ORGANIZED')          as organized_preschool,
            count(*) filter (where t.category_code = 'STUDENT_SCHOOL')     as school_students,
            count(*) filter (where t.category_code = 'MIDDLE_STUDENT')     as vocational_students,
            count(*) filter (where t.category_code = 'STUDENT')            as university_students,
            count(*) filter (where t.category_code = 'SEREVANTS')          as employees,
            count(*) filter (where t.category_code = 'WORKER')             as workers,
            count(*) filter (where t.category_code = 'MEDICAL_WORKER')     as medical_staff,
            count(*) filter (where t.category_code = 'NOT_EMPLOYED')       as unemployed
            """;

    /**
     * Same shape as {@link #UNION_SOURCE_TEMPLATE} but filters by a set of
     * MKB-10 codes ({@code in (:mkb10Codes)}) instead of a single optional
     * exact match — used by {@code Form2ManualEntryQueryService} to count
     * only the diagnoses belonging to whichever {@code ManualReport}
     * entries are tagged for "Shakl №2". A plain {@code in (:param)} bind is
     * fine here (unlike {@code organizationIds} above): the set is a
     * handful of disease codes, never anywhere near the ~1000-row scale
     * that made {@code = any(...)}/{@code IN} problematic for organization
     * ids.
     */
    private static final String UNION_SOURCE_MKB10_TEMPLATE = """
            select f.sender_organization_id, p.category_code
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and (f.mkb10_code in (:mkb10Codes) or f.final_mkb10_code in (:mkb10Codes))
            union all
            select f.sender_organization_id, p.category_code
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
              and (f.mkb10_code in (:mkb10Codes) or f.final_mkb10_code in (:mkb10Codes))
            """;

    private final EntityManager entityManager;

    /**
     * A single, unattributed total row across every given organization id,
     * restricted to cases whose diagnosis is in {@code mkb10Codes} — an
     * empty/null code set means "nothing to count" (zero), not "no
     * filter", since an empty set here means no {@code ManualReport} is
     * configured for the caller yet.
     */
    public Form2OrganizationCountProjection countTotalByMkb10Codes(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            Set<String> mkb10Codes
    ) {
        if (organizationIds == null || organizationIds.isEmpty() || mkb10Codes == null || mkb10Codes.isEmpty()) {
            return Form2OrganizationCountProjection.empty(null);
        }

        Query query = entityManager.createNativeQuery(totalMkb10Sql(organizationIds))
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive)
                .setParameter("mkb10Codes", mkb10Codes);

        List<?> rows = query.getResultList();
        return rows.isEmpty()
                ? Form2OrganizationCountProjection.empty(null)
                : toProjection((Object[]) rows.get(0), false);
    }

    private String totalMkb10Sql(List<Long> organizationIds) {
        return "select " + AGGREGATE_COLUMNS + " from (" + unionMkb10Source(organizationIds) + ") t";
    }

    private String unionMkb10Source(List<Long> organizationIds) {
        String valuesList = organizationIds.stream().map(id -> "(" + id + ")").collect(Collectors.joining(","));
        return UNION_SOURCE_MKB10_TEMPLATE.formatted(valuesList);
    }

    /** One aggregate row per organization id — for a region/district/organization-level breakdown. */
    public List<Form2OrganizationCountProjection> countGroupedByOrganization(
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
     * for the report's root node.
     */
    public Form2OrganizationCountProjection countTotal(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive,
            String diagnosisCode
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Form2OrganizationCountProjection.empty(null);
        }

        Query query = bindParameters(
                entityManager.createNativeQuery(totalSql(organizationIds)), fromInclusive, toExclusive, diagnosisCode
        );

        List<?> rows = query.getResultList();
        return rows.isEmpty()
                ? Form2OrganizationCountProjection.empty(null)
                : toProjection((Object[]) rows.get(0), false);
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

    private Form2OrganizationCountProjection toProjection(Object[] row, boolean hasOrganizationId) {
        int offset = hasOrganizationId ? 1 : 0;
        Long organizationId = hasOrganizationId ? ((Number) row[0]).longValue() : null;

        return new Form2OrganizationCountProjection(
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
                count(row, offset + 9)
        );
    }

    private long count(Object[] row, int index) {
        Object value = row[index];
        return value == null ? 0L : ((Number) value).longValue();
    }
}
