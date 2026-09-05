package uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCategoryCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsOrganizationCategoryCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Statistika" — native SQL aggregation across {@code form058} and
 * {@code form058_1}, grouped by {@code patient.category_code}, over an
 * arbitrary caller-supplied {@code [fromInclusive, toExclusive)} range.
 * Structurally the CONFIRMED/PRIMARY split of {@code Form1ReportRepository}
 * (same {@code status = 'APPROVED'} vs {@code status not in ('APPROVED',
 * 'CANCELED')} bucketing, same {@code VALUES}-list org-id join instead of
 * {@code IN}/{@code = any(...)} — see that class for the full rationale)
 * crossed with the category-column grouping of {@code
 * Form281ReportRepository}: every FILTER-based aggregate is computed by
 * Postgres in one pass per organization batch, and no case/patient row is
 * ever materialized in the JVM.
 * <p>
 * {@code category_code} is grouped as-is, including {@code null} (patients
 * with no category assigned) — the query service sums every group into the
 * node's overall total and keeps only the groups matching a known {@code
 * ref_catalog(type = 'CATEGORY')} code for the per-category breakdown, so a
 * caller never has to filter categories in SQL just to get an accurate grand
 * total. Age is {@code extract(year from age(f.created_at::date,
 * p.birth_date))} as of the case's own {@code created_at}, matching every
 * other report. Grouped/filtered by {@code sender_organization_id} — the
 * institution that <b>created</b> the case — see {@code
 * Form1ReportRepository} for why.
 * <p>
 * Every {@code ::type} cast wraps its named parameter in parentheses —
 * {@code (:param)::type} — see {@code Form1ReportRepository} for why.
 */
@Repository
@RequiredArgsConstructor
public class StatisticsReportRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select p.category_code as category_code, p.gender_code as gender_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int as age_years,
                   'CONFIRMED' as metric
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select p.category_code, p.gender_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int, 'PRIMARY'
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select p.category_code, p.gender_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int, 'CONFIRMED'
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select p.category_code, p.gender_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int, 'PRIMARY'
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    /** Same as {@link #UNION_SOURCE_TEMPLATE} but also keeps {@code sender_organization_id} for the org grouping. */
    private static final String UNION_SOURCE_WITH_ORG_TEMPLATE = """
            select f.sender_organization_id, p.category_code as category_code, p.gender_code as gender_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int as age_years,
                   'CONFIRMED' as metric
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select f.sender_organization_id, p.category_code, p.gender_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int, 'PRIMARY'
            from form058 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select f.sender_organization_id, p.category_code, p.gender_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int, 'CONFIRMED'
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select f.sender_organization_id, p.category_code, p.gender_code,
                   extract(year from age(f.created_at::date, p.birth_date))::int, 'PRIMARY'
            from form058_1 f
            join patient p on p.id = f.patient_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status not in ('APPROVED', 'CANCELED')
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    private static final String METRIC_COLUMNS = """
            count(*) filter (where t.metric = 'CONFIRMED')                              as confirmed_total,
            count(*) filter (where t.metric = 'CONFIRMED' and t.gender_code = 'FEMALE') as confirmed_female,
            count(*) filter (where t.metric = 'CONFIRMED' and t.gender_code = 'MALE')   as confirmed_male,
            count(*) filter (where t.metric = 'CONFIRMED' and t.age_years < 18)         as confirmed_under_18,
            count(*) filter (where t.metric = 'CONFIRMED' and t.age_years >= 18)        as confirmed_adult,
            count(*) filter (where t.metric = 'PRIMARY')                                as primary_total,
            count(*) filter (where t.metric = 'PRIMARY' and t.gender_code = 'FEMALE')   as primary_female,
            count(*) filter (where t.metric = 'PRIMARY' and t.gender_code = 'MALE')     as primary_male,
            count(*) filter (where t.metric = 'PRIMARY' and t.age_years < 18)           as primary_under_18,
            count(*) filter (where t.metric = 'PRIMARY' and t.age_years >= 18)          as primary_adult
            """;

    private final EntityManager entityManager;

    /**
     * One aggregate row per distinct {@code category_code} value (including
     * {@code null}) across the whole scope — the raw material for the
     * report's root node.
     */
    public List<StatisticsCategoryCountProjection> countByCategory(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.category_code as category_code, " + METRIC_COLUMNS
                + " from (" + unionSource(organizationIds) + ") t group by t.category_code";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive).getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new StatisticsCategoryCountProjection((String) r[0], readCounts(r, 1));
                })
                .toList();
    }

    /**
     * One aggregate row per (organization id, {@code category_code}) pair —
     * the raw material for the geography drill-down.
     */
    public List<StatisticsOrganizationCategoryCountProjection> countGroupedByOrganizationAndCategory(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.sender_organization_id as organization_id, t.category_code as category_code, "
                + METRIC_COLUMNS
                + " from (" + unionSourceWithOrg(organizationIds) + ") t"
                + " group by t.sender_organization_id, t.category_code";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive).getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new StatisticsOrganizationCategoryCountProjection(
                            ((Number) r[0]).longValue(), (String) r[1], readCounts(r, 2)
                    );
                })
                .toList();
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

    private StatisticsCounts readCounts(Object[] row, int offset) {
        return new StatisticsCounts(
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
