package uz.uzinfocom.app.modules.report.statistics.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.lookup.projection.ReferenceItemProjection;
import uz.uzinfocom.app.modules.reference.repository.CatalogRepository;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsAgeBreakdownResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCategoryCellResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsGenderBreakdownResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsNodeCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsNodeResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsPeriodCountsResponse;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsActRepository;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsCardRepository;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyNode;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "Statistika" — geography-first (republic→region→district→
 * organization, one level per call via the shared {@link
 * ReportHierarchyService}) surveillance statistics: confirmed/primary case
 * counts (form058 + form058_1), an age (18-year) and gender cut, and a
 * per-social-category breakdown driven entirely by the live {@code
 * ref_catalog(type = 'CATEGORY')} catalog — unlike "Form 8", whose category
 * buckets are a hardcoded fixed set, adding/renaming a category entry here
 * changes the report's columns with no code change.
 * <p>
 * Unlike every year-over-year report under {@code modules.report} (which
 * shifts one caller-supplied range back by a fixed number of years), this
 * compares two entirely independent, caller-chosen ranges — "Davr A"
 * ({@code fromA}/{@code toA}, always present) and an optional "Davr B"
 * ({@code fromB}/{@code toB}) — so an analyst can freely compare, say, March
 * against April, not just "this year vs last year". Each period is a
 * completely separate call into {@link ReportHierarchyService} (the shared
 * hierarchy engine only ever carries one {@link ReportDateRange} at a time);
 * the two resulting node lists are then zipped by {@code code} — safe
 * because the geography/organization set a node list enumerates never
 * depends on the date range, only the counts do.
 */
@Service
@RequiredArgsConstructor
public class StatisticsReportQueryService {

    private static final String CATALOG_TYPE_CATEGORY = "CATEGORY";

    private final StatisticsReportRepository statisticsReportRepository;
    private final StatisticsCardRepository statisticsCardRepository;
    private final StatisticsActRepository statisticsActRepository;
    private final CatalogRepository catalogRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final LocalizedTextResolver localizedTextResolver;

    public List<StatisticsNodeResponse> getRoot(
            LocalDate fromA, LocalDate toA, LocalDate fromB, LocalDate toB
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        List<ReferenceItemProjection> categories = orderedCategories();
        StatisticsGeographyCountSource countSource = countSource(categories);

        List<ReportHierarchyNode<StatisticsNodeCounts>> nodesA = reportHierarchyService.loadRootBreakdown(
                currentOrganization, countSource, reportDateRangeResolver.resolve(fromA, toA), null
        );
        List<ReportHierarchyNode<StatisticsNodeCounts>> nodesB = hasPeriodB(fromB, toB)
                ? reportHierarchyService.loadRootBreakdown(
                        currentOrganization, countSource, reportDateRangeResolver.resolve(fromB, toB), null
                )
                : null;

        return zip(nodesA, nodesB, categories);
    }

    public List<StatisticsNodeResponse> getChildren(
            String regionCode, String districtCode, LocalDate fromA, LocalDate toA, LocalDate fromB, LocalDate toB
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        List<ReferenceItemProjection> categories = orderedCategories();
        StatisticsGeographyCountSource countSource = countSource(categories);

        List<ReportHierarchyNode<StatisticsNodeCounts>> nodesA = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, countSource,
                reportDateRangeResolver.resolve(fromA, toA), null
        );
        List<ReportHierarchyNode<StatisticsNodeCounts>> nodesB = hasPeriodB(fromB, toB)
                ? reportHierarchyService.loadChildren(
                        currentOrganization, regionCode, districtCode, countSource,
                        reportDateRangeResolver.resolve(fromB, toB), null
                )
                : null;

