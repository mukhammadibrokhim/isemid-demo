package uz.uzinfocom.app.modules.report.statistics.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.modules.report.statistics.application.query.StatisticsReportQueryService;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsForm129BlockResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsFormBlockResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsNodeResponse;
import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsPeriodCountsResponse;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Excel export for "Statistika" (geography-first, two independently chosen
 * periods "Davr A"/"Davr B"). Forms №058, №058-1 and №129 are exported as
 * three separate column groups per period, mirroring the API response shape
 * ({@link StatisticsPeriodCountsResponse}) — only the top-level per-block
 * counts (confirmed/primary totals, age/gender breakdown, cards/acts totals
 * for 058/058-1) are exported as columns; the dynamic per-category
 * breakdown and the cards/acts by-status/by-type breakdown are, like
 * form6/8/9's per-node breakdowns, intentionally not expanded into extra
 * columns in this pass. "Davr B" columns are blank when the caller didn't
 * request a comparison period ({@code periodB} is {@code null} in that case).
 */
@Component
@RequiredArgsConstructor
public class StatisticsExcelExportSource implements ExcelExportSource<StatisticsExportFilter, StatisticsNodeResponse> {

    private final StatisticsReportQueryService statisticsReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "STATISTICS";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(StatisticsExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(StatisticsExportFilter filter, Consumer<StatisticsNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<StatisticsNodeResponse> flattenAll(StatisticsExportFilter filter) {
        List<StatisticsNodeResponse> root = statisticsReportQueryService.getRoot(
                filter.fromA(), filter.toA(), filter.fromB(), filter.toB(),
                filter.genderCode(), filter.ageGroup(), filter.categoryCode()
        );
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> statisticsReportQueryService.getChildren(
                        regionCode, districtCode, filter.fromA(), filter.toA(), filter.fromB(), filter.toB(),
                        filter.genderCode(), filter.ageGroup(), filter.categoryCode()
                ),
                StatisticsNodeResponse::code,
                StatisticsNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<StatisticsNodeResponse>> availableColumns() {
        String periodA = messageResolver.resolve("report.statistics.export.periodA");
        String periodB = messageResolver.resolve("report.statistics.export.periodB");

        List<ExcelColumn<StatisticsNodeResponse>> columns = new ArrayList<>();
        columns.add(ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), StatisticsNodeResponse::code));
        columns.add(ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), StatisticsNodeResponse::name));

        columns.addAll(formBlockColumns("A", periodA, StatisticsNodeResponse::periodA, "058", StatisticsPeriodCountsResponse::form058));
        columns.addAll(formBlockColumns("A", periodA, StatisticsNodeResponse::periodA, "0581", StatisticsPeriodCountsResponse::form0581));
        columns.addAll(form129Columns("A", periodA, StatisticsNodeResponse::periodA));

        columns.addAll(formBlockColumns("B", periodB, StatisticsNodeResponse::periodB, "058", StatisticsPeriodCountsResponse::form058));
        columns.addAll(formBlockColumns("B", periodB, StatisticsNodeResponse::periodB, "0581", StatisticsPeriodCountsResponse::form0581));
        columns.addAll(form129Columns("B", periodB, StatisticsNodeResponse::periodB));

