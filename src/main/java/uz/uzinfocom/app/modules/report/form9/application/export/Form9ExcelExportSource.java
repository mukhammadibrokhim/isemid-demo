package uz.uzinfocom.app.modules.report.form9.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form9.application.query.Form9ReportQueryService;
import uz.uzinfocom.app.modules.report.form9.application.query.Form9ReportQueryService.MonthColumn;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9MonthRowResponse;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9ReportNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;
import uz.uzinfocom.app.shared.excel.ExcelTitleBlock;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/**
 * Excel export for "Form 9" (geography-first, year-over-year, two metrics per node —
 * registered + hospitalized) — mirrors {@code Form6ExcelExportSource} for the base geography
 * columns (code/name/previous year/current year/delta per metric), plus one extra column group
 * per calendar month (the same breakdown the UI's per-row "Ойлик кесим" popup shows, both
 * metrics), fetched for every region- and district-level row via {@link
 * ReportHierarchyExportFlattener}'s node-visitor overload so it happens in the same tree walk
 * instead of a second pass. Organization-level rows (the deepest level this system models)
 * leave the month columns blank — {@code Form9ReportQueryService#getMonthlyBreakdown} only
 * resolves a region or a district, never a single organization, so there is nothing genuine to
 * put there rather than a fabricated number.
 */
@Component
@RequiredArgsConstructor
public class Form9ExcelExportSource implements ExcelExportSource<Form9ExportFilter, Form9ExcelExportSource.Form9ExportRow> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Form9ReportQueryService form9ReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    /**
     * One export row: the node's own overall counts, plus (for a region/district row) its
     * monthly breakdown keyed by {@link Form9MonthRowResponse#monthCode()} — empty for an
     * organization-level row, never {@code null}, so column extractors need no null-check.
     */
    public record Form9ExportRow(Form9ReportNodeResponse node, Map<String, Form9MonthRowResponse> monthsByCode) {
    }

    @Override
    public String exportType() {
        return "FORM9";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form9ExportFilter filter) {
        return buildRows(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form9ExportFilter filter, Consumer<Form9ExportRow> rowProcessor) {
        buildRows(filter).forEach(rowProcessor);
    }

    private List<Form9ExportRow> buildRows(Form9ExportFilter filter) {
        List<Form9ReportNodeResponse> root = form9ReportQueryService.getRoot(
                filter.from(), filter.to(), filter.diagnosisCode()
        );

        Map<String, Map<String, Form9MonthRowResponse>> monthsByNodeCode = new HashMap<>();

        List<Form9ReportNodeResponse> flattened = flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form9ReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to(), filter.diagnosisCode()
                ),
                Form9ReportNodeResponse::code,
                Form9ReportNodeResponse::hasChildren,
                (node, regionCode, districtCode) -> monthsByNodeCode.put(
                        node.code(),
                        form9ReportQueryService.getMonthlyBreakdown(
                                        regionCode, districtCode, filter.from(), filter.to(), filter.diagnosisCode()
                                )
                                .rows().stream()
                                .collect(Collectors.toMap(Form9MonthRowResponse::monthCode, Function.identity()))
                )
        );

        return flattened.stream()
                .map(node -> new Form9ExportRow(node, monthsByNodeCode.getOrDefault(node.code(), Map.of())))
                .toList();
    }

    @Override
    public List<ExcelColumn<Form9ExportRow>> availableColumns() {
        String registered = messageResolver.resolve("report.export.block.registered");
        String hospitalized = messageResolver.resolve("report.export.block.hospitalized");
        String previousYear = messageResolver.resolve("report.export.block.previousYear");
        String currentYear = messageResolver.resolve("report.export.block.currentYear");
        String difference = messageResolver.resolve("report.export.block.difference");

        List<ExcelColumn<Form9ExportRow>> columns = new ArrayList<>(List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), row -> row.node().code()),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), row -> row.node().name()),
                ExcelColumn.of("registeredPreviousYear", registered, previousYear, row -> row.node().registeredPreviousYear()),
                ExcelColumn.of("registeredCurrentYear", registered, currentYear, row -> row.node().registeredCurrentYear()),
                ExcelColumn.of("registeredDelta", registered, difference, row -> row.node().registeredDelta()),
                ExcelColumn.of("hospitalizedPreviousYear", hospitalized, previousYear, row -> row.node().hospitalizedPreviousYear()),
                ExcelColumn.of("hospitalizedCurrentYear", hospitalized, currentYear, row -> row.node().hospitalizedCurrentYear()),
                ExcelColumn.of("hospitalizedDelta", hospitalized, difference, row -> row.node().hospitalizedDelta())
        ));

        for (MonthColumn month : form9ReportQueryService.monthColumns()) {
            String registeredGroup = month.label() + " — " + registered;
            String hospitalizedGroup = month.label() + " — " + hospitalized;
            columns.add(ExcelColumn.of("month_" + month.code() + "_registeredPreviousYear", registeredGroup, previousYear,
                    row -> monthValue(row, month.code(), Form9MonthRowResponse::registeredPreviousYear)));
            columns.add(ExcelColumn.of("month_" + month.code() + "_registeredCurrentYear", registeredGroup, currentYear,
                    row -> monthValue(row, month.code(), Form9MonthRowResponse::registeredCurrentYear)));
            columns.add(ExcelColumn.of("month_" + month.code() + "_registeredDelta", registeredGroup, difference,
                    row -> monthValue(row, month.code(), Form9MonthRowResponse::registeredDelta)));
            columns.add(ExcelColumn.of("month_" + month.code() + "_hospitalizedPreviousYear", hospitalizedGroup, previousYear,
                    row -> monthValue(row, month.code(), Form9MonthRowResponse::hospitalizedPreviousYear)));
            columns.add(ExcelColumn.of("month_" + month.code() + "_hospitalizedCurrentYear", hospitalizedGroup, currentYear,
                    row -> monthValue(row, month.code(), Form9MonthRowResponse::hospitalizedCurrentYear)));
            columns.add(ExcelColumn.of("month_" + month.code() + "_hospitalizedDelta", hospitalizedGroup, difference,
                    row -> monthValue(row, month.code(), Form9MonthRowResponse::hospitalizedDelta)));
        }

        return columns;
    }

    private Long monthValue(Form9ExportRow row, String monthCode, ToLongFunction<Form9MonthRowResponse> accessor) {
        Form9MonthRowResponse month = row.monthsByCode().get(monthCode);
        return month == null ? null : accessor.applyAsLong(month);
    }

    @Override
    public List<ExcelTitleBlock> titleBlocks(Form9ExportFilter filter) {
        int lastCol = availableColumns().size() - 1;
        return List.of(
                new ExcelTitleBlock(messageResolver.resolve("report.form9.export.title"), 0, 0, 0, lastCol),
                new ExcelTitleBlock(periodText(filter), 1, 1, 0, lastCol)
        );
    }

    private String periodText(Form9ExportFilter filter) {
        String from = filter.from() == null ? "—" : DATE_FORMAT.format(filter.from());
        String to = filter.to() == null ? "—" : DATE_FORMAT.format(filter.to());
        return messageResolver.resolve("report.form9.export.period", from, to);
    }

    @Override
    public String sheetName() {
        return "Shakl 9";
    }

    @Override
    public String fileNamePrefix() {
        return "form9_export";
    }
}