        return zip(nodesA, nodesB, categories);
    }

    private boolean hasPeriodB(LocalDate fromB, LocalDate toB) {
        return fromB != null || toB != null;
    }

    private List<ReferenceItemProjection> orderedCategories() {
        return catalogRepository.findAllProjectedByTypeAndDeletedFalseOrderByNameUzAsc(CATALOG_TYPE_CATEGORY);
    }

    private StatisticsGeographyCountSource countSource(List<ReferenceItemProjection> categories) {
        Set<String> knownCategoryCodes = categories.stream()
                .map(ReferenceItemProjection::getCode)
                .collect(Collectors.toSet());
        return new StatisticsGeographyCountSource(
                statisticsReportRepository, statisticsCardRepository, statisticsActRepository, knownCategoryCodes
        );
    }

    /**
     * Pairs period A's node list (which defines the row set/order) with
     * period B's by {@code code}. The two lists always enumerate the same
     * geography/organizations — only the counts differ — so every code in
     * {@code nodesA} is guaranteed present in {@code nodesB} when the latter
     * was requested.
     */
    private List<StatisticsNodeResponse> zip(
            List<ReportHierarchyNode<StatisticsNodeCounts>> nodesA,
            List<ReportHierarchyNode<StatisticsNodeCounts>> nodesB,
            List<ReferenceItemProjection> categories
    ) {
        Map<String, ReportHierarchyNode<StatisticsNodeCounts>> nodesBByCode = nodesB == null
                ? Map.of()
                : nodesB.stream().collect(Collectors.toMap(ReportHierarchyNode::code, node -> node));

        return nodesA.stream()
                .map(nodeA -> {
                    ReportHierarchyNode<StatisticsNodeCounts> nodeB = nodesBByCode.get(nodeA.code());
                    return new StatisticsNodeResponse(
                            nodeA.code(), nodeA.name(), nodeA.hasChildren(),
                            toPeriodResponse(nodeA.counts(), categories),
                            nodeB == null ? null : toPeriodResponse(nodeB.counts(), categories)
                    );
                })
                .toList();
    }

    private StatisticsPeriodCountsResponse toPeriodResponse(
            StatisticsNodeCounts counts, List<ReferenceItemProjection> categories
    ) {
        StatisticsCounts overall = counts.overall();

        return new StatisticsPeriodCountsResponse(
                overall.confirmedTotal(), overall.primaryTotal(),
                new StatisticsAgeBreakdownResponse(
                        overall.confirmedUnder18(), overall.confirmedAdult(),
                        overall.primaryUnder18(), overall.primaryAdult()
                ),
                new StatisticsGenderBreakdownResponse(
                        overall.confirmedFemale(), overall.confirmedMale(),
                        overall.primaryFemale(), overall.primaryMale()
                ),
                categoryCells(categories, counts),
                counts.cards().total(), cardStatusCells(counts),
                counts.acts().total(), actStatusCells(counts)
        );
    }

    /** Every {@link CardStatus} value, in enum order, zero-filled where the node has no cards in that status. */
    private List<CardStatusCountResponse> cardStatusCells(StatisticsNodeCounts counts) {
        Map<CardStatus, Long> byStatus = counts.cards().byStatus();
        return Arrays.stream(CardStatus.values())
                .map(status -> new CardStatusCountResponse(status, byStatus.getOrDefault(status, 0L)))
                .toList();
    }

    /** Every {@link ActStatus} value, in enum order, zero-filled where the node has no acts in that status. */
    private List<ActStatusCountResponse> actStatusCells(StatisticsNodeCounts counts) {
        Map<ActStatus, Long> byStatus = counts.acts().byStatus();
        return Arrays.stream(ActStatus.values())
                .map(status -> new ActStatusCountResponse(status, byStatus.getOrDefault(status, 0L)))
                .toList();
    }

    private List<StatisticsCategoryCellResponse> categoryCells(
            List<ReferenceItemProjection> categories, StatisticsNodeCounts counts
    ) {
        return categories.stream()
                .map(category -> {
                    StatisticsCounts c = counts.category(category.getCode());
                    return new StatisticsCategoryCellResponse(
                            category.getCode(), localizedName(category), c.confirmedTotal(), c.primaryTotal()
                    );
                })
                .toList();
    }

    private String localizedName(ReferenceItemProjection category) {
        return localizedTextResolver.resolve(
                category.getNameUz(), category.getNameUzCyril(), category.getNameRu(), category.getNameKaa()
        );
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