        return columns;
    }

    /** One period's form058/form058-1 block, as {@code period<A|B><058|0581><Field>} columns. */
    private List<ExcelColumn<StatisticsNodeResponse>> formBlockColumns(
            String periodKey, String periodLabel,
            Function<StatisticsNodeResponse, StatisticsPeriodCountsResponse> period,
            String formKey,
            Function<StatisticsPeriodCountsResponse, StatisticsFormBlockResponse> block
    ) {
        String formLabel = messageResolver.resolve("report.statistics.export.form" + formKey);
        String keyPrefix = "period" + periodKey + formKey;

        return List.of(
                periodColumn(keyPrefix + "ConfirmedTotal", header(periodLabel, formLabel, "subConfirmedTotal"),
                        period, p -> block.apply(p).confirmedTotal()),
                periodColumn(keyPrefix + "PrimaryTotal", header(periodLabel, formLabel, "subPrimaryTotal"),
                        period, p -> block.apply(p).primaryTotal()),
                periodColumn(keyPrefix + "ConfirmedUnder18", header(periodLabel, formLabel, "subConfirmedUnder18"),
                        period, p -> block.apply(p).ageBreakdown().confirmedUnder18()),
                periodColumn(keyPrefix + "ConfirmedAdult", header(periodLabel, formLabel, "subConfirmedAdult"),
                        period, p -> block.apply(p).ageBreakdown().confirmedAdult()),
                periodColumn(keyPrefix + "PrimaryUnder18", header(periodLabel, formLabel, "subPrimaryUnder18"),
                        period, p -> block.apply(p).ageBreakdown().primaryUnder18()),
                periodColumn(keyPrefix + "PrimaryAdult", header(periodLabel, formLabel, "subPrimaryAdult"),
                        period, p -> block.apply(p).ageBreakdown().primaryAdult()),
                periodColumn(keyPrefix + "ConfirmedFemale", header(periodLabel, formLabel, "subConfirmedFemale"),
                        period, p -> block.apply(p).genderBreakdown().confirmedFemale()),
                periodColumn(keyPrefix + "ConfirmedMale", header(periodLabel, formLabel, "subConfirmedMale"),
                        period, p -> block.apply(p).genderBreakdown().confirmedMale()),
                periodColumn(keyPrefix + "PrimaryFemale", header(periodLabel, formLabel, "subPrimaryFemale"),
                        period, p -> block.apply(p).genderBreakdown().primaryFemale()),
                periodColumn(keyPrefix + "PrimaryMale", header(periodLabel, formLabel, "subPrimaryMale"),
                        period, p -> block.apply(p).genderBreakdown().primaryMale()),
                periodColumn(keyPrefix + "CardsTotal", header(periodLabel, formLabel, "subCardsTotal"),
                        period, p -> block.apply(p).cardsTotal()),
                periodColumn(keyPrefix + "ActsTotal", header(periodLabel, formLabel, "subActsTotal"),
                        period, p -> block.apply(p).actsTotal())
        );
    }

    /** One period's form129 block, as {@code period<A|B>129<Field>} columns. */
    private List<ExcelColumn<StatisticsNodeResponse>> form129Columns(
            String periodKey, String periodLabel,
            Function<StatisticsNodeResponse, StatisticsPeriodCountsResponse> period
    ) {
        String formLabel = messageResolver.resolve("report.statistics.export.form129");
        String keyPrefix = "period" + periodKey + "129";
        Function<StatisticsPeriodCountsResponse, StatisticsForm129BlockResponse> block = StatisticsPeriodCountsResponse::form129;

        return List.of(
                periodColumn(keyPrefix + "Total", header(periodLabel, formLabel, "subTotal"),
                        period, p -> block.apply(p).total()),
                periodColumn(keyPrefix + "Under18", header(periodLabel, formLabel, "subUnder18"),
                        period, p -> block.apply(p).under18()),
                periodColumn(keyPrefix + "Adult", header(periodLabel, formLabel, "subAdult"),
                        period, p -> block.apply(p).adult()),
                periodColumn(keyPrefix + "Female", header(periodLabel, formLabel, "subFemale"),
                        period, p -> block.apply(p).female()),
                periodColumn(keyPrefix + "Male", header(periodLabel, formLabel, "subMale"),
                        period, p -> block.apply(p).male())
        );
    }

    private String header(String periodLabel, String formLabel, String subKey) {
        return periodLabel + " / " + formLabel + " — " + messageResolver.resolve("report.statistics.export." + subKey);
    }

    /** Null-safe: "Davr B" columns are blank (not an error) when the caller didn't request a comparison period. */
    private ExcelColumn<StatisticsNodeResponse> periodColumn(
            String key, String header,
            Function<StatisticsNodeResponse, StatisticsPeriodCountsResponse> period,
            Function<StatisticsPeriodCountsResponse, Object> field
    ) {
        return ExcelColumn.of(key, header, row -> {
            StatisticsPeriodCountsResponse counts = period.apply(row);
            return counts == null ? null : field.apply(counts);
        });
    }

    @Override
    public String sheetName() {
        return "Statistika";
    }

    @Override
    public String fileNamePrefix() {
        return "statistics_export";
    }
}
