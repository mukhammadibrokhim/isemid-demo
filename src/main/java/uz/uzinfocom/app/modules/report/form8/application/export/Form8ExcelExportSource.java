package uz.uzinfocom.app.modules.report.form8.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form8.application.query.Form8ReportQueryService;
import uz.uzinfocom.app.modules.report.form8.application.query.Form8ReportQueryService.CategoryColumn;
import uz.uzinfocom.app.modules.report.form8.application.query.dto.Form8CategoryRowResponse;
import uz.uzinfocom.app.modules.report.form8.application.query.dto.Form8ReportNodeResponse;
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
 * Excel export for "Form 8" (geography-first, year-over-year, confirmed only) — mirrors {@code
 * Form6ExcelExportSource} for the base geography columns (code/name/previous year/current
 * year/delta), plus one extra column group per social category (the same breakdown the UI's
 * per-row "Ijtimoiy tarkib" popup shows), fetched for every region- and district-level row via
 * {@link ReportHierarchyExportFlattener}'s node-visitor overload so it happens in the same tree
 * walk instead of a second pass. Organization-level rows (the deepest level this system models)
 * leave the category columns blank — {@code Form8ReportQueryService#getCategoryBreakdown} only
 * resolves a region or a district, never a single organization, so there is nothing genuine to
 * put there rather than a fabricated number.
 */
@Component
@RequiredArgsConstructor
public class Form8ExcelExportSource implements ExcelExportSource<Form8ExportFilter, Form8ExcelExportSource.Form8ExportRow> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Form8ReportQueryService form8ReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    /**
     * One export row: the node's own overall counts, plus (for a region/district row) its
     * social-category breakdown keyed by {@link Form8CategoryRowResponse#code()} — empty for an
     * organization-level row, never {@code null}, so column extractors need no null-check.
     */
    public record Form8ExportRow(Form8ReportNodeResponse node, Map<String, Form8CategoryRowResponse> categoriesByCode) {
    }

    @Override
    public String exportType() {
        return "FORM8";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form8ExportFilter filter) {
        return buildRows(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form8ExportFilter filter, Consumer<Form8ExportRow> rowProcessor) {
        buildRows(filter).forEach(rowProcessor);
    }

    private List<Form8ExportRow> buildRows(Form8ExportFilter filter) {
        List<Form8ReportNodeResponse> root = form8ReportQueryService.getRoot(
                filter.from(), filter.to(), filter.diagnosisCode()
        );

        Map<String, Map<String, Form8CategoryRowResponse>> categoriesByNodeCode = new HashMap<>();

        List<Form8ReportNodeResponse> flattened = flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form8ReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to(), filter.diagnosisCode()
                ),
                Form8ReportNodeResponse::code,
                Form8ReportNodeResponse::hasChildren,
                (node, regionCode, districtCode) -> categoriesByNodeCode.put(
                        node.code(),
                        form8ReportQueryService.getCategoryBreakdown(
                                        regionCode, districtCode, filter.from(), filter.to(), filter.diagnosisCode()
                                )
                                .rows().stream()
                                .collect(Collectors.toMap(Form8CategoryRowResponse::code, Function.identity()))
                )
        );

        return flattened.stream()
                .map(node -> new Form8ExportRow(node, categoriesByNodeCode.getOrDefault(node.code(), Map.of())))
                .toList();
    }

    @Override
    public List<ExcelColumn<Form8ExportRow>> availableColumns() {
        String countGroup = messageResolver.resolve("report.form8.export.group.count");
        String previousYear = messageResolver.resolve("report.export.block.previousYear");
        String currentYear = messageResolver.resolve("report.export.block.currentYear");
        String delta = messageResolver.resolve("report.export.block.delta");

        List<ExcelColumn<Form8ExportRow>> columns = new ArrayList<>(List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), row -> row.node().code()),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), row -> row.node().name()),
                ExcelColumn.of("previousYear", countGroup, previousYear, row -> row.node().previousYear()),
                ExcelColumn.of("currentYear", countGroup, currentYear, row -> row.node().currentYear()),
                ExcelColumn.of("delta", countGroup, delta, row -> row.node().delta())
        ));

        for (CategoryColumn category : form8ReportQueryService.categoryColumns()) {
            columns.add(ExcelColumn.of("category_" + category.code() + "_previousYear", category.label(), previousYear,
                    row -> categoryValue(row, category.code(), Form8CategoryRowResponse::previousYear)));
            columns.add(ExcelColumn.of("category_" + category.code() + "_currentYear", category.label(), currentYear,
                    row -> categoryValue(row, category.code(), Form8CategoryRowResponse::currentYear)));
            columns.add(ExcelColumn.of("category_" + category.code() + "_delta", category.label(), delta,
                    row -> categoryValue(row, category.code(), Form8CategoryRowResponse::delta)));
        }

        return columns;
    }

    private Long categoryValue(Form8ExportRow row, String categoryCode, ToLongFunction<Form8CategoryRowResponse> accessor) {
        Form8CategoryRowResponse category = row.categoriesByCode().get(categoryCode);
        return category == null ? null : accessor.applyAsLong(category);
    }

    @Override
    public List<ExcelTitleBlock> titleBlocks(Form8ExportFilter filter) {
        int lastCol = availableColumns().size() - 1;
        return List.of(
                new ExcelTitleBlock(messageResolver.resolve("report.form8.export.title"), 0, 0, 0, lastCol),
                new ExcelTitleBlock(periodText(filter), 1, 1, 0, lastCol)
        );
    }

    private String periodText(Form8ExportFilter filter) {
        String from = filter.from() == null ? "—" : DATE_FORMAT.format(filter.from());
        String to = filter.to() == null ? "—" : DATE_FORMAT.format(filter.to());
        return messageResolver.resolve("report.form8.export.period", from, to);
    }

    @Override
    public String sheetName() {
        return "Shakl 8";
    }

    @Override
    public String fileNamePrefix() {
        return "form8_export";
    }
}
