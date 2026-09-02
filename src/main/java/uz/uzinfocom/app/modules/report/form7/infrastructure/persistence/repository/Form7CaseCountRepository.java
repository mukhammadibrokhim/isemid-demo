package uz.uzinfocom.app.modules.report.form7.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.form7.application.query.dto.Form7CaseCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Shakl №7" — auxiliary ("yordamchi") native SQL aggregation across {@code
 * form058} and {@code form058_1} for the auto-computed part of a Form 7
 * entry: the "Hisobot davrida ro'yxatga olingan bemorlar" age/gender cuts
 * and "Birlamchi tashxis tasdiqlandi". Mirrors {@code Form1ReportRepository}
 * /{@code Form2ReportRepository}: same {@code VALUES}-list org-id join
 * instead of {@code IN}/{@code = any(...)} (see {@code Form1ReportRepository}
 * for the rationale), same {@code (:param)::type} cast style, same {@code
 * extract(year from age(f.created_at::date, p.birth_date))::int} age
 * calculation, grouped/filtered by {@code sender_organization_id} — the
 * institution that <b>created</b> the case.
 * <p>
 * "Registered" = every form058/form058_1 notification created by the
 * organization in {@code [fromInclusive, toExclusive)} that is not deleted
 * and not {@code CANCELED} (a form058 rejected by the receiver is stored as
 * {@code CANCELED}). "Birlamchi tashxis tasdiqlandi" = that subset with
 * {@code status = 'APPROVED'}. No diagnosis filtering.
 */
@Repository
@RequiredArgsConstructor
public class Form7CaseCountRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select extract(year from age(f.created_at::date, p.birth_date))::int as age_years,
                   p.gender_code,
                   (f.status = 'APPROVED')                                       as confirmed
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status <> 'CANCELED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select extract(year from age(f.created_at::date, p.birth_date))::int,
                   p.gender_code,
                   (f.status = 'APPROVED')
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status <> 'CANCELED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    private static final String AGGREGATE_COLUMNS = """
            count(*)                                                        as total,
            count(*) filter (where t.age_years < 14)                        as under_14,
            count(*) filter (where t.age_years < 18)                        as under_18,
            count(*) filter (where t.age_years >= 18)                       as adult,
            count(*) filter (where t.gender_code = 'FEMALE')                as female,
            count(*) filter (where t.confirmed)                             as primary_diagnosis_confirmed
            """;

    private final EntityManager entityManager;

    /**
     * A single, unattributed count block across every given organization id,
     * for a single organization + period. {@code organizationIds} is
     * normally a singleton (the caller's own organization) but the shape
     * matches the other report repositories.
     */
    public Form7CaseCountProjection countBlock(
            List<Long> organizationIds,
            Instant fromInclusive,
            Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Form7CaseCountProjection.EMPTY;
        }

        String sql = "select " + AGGREGATE_COLUMNS + " from (" + unionSource(organizationIds) + ") t";

        Query query = entityManager.createNativeQuery(sql)
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive);

        List<?> rows = query.getResultList();
        return rows.isEmpty() ? Form7CaseCountProjection.EMPTY : toProjection((Object[]) rows.getFirst());
    }

    private String unionSource(List<Long> organizationIds) {
        String valuesList = organizationIds.stream().map(id -> "(" + id + ")").collect(Collectors.joining(","));
        return UNION_SOURCE_TEMPLATE.formatted(valuesList);
    }

    private Form7CaseCountProjection toProjection(Object[] row) {
        return new Form7CaseCountProjection(
                count(row, 0), count(row, 1), count(row, 2), count(row, 3), count(row, 4), count(row, 5)
        );
    }

    private long count(Object[] row, int index) {
        Object value = row[index];
        return value == null ? 0L : ((Number) value).longValue();
    }
}
