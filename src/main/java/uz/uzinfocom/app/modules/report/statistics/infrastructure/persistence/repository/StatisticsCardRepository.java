package uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCardStatusCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsFormType;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsOrganizationCardStatusCountProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * "Statistika"'s card breakdown — native SQL aggregation over {@code card},
 * joined to whichever of {@code form058}/{@code form058_1} it belongs to (a
 * card carries exactly one of {@code form058_id}/{@code form058_1_id} — see
 * {@code Card}) both to reach that case's {@code sender_organization_id} and
 * to tag the row with its {@code form_type} ({@code 'FORM058'} / {@code
 * 'FORM0581'}), over an arbitrary caller-supplied {@code [fromInclusive,
 * toExclusive)} range on the <b>card's own</b> {@code created_at} (not the
 * case's) — matching {@code CardStatsRepository}'s dashboard aggregation.
 * <p>
 * Each result row is one (form, {@link CardStatus}, {@link CardType}) cell: a
 * card has exactly one status and one type, so the query service sums over
 * types for the by-status breakdown, over statuses for the by-type breakdown,
 * and over both for the block total.
 * <p>
 * Deliberately a separate, org-id-list-based repository rather than reusing
 * {@code CardStatsRepository}: that one is scope-object-based ({@code
 * ResolvedOrganizationScope}) for the caller's own whole access scope, while
 * {@code ReportHierarchyService} needs the same {@code VALUES}-list org-id
 * join every other report repository uses (see {@code Form1ReportRepository})
 * to aggregate one specific geography node's organizations at a time.
 */
@Repository
@RequiredArgsConstructor
public class StatisticsCardRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select 'FORM058'::text as form_type, c.status as status, c.card_type as card_type
            from card c
            join form058 f on f.id = c.form058_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where c.deleted = false
              and c.created_at >= (:fromInclusive)::timestamptz and c.created_at < (:toExclusive)::timestamptz
            union all
            select 'FORM0581'::text, c.status, c.card_type
            from card c
            join form058_1 f on f.id = c.form058_1_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where c.deleted = false
              and c.created_at >= (:fromInclusive)::timestamptz and c.created_at < (:toExclusive)::timestamptz
            """;

    private static final String UNION_SOURCE_WITH_ORG_TEMPLATE = """
            select f.sender_organization_id, 'FORM058'::text as form_type, c.status as status, c.card_type as card_type
            from card c
            join form058 f on f.id = c.form058_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where c.deleted = false
              and c.created_at >= (:fromInclusive)::timestamptz and c.created_at < (:toExclusive)::timestamptz
            union all
            select f.sender_organization_id, 'FORM0581'::text, c.status, c.card_type
            from card c
            join form058_1 f on f.id = c.form058_1_id
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where c.deleted = false
              and c.created_at >= (:fromInclusive)::timestamptz and c.created_at < (:toExclusive)::timestamptz
            """;

    private final EntityManager entityManager;

    /** One aggregate row per (form, {@link CardStatus}, {@link CardType}) across the whole scope — the report's root node. */
    public List<StatisticsCardStatusCountProjection> countCells(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.form_type as form_type, t.status as status, t.card_type as card_type, count(*) as total"
                + " from (" + unionSource(organizationIds) + ") t group by t.form_type, t.status, t.card_type";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive).getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new StatisticsCardStatusCountProjection(
                            StatisticsFormType.valueOf((String) r[0]),
                            CardStatus.valueOf((String) r[1]),
                            CardType.valueOf((String) r[2]),
                            ((Number) r[3]).longValue()
                    );
                })
                .toList();
    }

    /** One aggregate row per (organization id, form, {@link CardStatus}, {@link CardType}) — the geography drill-down. */
    public List<StatisticsOrganizationCardStatusCountProjection> countGroupedByOrganizationCells(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        String sql = "select t.sender_organization_id as organization_id, t.form_type as form_type, "
                + "t.status as status, t.card_type as card_type, count(*) as total"
                + " from (" + unionSourceWithOrg(organizationIds) + ") t"
                + " group by t.sender_organization_id, t.form_type, t.status, t.card_type";

        List<?> rows = bindRange(entityManager.createNativeQuery(sql), fromInclusive, toExclusive).getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new StatisticsOrganizationCardStatusCountProjection(
                            ((Number) r[0]).longValue(),
                            StatisticsFormType.valueOf((String) r[1]),
                            CardStatus.valueOf((String) r[2]),
                            CardType.valueOf((String) r[3]),
                            ((Number) r[4]).longValue()
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
