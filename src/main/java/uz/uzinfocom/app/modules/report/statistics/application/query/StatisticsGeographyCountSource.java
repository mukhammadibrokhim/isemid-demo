package uz.uzinfocom.app.modules.report.statistics.application.query;

import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsActCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCardCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCategoryCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsNodeCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsOrganizationActStatusCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsOrganizationCardStatusCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsOrganizationCategoryCountProjection;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsActRepository;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsCardRepository;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The {@link ReportCountSource} strategy for "Statistika": every
 * organization batch is aggregated in one grouped-by-{@code category_code}
 * query for form058/form058_1 (rolled up into {@link StatisticsNodeCounts}'
 * {@code overall}/{@code byCategoryCode}, same as before), plus one
 * grouped-by-status query each for {@code card} and {@code act} — three
 * small aggregate queries total per organization batch per period, none of
 * which ever materializes a case/card/act row in the JVM.
 * <p>
 * Deliberately <b>not</b> a Spring bean — constructed per request in {@code
 * StatisticsReportQueryService} with the set of currently-active category
 * codes, then handed to {@code ReportHierarchyService}, which is designed to
 * receive a fresh {@link ReportCountSource} per call. The {@code
 * diagnosisCode} argument of the interface is unused here — this report has
 * no diagnosis dimension.
 */
public final class StatisticsGeographyCountSource implements ReportCountSource<StatisticsNodeCounts> {

    private final StatisticsReportRepository statisticsReportRepository;
    private final StatisticsCardRepository statisticsCardRepository;
    private final StatisticsActRepository statisticsActRepository;
    private final Set<String> knownCategoryCodes;

    public StatisticsGeographyCountSource(
            StatisticsReportRepository statisticsReportRepository,
            StatisticsCardRepository statisticsCardRepository,
            StatisticsActRepository statisticsActRepository,
            Set<String> knownCategoryCodes
    ) {
        this.statisticsReportRepository = statisticsReportRepository;
        this.statisticsCardRepository = statisticsCardRepository;
        this.statisticsActRepository = statisticsActRepository;
        this.knownCategoryCodes = knownCategoryCodes;
    }

    @Override
    public StatisticsNodeCounts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        List<StatisticsCategoryCountProjection> categoryRows = statisticsReportRepository
                .countByCategory(organizationIds, range.fromInclusive(), range.toExclusive());
        StatisticsCardCounts cardCounts = cardTotal(organizationIds, range);
        StatisticsActCounts actCounts = actTotal(organizationIds, range);

        return rollUp(categoryRows, cardCounts, actCounts);
    }

    @Override
    public Map<Long, StatisticsNodeCounts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        Map<Long, List<StatisticsCategoryCountProjection>> categoryRowsByOrg = new HashMap<>();
        for (StatisticsOrganizationCategoryCountProjection p : statisticsReportRepository
                .countGroupedByOrganizationAndCategory(organizationIds, range.fromInclusive(), range.toExclusive())) {
            categoryRowsByOrg
                    .computeIfAbsent(p.organizationId(), _ -> new ArrayList<>())
                    .add(new StatisticsCategoryCountProjection(p.categoryCode(), p.counts()));
        }

        Map<Long, StatisticsCardCounts> cardCountsByOrg = new HashMap<>();
        for (StatisticsOrganizationCardStatusCountProjection p : statisticsCardRepository
                .countGroupedByOrganizationAndStatus(organizationIds, range.fromInclusive(), range.toExclusive())) {
            cardCountsByOrg.merge(
                    p.organizationId(), new StatisticsCardCounts(p.count(), Map.of(p.status(), p.count())),
                    StatisticsCardCounts::plus
            );
        }

        Map<Long, StatisticsActCounts> actCountsByOrg = new HashMap<>();
        for (StatisticsOrganizationActStatusCountProjection p : statisticsActRepository
                .countGroupedByOrganizationAndStatus(organizationIds, range.fromInclusive(), range.toExclusive())) {
            actCountsByOrg.merge(
                    p.organizationId(), new StatisticsActCounts(p.count(), Map.of(p.status(), p.count())),
                    StatisticsActCounts::plus
            );
        }

        Map<Long, StatisticsNodeCounts> result = new HashMap<>();
        for (Long organizationId : organizationIds) {
            result.put(organizationId, rollUp(
                    categoryRowsByOrg.getOrDefault(organizationId, List.of()),
                    cardCountsByOrg.getOrDefault(organizationId, StatisticsCardCounts.EMPTY),
                    actCountsByOrg.getOrDefault(organizationId, StatisticsActCounts.EMPTY)
            ));
        }
        return result;
    }

    @Override
    public StatisticsNodeCounts empty() {
        return StatisticsNodeCounts.EMPTY;
    }

    @Override
    public StatisticsNodeCounts merge(StatisticsNodeCounts a, StatisticsNodeCounts b) {
        return a.plus(b);
    }

    private StatisticsCardCounts cardTotal(List<Long> organizationIds, ReportDateRange range) {
        StatisticsCardCounts result = StatisticsCardCounts.EMPTY;
        for (var p : statisticsCardRepository.countByStatus(organizationIds, range.fromInclusive(), range.toExclusive())) {
            result = result.plus(new StatisticsCardCounts(p.count(), Map.of(p.status(), p.count())));
        }
        return result;
    }

    private StatisticsActCounts actTotal(List<Long> organizationIds, ReportDateRange range) {
        StatisticsActCounts result = StatisticsActCounts.EMPTY;
        for (var p : statisticsActRepository.countByStatus(organizationIds, range.fromInclusive(), range.toExclusive())) {
            result = result.plus(new StatisticsActCounts(p.count(), Map.of(p.status(), p.count())));
        }
        return result;
    }

    private StatisticsNodeCounts rollUp(
            List<StatisticsCategoryCountProjection> categoryRows, StatisticsCardCounts cards, StatisticsActCounts acts
    ) {
        StatisticsCounts overall = StatisticsCounts.EMPTY;
        Map<String, StatisticsCounts> byCategoryCode = new HashMap<>();
        for (StatisticsCategoryCountProjection row : categoryRows) {
            overall = overall.plus(row.counts());
            if (row.categoryCode() != null && knownCategoryCodes.contains(row.categoryCode())) {
                byCategoryCode.merge(row.categoryCode(), row.counts(), StatisticsCounts::plus);
            }
        }
        return new StatisticsNodeCounts(overall, byCategoryCode, cards, acts);
    }
}
