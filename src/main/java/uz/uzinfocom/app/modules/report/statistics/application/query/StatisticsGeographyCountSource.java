package uz.uzinfocom.app.modules.report.statistics.application.query;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsActCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsActStatusCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCardCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCardStatusCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCategoryCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsForm129CountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsForm129Counts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsFormBlockCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsFormType;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsNodeCounts;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsOrganizationActStatusCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsOrganizationCardStatusCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsOrganizationCategoryCountProjection;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsOrganizationForm129CountProjection;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsActRepository;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsCardRepository;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsForm129Repository;
import uz.uzinfocom.app.modules.report.statistics.infrastructure.persistence.repository.StatisticsReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The {@link ReportCountSource} strategy for "Statistika". Every organization
 * batch is aggregated in four grouped native queries per period:
 * <ul>
 *   <li>form058/form058_1 cases — grouped by {@code (form_type,
 *       category_code)} ({@link StatisticsReportRepository})</li>
 *   <li>cards — grouped by {@code (form_type, status, card_type)} ({@link
 *       StatisticsCardRepository})</li>
 *   <li>acts — grouped by {@code (form_type, status, act_type)} ({@link
 *       StatisticsActRepository})</li>
 *   <li>form129 cases — grouped by {@code (category_code, status)} ({@link
 *       StatisticsForm129Repository})</li>
 * </ul>
 * rolled into one {@link StatisticsNodeCounts} whose three blocks (form 058,
 * form 058-1, form 129) are kept fully separate. None of the queries ever
 * materializes a case/card/act row in the JVM.
 * <p>
 * Deliberately <b>not</b> a Spring bean — constructed per request in {@code
 * StatisticsReportQueryService} with the set of currently-active category
 * codes, then handed to {@code ReportHierarchyService}, which is designed to
 * receive a fresh {@link ReportCountSource} per call. The {@code
 * diagnosisCode} argument of the interface is unused here — this report has no
 * diagnosis dimension.
 */
public final class StatisticsGeographyCountSource implements ReportCountSource<StatisticsNodeCounts> {

    private final StatisticsReportRepository statisticsReportRepository;
    private final StatisticsCardRepository statisticsCardRepository;
    private final StatisticsActRepository statisticsActRepository;
    private final StatisticsForm129Repository statisticsForm129Repository;
    private final Set<String> knownCategoryCodes;

    public StatisticsGeographyCountSource(
            StatisticsReportRepository statisticsReportRepository,
            StatisticsCardRepository statisticsCardRepository,
            StatisticsActRepository statisticsActRepository,
            StatisticsForm129Repository statisticsForm129Repository,
            Set<String> knownCategoryCodes
    ) {
        this.statisticsReportRepository = statisticsReportRepository;
        this.statisticsCardRepository = statisticsCardRepository;
        this.statisticsActRepository = statisticsActRepository;
        this.statisticsForm129Repository = statisticsForm129Repository;
        this.knownCategoryCodes = knownCategoryCodes;
    }

    @Override
    public StatisticsNodeCounts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        List<StatisticsCategoryCountProjection> categoryRows = statisticsReportRepository
                .countByCategory(organizationIds, range.fromInclusive(), range.toExclusive());
        List<StatisticsCardStatusCountProjection> cardRows = statisticsCardRepository
                .countCells(organizationIds, range.fromInclusive(), range.toExclusive());
        List<StatisticsActStatusCountProjection> actRows = statisticsActRepository
                .countCells(organizationIds, range.fromInclusive(), range.toExclusive());
        List<StatisticsForm129CountProjection> form129Rows = statisticsForm129Repository
                .countByCategoryAndStatus(organizationIds, range.fromInclusive(), range.toExclusive());

