package uz.uzinfocom.app.platform.dashboard.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.DynamicsPointResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.SourceCountResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.TopDiagnosisResponse;
import uz.uzinfocom.app.platform.dashboard.infrastructure.persistence.dto.CaseSummaryAggregate;
import uz.uzinfocom.app.platform.dashboard.infrastructure.persistence.dto.GeoCodeCount;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopeOrganizationIdResolver;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Home-dashboard case statistics (form058 + form058-1 combined), aggregated
 * entirely inside Postgres via native SQL {@code UNION ALL} queries — one
 * round trip per statistic, one {@code GROUP BY}, no Java-side {@code
 * HashMap}/{@code TreeMap} merging of two separately-fetched result sets.
 * Replaces the per-form-type calls into {@code Form058StatsQueryService}/
 * {@code Form0581StatsQueryService} that {@code HomeDashboardQueryService}
 * used to fetch and merge itself.
 * <p>
 * The receiver-organization scope rule replicated here ({@link
 * #receiverScopeFilter}) is exactly {@link
 * uz.uzinfocom.app.platform.scope.jpa.OrganizationScopePredicateFactory#apply}
 * applied to the {@code receiverOrganizationId} field — the dashboard only
 * ever looks at incoming cases, i.e. {@code received = true} in {@code
 * SenderReceiverScopePredicateFactory} terms. REGION/DISTRICT id lists are
 * resolved via the same cached {@link OrganizationScopeOrganizationIdResolver}
 * every Criteria-API repository already uses, so scope resolution itself
 * stays a single source of truth; only the aggregation moves into SQL.
 * <p>
 * The active/pending status lists hardcoded into {@link #FORM058_PENDING_STATUSES}
 * and {@link #FORM0581_PENDING_STATUSES} mirror {@code FormStatus#isApprovalDecisionPending()}
 * and {@code Form0581Status#isApprovalDecisionPending()} respectively — these two
 * enums deliberately differ (058 has CARD_LINKED, 058-1 does not). If either
 * enum's pending set changes, this class must be updated to match.
 */
@Repository
public class CaseStatsAggregateRepository {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    private static final String FORM058_PENDING_STATUSES = "'SENT','RECEIVED','CARD_LINKED','APPROVED_PENDING'";
    private static final String FORM0581_PENDING_STATUSES = "'SENT','RECEIVED','APPROVED_PENDING'";

    private final EntityManager entityManager;
    private final OrganizationScopeOrganizationIdResolver organizationIdResolver;

    public CaseStatsAggregateRepository(
            EntityManager entityManager,
            OrganizationScopeOrganizationIdResolver organizationIdResolver
    ) {
        this.entityManager = entityManager;
        this.organizationIdResolver = organizationIdResolver;
    }

    public CaseSummaryAggregate caseSummary(ResolvedOrganizationScope scope, LocalDate asOfDate) {
        ScopeFilter filter = receiverScopeFilter(scope);
        if (filter.excludesEverything()) {
            return new CaseSummaryAggregate(0, 0, 0, 0);
        }

        String orgFilter = orgFilterFragment(filter);
        String sql = """
                SELECT
                    COUNT(*) FILTER (WHERE src = '058')  AS form058_total,
                    COUNT(*) FILTER (WHERE src = '0581') AS form0581_total,
                    COUNT(*) FILTER (WHERE is_pending)   AS active_total,
                    COUNT(*) FILTER (WHERE created_at >= :todayStart AND created_at < :todayEnd) AS today_total
                FROM (
                    SELECT '058' AS src, status IN (%s) AS is_pending, created_at
                    FROM form058
                    WHERE deleted = false %s
                    UNION ALL
                    SELECT '0581' AS src, status IN (%s) AS is_pending, created_at
                    FROM form058_1
                    WHERE deleted = false %s
                ) combined
                """.formatted(FORM058_PENDING_STATUSES, orgFilter, FORM0581_PENDING_STATUSES, orgFilter);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("todayStart", asOfDate.atStartOfDay(APPLICATION_ZONE).toInstant());
        query.setParameter("todayEnd", asOfDate.plusDays(1).atStartOfDay(APPLICATION_ZONE).toInstant());
        bindOrgIdsIfPresent(query, filter);

        Object[] row = (Object[]) query.getSingleResult();
        return new CaseSummaryAggregate(
                toLong(row[0]),
                toLong(row[1]),
                toLong(row[2]),
                toLong(row[3])
        );
    }

    public List<DynamicsPointResponse> monthlyDynamics(ResolvedOrganizationScope scope, LocalDate from, LocalDate to) {
        ScopeFilter filter = receiverScopeFilter(scope);
        if (filter.excludesEverything()) {
            return List.of();
        }

        String orgFilter = orgFilterFragment(filter);
        String sql = """
                SELECT date_trunc('month', combined.created_at AT TIME ZONE 'Asia/Tashkent') AS period_start,
                       COUNT(*) AS cnt
                FROM (
                    SELECT created_at FROM form058
                    WHERE deleted = false %s AND created_at >= :from AND created_at < :to
                    UNION ALL
                    SELECT created_at FROM form058_1
                    WHERE deleted = false %s AND created_at >= :from AND created_at < :to
                ) combined
                GROUP BY 1
                ORDER BY 1
                """.formatted(orgFilter, orgFilter);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("from", from.atStartOfDay(APPLICATION_ZONE).toInstant());
        query.setParameter("to", to.plusDays(1).atStartOfDay(APPLICATION_ZONE).toInstant());
        bindOrgIdsIfPresent(query, filter);

        List<Object[]> rows = castRows(query.getResultList());
        return rows.stream()
                .map(row -> new DynamicsPointResponse(toLocalDate(row[0]), toLong(row[1])))
                .toList();
    }

    public List<TopDiagnosisResponse> topDiagnoses(ResolvedOrganizationScope scope, int limit) {
        ScopeFilter filter = receiverScopeFilter(scope);
        if (filter.excludesEverything()) {
            return List.of();
        }

        String orgFilter = orgFilterFragment(filter);
        String sql = """
                SELECT mkb10_code, COUNT(*) AS cnt
                FROM (
                    SELECT mkb10_code FROM form058
                    WHERE deleted = false AND mkb10_code IS NOT NULL %s
                    UNION ALL
                    SELECT mkb10_code FROM form058_1
                    WHERE deleted = false AND mkb10_code IS NOT NULL %s
                ) combined
                GROUP BY mkb10_code
                ORDER BY cnt DESC
                LIMIT :limit
                """.formatted(orgFilter, orgFilter);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("limit", limit);
        bindOrgIdsIfPresent(query, filter);

        List<Object[]> rows = castRows(query.getResultList());
        return rows.stream()
                .map(row -> new TopDiagnosisResponse((String) row[0], toLong(row[1])))
                .toList();
    }

    public List<SourceCountResponse> sourceBreakdown(ResolvedOrganizationScope scope) {
        ScopeFilter filter = receiverScopeFilter(scope);
        if (filter.excludesEverything()) {
            return List.of();
        }

        String orgFilter = orgFilterFragment(filter);
        String sql = """
                SELECT source, COUNT(*) AS cnt
                FROM (
                    SELECT source FROM form058 WHERE deleted = false %s
                    UNION ALL
                    SELECT source FROM form058_1 WHERE deleted = false %s
                ) combined
                GROUP BY source
                ORDER BY cnt DESC
                """.formatted(orgFilter, orgFilter);

        Query query = entityManager.createNativeQuery(sql);
        bindOrgIdsIfPresent(query, filter);

        List<Object[]> rows = castRows(query.getResultList());
        return rows.stream()
                .map(row -> new SourceCountResponse((String) row[0], toLong(row[1])))
                .toList();
    }

    /**
     * Case counts for every district within {@code regionCode}, joined
     * against {@code ref_district} so districts with zero cases still
     * appear with a count of 0 (a {@code LEFT JOIN}, not an inner one).
     * Unlike the other queries here this is not receiver-scope-filtered by
     * organization id list — the region itself already IS the scope (only
     * ever called for a REGION-mode caller's own region, or the ALL-mode
     * countrywide breakdown via {@link #regionBreakdown}), matching what
     * {@code HomeDashboardQueryService.buildGeoBreakdown} restricted to
     * before this moved into SQL.
     */
    public List<GeoCodeCount> districtBreakdown(String regionCode) {
        String sql = """
                SELECT d.code AS geo_code, COALESCE(f.cnt, 0) AS cnt
                FROM ref_district d
                LEFT JOIN (
                    SELECT o.district_code AS district_code, COUNT(*) AS cnt
                    FROM (
                        SELECT receiver_organization_id FROM form058 WHERE deleted = false
                        UNION ALL
                        SELECT receiver_organization_id FROM form058_1 WHERE deleted = false
                    ) combined
                    JOIN organization o ON o.id = combined.receiver_organization_id AND o.active = true
                    GROUP BY o.district_code
                ) f ON f.district_code = d.code
                WHERE d.parent_code = :regionCode AND d.deleted = false
                ORDER BY d.name_uz
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("regionCode", regionCode);

        List<Object[]> rows = castRows(query.getResultList());
        return rows.stream()
                .map(row -> new GeoCodeCount((String) row[0], toLong(row[1])))
                .toList();
    }

    /** Countrywide region breakdown — see {@link #districtBreakdown} for the join shape. */
    public List<GeoCodeCount> regionBreakdown() {
        String sql = """
                SELECT r.code AS geo_code, COALESCE(f.cnt, 0) AS cnt
                FROM ref_region r
                LEFT JOIN (
                    SELECT o.region_code AS region_code, COUNT(*) AS cnt
                    FROM (
                        SELECT receiver_organization_id FROM form058 WHERE deleted = false
                        UNION ALL
                        SELECT receiver_organization_id FROM form058_1 WHERE deleted = false
                    ) combined
                    JOIN organization o ON o.id = combined.receiver_organization_id AND o.active = true
                    GROUP BY o.region_code
                ) f ON f.region_code = r.code
                WHERE r.deleted = false
                ORDER BY r.name_uz
                """;

        Query query = entityManager.createNativeQuery(sql);

        List<Object[]> rows = castRows(query.getResultList());
        return rows.stream()
                .map(row -> new GeoCodeCount((String) row[0], toLong(row[1])))
                .toList();
    }

    private record ScopeFilter(boolean unrestricted, List<Long> organizationIds) {
        boolean excludesEverything() {
            return !unrestricted && organizationIds.isEmpty();
        }
    }

    private ScopeFilter receiverScopeFilter(ResolvedOrganizationScope scope) {
        if (!scope.isSanepidService()) {
            return new ScopeFilter(false, List.of(requireOrganizationId(scope)));
        }

        return switch (scope.mode()) {
            case ALL -> new ScopeFilter(true, List.of());
            case ORGANIZATION -> new ScopeFilter(false, List.of(requireOrganizationId(scope)));
            case REGION, DISTRICT -> new ScopeFilter(
                    false,
                    organizationIdResolver.resolveScopeOrganizationIds(scope.mode(), scope.regionCode(), scope.districtCode())
            );
        };
    }

    private Long requireOrganizationId(ResolvedOrganizationScope scope) {
        if (scope.organizationId() == null) {
            throw new ScopeViolationException("organization.scope_violation");
        }
        return scope.organizationId();
    }

    private String orgFilterFragment(ScopeFilter filter) {
        return filter.unrestricted() ? "" : "AND receiver_organization_id IN (:orgIds)";
    }

    private void bindOrgIdsIfPresent(Query query, ScopeFilter filter) {
        if (!filter.unrestricted()) {
            query.setParameter("orgIds", filter.organizationIds());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> castRows(List<?> rows) {
        return (List<Object[]>) rows;
    }

    private long toLong(Object value) {
        return ((Number) value).longValue();
    }

    private LocalDate toLocalDate(Object value) {
        return switch (value) {
            case Timestamp timestamp -> timestamp.toLocalDateTime().toLocalDate();
            case LocalDateTime localDateTime -> localDateTime.toLocalDate();
            case LocalDate localDate -> localDate;
            case java.sql.Date date -> date.toLocalDate();
            case null, default -> throw new IllegalStateException(
                    "Unexpected date_trunc result type: " + (value == null ? "null" : value.getClass())
            );
        };
    }
}
