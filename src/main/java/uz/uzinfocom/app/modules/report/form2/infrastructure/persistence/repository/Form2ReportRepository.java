package uz.uzinfocom.app.modules.report.form2.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form2.application.query.dto.Form2OrganizationCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Form 2" — native SQL aggregation across {@code form058} and {@code
 * form058_1} over an arbitrary {@code [fromInclusive, toExclusive)} range,
 * with no diagnosis filtering. Mirrors {@code
 * Form1ReportRepository}: a single Criteria-API query can't span two
 * unrelated entity roots, so this UNIONs both tables' rows first — each
 * branch pre-filtered on sender/status/created_at so the planner can use
 * each table's own composite index ({@code idx_form058_outgoing_table},
 * {@code idx_form0581_sender_status_created}) to avoid a full scan — then
 * aggregates every bucket with Postgres {@code count(*) filter (where ...)}
 * in a single pass over the already-narrowed row set. No per-row data
 * (patient identity, diagnosis text, etc.) ever leaves the database — only
 * the final small numeric aggregate does, keeping this O(1) in JVM heap
 * regardless of how many cases matched.
 * <p>
 * Grouped/filtered by {@code sender_organization_id} — the institution that
 * <b>created</b> the case ("createdOrg") — not {@code
 * receiver_organization_id} (always the SANEPID_SERVICE branch that
 * processes the case, never the actual medical institution). See {@code
 * Form1ReportRepository} for the full rationale.
 * <p>
 * {@code organizationIds} is spliced into the SQL text as a literal {@code
 * VALUES} list joined against, not filtered via {@code IN}/{@code =
 * any(...)} and not bound as a query parameter — see {@code
 * Form1ReportRepository} for why (every earlier alternative either broke or
 * made Postgres treat "basically every organization" as if it were a
 * selective index condition, ~60x slower in testing).
 * <p>
 * Only PRIMARY (not yet resolved) notifications are counted here — {@code
 * status NOT IN ('APPROVED', 'CANCELED')}. Category buckets are {@code
 * patient.category_code} against the {@code ref_catalog} {@code CATEGORY}
 * type (see {@code Form2OrganizationCountProjection}).
 * <p>
 * Every {@code ::type} cast below wraps its named parameter in parentheses
 * — {@code (:param)::type}, never {@code :param::type} — see {@code
 * Form1ReportRepository} for why (Hibernate's named-parameter scanner reads
 * trailing {@code ::} as part of the parameter's own name otherwise).
 */
@Repository
@RequiredArgsConstructor
public class Form2ReportRepository {

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
     * Every PRIMARY (not yet resolved) form058/form058_1 notification
     * created in {@code [fromInclusive, toExclusive)} for the given
     * organizations — no diagnosis filtering, used by {@code
     * Form2ManualEntryQueryService} for the "Shakl №2" registered-case
     * counts.
     */
    private static final String UNION_SOURCE_TEMPLATE = """
            select f.sender_organization_id, p.category_code
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select f.sender_organization_id, p.category_code
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    private final EntityManager entityManager;

    /** A single, unattributed total row across every given organization id. */
    public Form2OrganizationCountProjection countTotal(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Form2OrganizationCountProjection.empty(null);
        }

        Query query = entityManager.createNativeQuery(totalSql(organizationIds))
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive);

        List<?> rows = query.getResultList();
        return rows.isEmpty()
                ? Form2OrganizationCountProjection.empty(null)
                : toProjection((Object[]) rows.getFirst(), false);
    }

    private String totalSql(List<Long> organizationIds) {
        return "select " + AGGREGATE_COLUMNS + " from (" + unionSource(organizationIds) + ") t";
    }

    private String unionSource(List<Long> organizationIds) {
        String valuesList = organizationIds.stream().map(id -> "(" + id + ")").collect(Collectors.joining(","));
        return UNION_SOURCE_TEMPLATE.formatted(valuesList);
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