        return rollUp(categoryRows, cardRows, actRows, form129Rows);
    }

    @Override
    public Map<Long, StatisticsNodeCounts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        Map<Long, List<StatisticsCategoryCountProjection>> categoryByOrg = new HashMap<>();
        for (StatisticsOrganizationCategoryCountProjection p : statisticsReportRepository
                .countGroupedByOrganizationAndCategory(organizationIds, range.fromInclusive(), range.toExclusive())) {
            categoryByOrg
                    .computeIfAbsent(p.organizationId(), _ -> new ArrayList<>())
                    .add(new StatisticsCategoryCountProjection(p.formType(), p.categoryCode(), p.counts()));
        }

        Map<Long, List<StatisticsCardStatusCountProjection>> cardByOrg = new HashMap<>();
        for (StatisticsOrganizationCardStatusCountProjection p : statisticsCardRepository
                .countGroupedByOrganizationCells(organizationIds, range.fromInclusive(), range.toExclusive())) {
            cardByOrg
                    .computeIfAbsent(p.organizationId(), _ -> new ArrayList<>())
                    .add(new StatisticsCardStatusCountProjection(p.formType(), p.status(), p.type(), p.count()));
        }

        Map<Long, List<StatisticsActStatusCountProjection>> actByOrg = new HashMap<>();
        for (StatisticsOrganizationActStatusCountProjection p : statisticsActRepository
                .countGroupedByOrganizationCells(organizationIds, range.fromInclusive(), range.toExclusive())) {
            actByOrg
                    .computeIfAbsent(p.organizationId(), _ -> new ArrayList<>())
                    .add(new StatisticsActStatusCountProjection(p.formType(), p.status(), p.type(), p.count()));
        }

        Map<Long, List<StatisticsForm129CountProjection>> form129ByOrg = new HashMap<>();
        for (StatisticsOrganizationForm129CountProjection p : statisticsForm129Repository
                .countGroupedByOrganization(organizationIds, range.fromInclusive(), range.toExclusive())) {
            form129ByOrg
                    .computeIfAbsent(p.organizationId(), _ -> new ArrayList<>())
                    .add(new StatisticsForm129CountProjection(
                            p.categoryCode(), p.status(), p.total(), p.female(), p.male(), p.under18(), p.adult()));
        }

        Map<Long, StatisticsNodeCounts> result = new HashMap<>();
        for (Long organizationId : organizationIds) {
            result.put(organizationId, rollUp(
                    categoryByOrg.getOrDefault(organizationId, List.of()),
                    cardByOrg.getOrDefault(organizationId, List.of()),
                    actByOrg.getOrDefault(organizationId, List.of()),
                    form129ByOrg.getOrDefault(organizationId, List.of())
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

    private StatisticsNodeCounts rollUp(
            List<StatisticsCategoryCountProjection> categoryRows,
            List<StatisticsCardStatusCountProjection> cardRows,
            List<StatisticsActStatusCountProjection> actRows,
            List<StatisticsForm129CountProjection> form129Rows
    ) {
        return new StatisticsNodeCounts(
                formBlock(StatisticsFormType.FORM058, categoryRows, cardRows, actRows),
                formBlock(StatisticsFormType.FORM0581, categoryRows, cardRows, actRows),
                form129Block(form129Rows)
        );
    }

    private StatisticsFormBlockCounts formBlock(
            StatisticsFormType formType,
            List<StatisticsCategoryCountProjection> categoryRows,
            List<StatisticsCardStatusCountProjection> cardRows,
            List<StatisticsActStatusCountProjection> actRows
    ) {
        StatisticsCounts overall = StatisticsCounts.EMPTY;
        Map<String, StatisticsCounts> byCategoryCode = new HashMap<>();
        for (StatisticsCategoryCountProjection row : categoryRows) {
            if (row.formType() != formType) {
                continue;
            }
            overall = overall.plus(row.counts());
            if (row.categoryCode() != null && knownCategoryCodes.contains(row.categoryCode())) {
                byCategoryCode.merge(row.categoryCode(), row.counts(), StatisticsCounts::plus);
            }
        }

        long cardTotal = 0;
        Map<CardStatus, Long> cardByStatus = new EnumMap<>(CardStatus.class);
        Map<CardType, Long> cardByType = new EnumMap<>(CardType.class);
        for (StatisticsCardStatusCountProjection row : cardRows) {
            if (row.formType() != formType) {
                continue;
            }
            cardTotal += row.count();
            cardByStatus.merge(row.status(), row.count(), Long::sum);
            cardByType.merge(row.type(), row.count(), Long::sum);
        }

        long actTotal = 0;
        Map<ActStatus, Long> actByStatus = new EnumMap<>(ActStatus.class);
        Map<ActType, Long> actByType = new EnumMap<>(ActType.class);
        for (StatisticsActStatusCountProjection row : actRows) {
            if (row.formType() != formType) {
                continue;
            }
            actTotal += row.count();
            actByStatus.merge(row.status(), row.count(), Long::sum);
            actByType.merge(row.type(), row.count(), Long::sum);
        }

        return new StatisticsFormBlockCounts(
                overall, byCategoryCode,
                new StatisticsCardCounts(cardTotal, cardByStatus, cardByType),
                new StatisticsActCounts(actTotal, actByStatus, actByType)
        );
    }

    private StatisticsForm129Counts form129Block(List<StatisticsForm129CountProjection> form129Rows) {
        long total = 0;
        long female = 0;
        long male = 0;
        long under18 = 0;
        long adult = 0;
        Map<Form129Status, Long> byStatus = new EnumMap<>(Form129Status.class);
        Map<String, Long> byCategoryCode = new HashMap<>();

        for (StatisticsForm129CountProjection row : form129Rows) {
            total += row.total();
            female += row.female();
            male += row.male();
            under18 += row.under18();
            adult += row.adult();
            byStatus.merge(row.status(), row.total(), Long::sum);
            if (row.categoryCode() != null && knownCategoryCodes.contains(row.categoryCode())) {
                byCategoryCode.merge(row.categoryCode(), row.total(), Long::sum);
            }
        }

        return new StatisticsForm129Counts(total, female, male, under18, adult, byStatus, byCategoryCode);
    }
}
