package uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsActStatusCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsOrganizationActStatusCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Statistika"'s act breakdown — native SQL aggregation over {@code
 * act} joined to its owning {@code card} then to whichever of {@code
 * form058}/{@code form058_1} that card belongs to, purely to reach that
 * case's {@code sender_organization_id}, over an arbitrary caller-supplied
 * {@code [fromInclusive, toExclusive)} range on the <b>act's own</b> {@code
 * created_at} — matching {@code ActStatsRepository}'s dashboard
 * aggregation. See {@code StatisticsCardRepository} for why this is a
 * separate, org-id-list-based repository rather than a reuse of {@code
 * ActStatsRepository}.
 */
@Repository
@RequiredArgsConstructor
public class StatisticsActRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select a.act_status as status
            from act a
            join card c on c.id = a.card_id
            join form058 f on f.id = c.form058_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where a.deleted = false and c.deleted = false
              and a.created_at >= (:fromInclusive)::timestamptz and a.created_at < (:toExclusive)::timestamptz
            union all
            select a.act_status
            from act a
            join card c on c.id = a.card_id
            join form058_1 f on f.id = c.form058_1_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where a.deleted = false and c.deleted = false
              and a.created_at >= (:fromInclusive)::timestamptz and a.created_at < (:toExclusive)::timestamptz
            """;

    private static final String UNION_SOURCE_WITH_ORG_TEMPLATE = """
            select f.sender_organization_id, a.act_status as status
            from act a
            join card c on c.id = a.card_id
            join form058 f on f.id = c.form058_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where a.deleted = false and c.deleted = false
              and a.created_at >= (:fromInclusive)::timestamptz and a.created_at < (:toExclusive)::timestamptz
            union all
            select f.sender_organization_id, a.act_status
            from act a
            join card c on c.id = a.card_id
            join form058_1 f on f.id = c.form058_1_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where a.deleted = false and c.deleted = false
              and a.created_at >= (:fromInclusive)::timestamptz and a.created_at < (:toExclusive)::timestamptz
            """;

    private final EntityManager entityManager;

    /** One aggregate row per {@link ActStatus} across the whole scope — the report's root node. */
    public List<StatisticsActStatusCountProjection> countByStatus(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.status as status, count(*) as total"
                + " from (" + unionSource(organizationIds) + ") t group by t.status";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive).getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new StatisticsActStatusCountProjection(ActStatus.valueOf((String) r[0]), ((Number) r[1]).longValue());
                })
                .toList();
    }

    /** One aggregate row per (organization id, {@link ActStatus}) pair — the geography drill-down. */
    public List<StatisticsOrganizationActStatusCountProjection> countGroupedByOrganizationAndStatus(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.sender_organization_id as organization_id, t.status as status, count(*) as total"
                + " from (" + unionSourceWithOrg(organizationIds) + ") t"
                + " group by t.sender_organization_id, t.status";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive).getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new StatisticsOrganizationActStatusCountProjection(
                            ((Number) r[0]).longValue(), ActStatus.valueOf((String) r[1]), ((Number) r[2]).longValue()
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
}
