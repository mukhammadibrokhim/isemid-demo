package uz.uzinfocom.app.modules.report.statistics.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.ActTypeCountResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTypeCountResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.lookup.Icd10LookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.PopulationLookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.projection.ReferenceItemProjection;
import uz.uzinfocom.app.modules.reference.repository.CatalogRepository;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsAgeBreakdownResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsAgeGroup;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsSeriesBucket;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsSeriesPointResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsSeriesResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsTopDiseaseResponse;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsSeriesRepository;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCategoryCellResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsForm129BlockResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsForm129CategoryCellResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsForm129Counts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsForm129StatusCountResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsFormBlockCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsFormType;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsTopDiseaseCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsFormBlockResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsGenderBreakdownResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsNodeCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsNodeResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsPeriodCountsResponse;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsActRepository;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsCardRepository;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsForm129Repository;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyNode;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.modules.report.shared.ResolvedReportNode;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
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
    private static final String TOTAL_ROW_CODE = "TOTAL";
    private static final String REPUBLIC_ROW_CODE = "UZ";
    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");
    private static final int DEFAULT_TOP_DISEASES = 10;
    private static final int MAX_TOP_DISEASES = 50;
    private static final int MAX_SERIES_BUCKETS = 400;

    private final StatisticsReportRepository statisticsReportRepository;
    private final StatisticsCardRepository statisticsCardRepository;
    private final StatisticsActRepository statisticsActRepository;
    private final StatisticsForm129Repository statisticsForm129Repository;
    private final StatisticsSeriesRepository statisticsSeriesRepository;
    private final CatalogRepository catalogRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final LocalizedTextResolver localizedTextResolver;
    private final PopulationLookupService populationLookupService;
    private final Icd10LookupService icd10LookupService;

    public List<StatisticsNodeResponse> getRoot(
            LocalDate fromA, LocalDate toA, LocalDate fromB, LocalDate toB,
            String genderCode, StatisticsAgeGroup ageGroup, String categoryCode
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        List<ReferenceItemProjection> categories = orderedCategories();
        StatisticsGeographyCountSource countSource =
                countSource(categories, StatisticsFilter.of(genderCode, ageGroup, categoryCode));
        String parentCode = reportHierarchyService.resolveNode(currentOrganization, null, null).code();

        List<ReportHierarchyNode<StatisticsNodeCounts>> nodesA = reportHierarchyService.loadRootBreakdown(
                currentOrganization, countSource, reportDateRangeResolver.resolve(fromA, toA), null
        );
        List<ReportHierarchyNode<StatisticsNodeCounts>> nodesB = hasPeriodB(fromB, toB)
                ? reportHierarchyService.loadRootBreakdown(
                currentOrganization, countSource, reportDateRangeResolver.resolve(fromB, toB), null
        )
                : null;

        return zip(nodesA, nodesB, categories, parentCode, populationYear(toA));
    }

    public List<StatisticsNodeResponse> getChildren(
            String regionCode, String districtCode, LocalDate fromA, LocalDate toA, LocalDate fromB, LocalDate toB,
            String genderCode, StatisticsAgeGroup ageGroup, String categoryCode
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        List<ReferenceItemProjection> categories = orderedCategories();
        StatisticsGeographyCountSource countSource =
                countSource(categories, StatisticsFilter.of(genderCode, ageGroup, categoryCode));
        String parentCode = reportHierarchyService.resolveNode(currentOrganization, regionCode, districtCode).code();

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

        return zip(nodesA, nodesB, categories, parentCode, populationYear(toA));
    }

    private boolean hasPeriodB(LocalDate fromB, LocalDate toB) {
        return fromB != null || toB != null;
    }

    /**
     * The {@code ref_population} year for the intensive-rate denominator — «Davr A» end year, else the current year.
     */
    private int populationYear(LocalDate toA) {
        return toA != null ? toA.getYear() : LocalDate.now(APPLICATION_ZONE).getYear();
    }

    /**
     * Time dynamics for one geography node (the caller's whole scope, or an
     * explicit {@code regionCode}/{@code districtCode} inside it): case counts
     * per {@link StatisticsSeriesBucket} bucket over {@code [from, to]}, forms
     * №058 / №058-1 / №129 as three separate series. Empty buckets are present
     * with zeros. No cross-filter — this endpoint is the trend line, the
     * gender/age/category cuts live on {@code /root} and {@code /children}.
     */
    public StatisticsSeriesResponse getSeries(
            String regionCode, String districtCode, LocalDate from, LocalDate to, StatisticsSeriesBucket bucketOrNull
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        ResolvedReportNode node = reportHierarchyService.resolveNode(currentOrganization, regionCode, districtCode);
        StatisticsSeriesBucket bucket = bucketOrNull != null ? bucketOrNull : StatisticsSeriesBucket.WEEK;

        LocalDate today = LocalDate.now(APPLICATION_ZONE);
        LocalDate lastStart = bucket.truncate(to != null ? to : today);
        LocalDate firstStart = bucket.truncate(from != null ? from : defaultSeriesFrom(bucket, lastStart));
        if (firstStart.isAfter(lastStart)) {
            firstStart = lastStart;
        }

        List<LocalDate> starts = new ArrayList<>();
        for (LocalDate cursor = firstStart; !cursor.isAfter(lastStart); cursor = bucket.next(cursor)) {
            starts.add(cursor);
        }
        if (starts.size() > MAX_SERIES_BUCKETS) {
            starts = new ArrayList<>(starts.subList(starts.size() - MAX_SERIES_BUCKETS, starts.size()));
        }

        Map<LocalDate, Integer> indexByStart = new HashMap<>(starts.size() * 2);
        for (int i = 0; i < starts.size(); i++) {
            indexByStart.put(starts.get(i), i);
        }

        long[] c058c = new long[starts.size()];
        long[] c058p = new long[starts.size()];
        long[] c0581c = new long[starts.size()];
        long[] c0581p = new long[starts.size()];
        long[] f129 = new long[starts.size()];

        Instant fromInclusive = starts.getFirst().atStartOfDay(APPLICATION_ZONE).toInstant();
        Instant toExclusive = bucket.next(starts.getLast()).atStartOfDay(APPLICATION_ZONE).toInstant();

        statisticsSeriesRepository.countCasesByBucket(node.organizationIds(), bucket, fromInclusive, toExclusive)
                .forEach(row -> {
                    Integer i = indexByStart.get(row.bucketStart());
                    if (i == null) {
                        return;
                    }
                    if (row.formType() == StatisticsFormType.FORM058) {
                        c058c[i] += row.confirmed();
                        c058p[i] += row.primary();
                    } else {
                        c0581c[i] += row.confirmed();
                        c0581p[i] += row.primary();
                    }
                });
        statisticsSeriesRepository.countForm129ByBucket(node.organizationIds(), bucket, fromInclusive, toExclusive)
                .forEach(row -> {
                    Integer i = indexByStart.get(row.bucketStart());
                    if (i != null) {
                        f129[i] += row.total();
                    }
                });

        List<StatisticsSeriesPointResponse> points = new ArrayList<>(starts.size());
        for (int i = 0; i < starts.size(); i++) {
            LocalDate start = starts.get(i);
            points.add(new StatisticsSeriesPointResponse(
                    start, bucket.endOf(start),
                    c058c[i], c058p[i], c0581c[i], c0581p[i], f129[i]
            ));
        }

        return new StatisticsSeriesResponse(
                node.code(), node.name(), bucket,
                starts.getFirst(), bucket.endOf(starts.getLast()), points
        );
    }

    private LocalDate defaultSeriesFrom(StatisticsSeriesBucket bucket, LocalDate lastStart) {
        return switch (bucket) {
            case DAY -> lastStart.minusDays(59);
            case WEEK -> lastStart.minusWeeks(25);
            case MONTH -> lastStart.minusMonths(11);
        };
    }

    /**
     * "Which disease preponderates" for one geography node over a period:
     * confirmed ({@code status = 'APPROVED'}) {@code form058} + {@code
     * form058_1} cases ranked by their {@code final_icd10_code} (final code
     * only, no fallback), descending, truncated to {@code limit} (default
     * {@value #DEFAULT_TOP_DISEASES}, max {@value #MAX_TOP_DISEASES}).
     */
    public List<StatisticsTopDiseaseResponse> getTopDiseases(
            String regionCode, String districtCode, LocalDate from, LocalDate to, Integer limitOrNull
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        ResolvedReportNode node = reportHierarchyService.resolveNode(currentOrganization, regionCode, districtCode);
        int limit = limitOrNull != null
                ? Math.clamp(limitOrNull, 1, MAX_TOP_DISEASES)
                : DEFAULT_TOP_DISEASES;

        ReportDateRange range = reportDateRangeResolver.resolve(from, to);
        List<StatisticsTopDiseaseCountProjection> rows = statisticsSeriesRepository.countConfirmedByFinalDiagnosis(
                node.organizationIds(), range.fromInclusive(), range.toExclusive()
        );
        List<StatisticsTopDiseaseCountProjection> top = rows.stream()
                .sorted(Comparator.comparingLong(StatisticsTopDiseaseCountProjection::confirmedCount).reversed())
                .limit(limit)
                .toList();

        Map<String, String> names = icd10LookupService.resolveNames(
                top.stream().map(StatisticsTopDiseaseCountProjection::diagnosisCode).toList()
        );
        return top.stream()
                .map(row -> new StatisticsTopDiseaseResponse(
                        row.diagnosisCode(),
                        names.getOrDefault(row.diagnosisCode(), row.diagnosisCode()),
                        row.confirmedCount()
                ))
                .toList();
    }

    private List<ReferenceItemProjection> orderedCategories() {
        return catalogRepository.findAllProjectedByTypeAndDeletedFalseOrderByNameUzAsc(CATALOG_TYPE_CATEGORY);
    }

    private StatisticsGeographyCountSource countSource(
            List<ReferenceItemProjection> categories, StatisticsFilter filter
    ) {
        Set<String> knownCategoryCodes = categories.stream()
                .map(ReferenceItemProjection::getCode)
                .collect(Collectors.toSet());
        return new StatisticsGeographyCountSource(
                statisticsReportRepository, statisticsCardRepository, statisticsActRepository,
                statisticsForm129Repository, knownCategoryCodes, filter
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
            List<ReferenceItemProjection> categories,
            String parentCode,
            int populationYear
    ) {
        Map<String, ReportHierarchyNode<StatisticsNodeCounts>> nodesBByCode = nodesB == null
                ? Map.of()
                : nodesB.stream().collect(Collectors.toMap(ReportHierarchyNode::code, node -> node));

        return nodesA.stream()
                .map(nodeA -> {
                    ReportHierarchyNode<StatisticsNodeCounts> nodeB = nodesBByCode.get(nodeA.code());
                    boolean isTotalRow = TOTAL_ROW_CODE.equals(nodeA.code());
                    return new StatisticsNodeResponse(
                            nodeA.code(), nodeA.name(), nodeA.hasChildren(),
                            isTotalRow ? null : parentCode,
                            populationFor(nodeA.code(), populationYear),
                            toPeriodResponse(nodeA.counts(), categories),
                            nodeB == null ? null : toPeriodResponse(nodeB.counts(), categories)
                    );
                })
                .toList();
    }

    /**
     * Territory population for the intensive-rate denominator — only a region
     * or district code resolves ({@link PopulationLookupService}); an
     * organization id (numeric), the republic root and the "Jami" row have no
     * own {@code ref_population} entry, so {@code null}. A zero lookup (code
     * present but no row for that year) is also surfaced as {@code null}.
     */
    private Long populationFor(String code, int year) {
        if (TOTAL_ROW_CODE.equals(code) || REPUBLIC_ROW_CODE.equalsIgnoreCase(code) || isNumeric(code)) {
            return null;
        }
        long population = populationLookupService.resolveByNodeCode(code, year);
        return population > 0 ? population : null;
    }

    private boolean isNumeric(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        for (int i = 0; i < code.length(); i++) {
            if (!Character.isDigit(code.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private StatisticsPeriodCountsResponse toPeriodResponse(
            StatisticsNodeCounts counts, List<ReferenceItemProjection> categories
    ) {
        return new StatisticsPeriodCountsResponse(
                formBlockResponse(counts.form058(), categories),
                formBlockResponse(counts.form0581(), categories),
                form129BlockResponse(counts.form129(), categories)
        );
    }

    private StatisticsFormBlockResponse formBlockResponse(
            StatisticsFormBlockCounts block, List<ReferenceItemProjection> categories
    ) {
        StatisticsCounts overall = block.overall();

        return new StatisticsFormBlockResponse(
                overall.confirmedTotal(), overall.primaryTotal(),
                new StatisticsAgeBreakdownResponse(
                        overall.confirmedUnder18(), overall.confirmedAdult(),
                        overall.primaryUnder18(), overall.primaryAdult()
                ),
                new StatisticsGenderBreakdownResponse(
                        overall.confirmedFemale(), overall.confirmedMale(),
                        overall.primaryFemale(), overall.primaryMale()
                ),
                categoryCells(categories, block),
                block.cards().total(), cardStatusCells(block), cardTypeCells(block),
                block.acts().total(), actStatusCells(block), actTypeCells(block)
        );
    }

    private StatisticsForm129BlockResponse form129BlockResponse(
            StatisticsForm129Counts counts, List<ReferenceItemProjection> categories
    ) {
        return new StatisticsForm129BlockResponse(
                counts.total(), counts.under18(), counts.adult(), counts.female(), counts.male(),
                form129CategoryCells(categories, counts), form129StatusCells(counts)
        );
    }

    /**
     * Every {@link CardStatus} value, in enum order, zero-filled where the block has no cards in that status.
     */
    private List<CardStatusCountResponse> cardStatusCells(StatisticsFormBlockCounts block) {
        Map<CardStatus, Long> byStatus = block.cards().byStatus();
        return Arrays.stream(CardStatus.values())
                .map(status -> new CardStatusCountResponse(status, byStatus.getOrDefault(status, 0L)))
                .toList();
    }

    /**
     * Every {@link CardType} value, in enum order, zero-filled where the block has no cards of that type.
     */
    private List<CardTypeCountResponse> cardTypeCells(StatisticsFormBlockCounts block) {
        Map<CardType, Long> byType = block.cards().byType();
        return Arrays.stream(CardType.values())
                .map(type -> new CardTypeCountResponse(type, byType.getOrDefault(type, 0L)))
                .toList();
    }

    /**
     * Every {@link ActStatus} value, in enum order, zero-filled where the block has no acts in that status.
     */
    private List<ActStatusCountResponse> actStatusCells(StatisticsFormBlockCounts block) {
        Map<ActStatus, Long> byStatus = block.acts().byStatus();
        return Arrays.stream(ActStatus.values())
                .map(status -> new ActStatusCountResponse(status, byStatus.getOrDefault(status, 0L)))
                .toList();
    }

    /**
     * Every {@link ActType} value, in enum order, zero-filled where the block has no acts of that type.
     */
    private List<ActTypeCountResponse> actTypeCells(StatisticsFormBlockCounts block) {
        Map<ActType, Long> byType = block.acts().byType();
        return Arrays.stream(ActType.values())
                .map(type -> new ActTypeCountResponse(type, byType.getOrDefault(type, 0L)))
                .toList();
    }

    /**
     * Every {@link Form129Status} value, in enum order, zero-filled where the node has no form129 in that status.
     */
    private List<StatisticsForm129StatusCountResponse> form129StatusCells(StatisticsForm129Counts counts) {
        Map<Form129Status, Long> byStatus = counts.byStatus();
        return Arrays.stream(Form129Status.values())
                .map(status -> new StatisticsForm129StatusCountResponse(status, byStatus.getOrDefault(status, 0L)))
                .toList();
    }

    private List<StatisticsCategoryCellResponse> categoryCells(
            List<ReferenceItemProjection> categories, StatisticsFormBlockCounts block
    ) {
        return categories.stream()
                .map(category -> {
                    StatisticsCounts c = block.category(category.getCode());
                    return new StatisticsCategoryCellResponse(
                            category.getCode(), localizedName(category), c.confirmedTotal(), c.primaryTotal()
                    );
                })
                .toList();
    }

    private List<StatisticsForm129CategoryCellResponse> form129CategoryCells(
            List<ReferenceItemProjection> categories, StatisticsForm129Counts counts
    ) {
        return categories.stream()
                .map(category -> new StatisticsForm129CategoryCellResponse(
                        category.getCode(), localizedName(category), counts.category(category.getCode())
                ))
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
