package uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCardStatusCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsOrganizationCardStatusCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Statistika"'s card breakdown — native SQL aggregation over {@code
 * card}, joined to whichever of {@code form058}/{@code form058_1} it belongs
 * to (a card carries exactly one of {@code form058_id}/{@code
 * form058_1_id} — see {@code Card}) purely to reach that case's {@code
 * sender_organization_id}, over an arbitrary caller-supplied {@code
 * [fromInclusive, toExclusive)} range on the <b>card's own</b> {@code
 * created_at} (not the case's) — matching {@code CardStatsRepository}'s
 * dashboard aggregation.
 * <p>
 * Deliberately a separate, org-id-list-based repository rather than reusing
 * {@code CardStatsRepository}: that one is scope-object-based ({@code
 * ResolvedOrganizationScope}) for the caller's own whole access scope, while
 * {@code ReportHierarchyService} needs the same {@code VALUES}-list org-id
 * join every other report repository uses (see {@code
 * Form1ReportRepository}) to aggregate one specific geography node's
 * organizations at a time.
 */
@Repository
@RequiredArgsConstructor
public class StatisticsCardRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select c.status as status
            from card c
            join form058 f on f.id = c.form058_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where c.deleted = false
              and c.created_at >= (:fromInclusive)::timestamptz and c.created_at < (:toExclusive)::timestamptz
            union all
            select c.status
            from card c
            join form058_1 f on f.id = c.form058_1_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where c.deleted = false
              and c.created_at >= (:fromInclusive)::timestamptz and c.created_at < (:toExclusive)::timestamptz
            """;

    private static final String UNION_SOURCE_WITH_ORG_TEMPLATE = """
            select f.sender_organization_id, c.status as status
            from card c
            join form058 f on f.id = c.form058_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where c.deleted = false
              and c.created_at >= (:fromInclusive)::timestamptz and c.created_at < (:toExclusive)::timestamptz
            union all
            select f.sender_organization_id, c.status
            from card c
            join form058_1 f on f.id = c.form058_1_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where c.deleted = false
              and c.created_at >= (:fromInclusive)::timestamptz and c.created_at < (:toExclusive)::timestamptz
            """;

    private final EntityManager entityManager;

    /** One aggregate row per {@link CardStatus} across the whole scope — the report's root node. */
    public List<StatisticsCardStatusCountProjection> countByStatus(
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
                    return new StatisticsCardStatusCountProjection(CardStatus.valueOf((String) r[0]), ((Number) r[1]).longValue());
                })
                .toList();
    }

    /** One aggregate row per (organization id, {@link CardStatus}) pair — the geography drill-down. */
    public List<StatisticsOrganizationCardStatusCountProjection> countGroupedByOrganizationAndStatus(
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
                    return new StatisticsOrganizationCardStatusCountProjection(
                            ((Number) r[0]).longValue(), CardStatus.valueOf((String) r[1]), ((Number) r[2]).longValue()
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
