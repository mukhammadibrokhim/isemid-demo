package uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsForm129CountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsOrganizationForm129CountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Statistika"'s form 129 block — native SQL aggregation over {@code
 * form_129}, joined to {@code patient} for the age/gender/category cuts and
 * scoped by {@code sender_organization_id} via the same {@code VALUES}-list
 * org-id join every other report repository uses (see {@code
 * Form1ReportRepository}). Range is on {@code form_129.created_at}.
 * <p>
 * Unlike form058/form058_1, {@code form_129} has <b>no {@code deleted}
 * column</b> (its lifecycle is create → accept/reject, a rejected form is
 * {@code status = 'CANCELED'}) — there is no soft-delete filter here, and
 * every status (including {@code CANCELED}) is counted and bucketed. Form 129
 * also has no confirmed/primary split and never carries cards or acts.
 * <p>
 * Each result row is one ({@code category_code}, {@link Form129Status}) cell
 * with its own gender and 18-year age FILTER aggregates, so the query service
 * sums over categories for the by-status breakdown, over statuses for the
 * by-category breakdown, and over both for the block total and the age/gender
 * cuts.
 */
@Repository
@RequiredArgsConstructor
public class StatisticsForm129Repository {

    private static final String SOURCE_TEMPLATE = """
            select f.status as status, p.category_code as category_code, p.gender_code as gender_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int as age_years
            from form_129 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    private static final String SOURCE_WITH_ORG_TEMPLATE = """
            select f.sender_organization_id, f.status as status, p.category_code as category_code,
                   p.gender_code as gender_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int as age_years
            from form_129 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    private static final String METRIC_COLUMNS = """
            count(*)                                            as total,
            count(*) filter (where t.gender_code = 'FEMALE')    as female,
            count(*) filter (where t.gender_code = 'MALE')      as male,
            count(*) filter (where t.age_years < 18)            as under_18,
            count(*) filter (where t.age_years >= 18)           as adult
            """;

    private final EntityManager entityManager;

    /** One aggregate row per ({@code category_code}, {@link Form129Status}) across the whole scope — the report's root node. */
    public List<StatisticsForm129CountProjection> countByCategoryAndStatus(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.category_code as category_code, t.status as status, " + METRIC_COLUMNS
                + " from (" + source(organizationIds) + ") t group by t.category_code, t.status";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive).getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new StatisticsForm129CountProjection(
                            (String) r[0], Form129Status.valueOf((String) r[1]),
                            count(r, 2), count(r, 3), count(r, 4), count(r, 5), count(r, 6)
                    );
                })
                .toList();
    }

    /** One aggregate row per (organization id, {@code category_code}, {@link Form129Status}) — the geography drill-down. */
    public List<StatisticsOrganizationForm129CountProjection> countGroupedByOrganization(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.sender_organization_id as organization_id, t.category_code as category_code, "
                + "t.status as status, " + METRIC_COLUMNS
                + " from (" + sourceWithOrg(organizationIds) + ") t"
                + " group by t.sender_organization_id, t.category_code, t.status";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive).getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new StatisticsOrganizationForm129CountProjection(
                            ((Number) r[0]).longValue(), (String) r[1], Form129Status.valueOf((String) r[2]),
                            count(r, 3), count(r, 4), count(r, 5), count(r, 6), count(r, 7)
                    );
                })
                .toList();
    }

    private String source(List<Long> organizationIds) {
        return SOURCE_TEMPLATE.formatted(valuesList(organizationIds));
    }

    private String sourceWithOrg(List<Long> organizationIds) {
        return SOURCE_WITH_ORG_TEMPLATE.formatted(valuesList(organizationIds));
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
