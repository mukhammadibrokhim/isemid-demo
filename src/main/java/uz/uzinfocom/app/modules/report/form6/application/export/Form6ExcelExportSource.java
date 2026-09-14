package uz.uzinfocom.app.modules.report.form6.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form6.application.query.Form6ReportQueryService;
import uz.uzinfocom.app.modules.report.form6.application.query.Form6ReportQueryService.AgeGroupColumn;
import uz.uzinfocom.app.modules.report.form6.application.query.dto.Form6AgeGroupRowResponse;
import uz.uzinfocom.app.modules.report.form6.application.query.dto.Form6ReportNodeResponse;
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
 * Excel export for "Form 6" (geography-first, year-over-year) — mirrors {@code
 * Form1ExcelExportSource} for the base geography columns (code/name/previous year/current
 * year/delta), plus one extra column group per age bucket (the same breakdown the UI's
 * per-row "Yosh tarkibi" popup shows), fetched for every region- and district-level row via
 * {@link ReportHierarchyExportFlattener}'s node-visitor overload so it happens in the same
 * tree walk instead of a second pass. Organization-level rows (the deepest level this system
 * models) leave the age-group columns blank — {@code Form6ReportQueryService#getAgeBreakdown}
 * only resolves a region or a district, never a single organization, so there is nothing
 * genuine to put there rather than a fabricated number.
 */
@Component
@RequiredArgsConstructor
public class Form6ExcelExportSource implements ExcelExportSource<Form6ExportFilter, Form6ExcelExportSource.Form6ExportRow> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Form6ReportQueryService form6ReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    /**
     * One export row: the node's own overall counts, plus (for a region/district row) its
     * age-group breakdown keyed by {@link Form6AgeGroupRowResponse#code()} — empty for an
     * organization-level row, never {@code null}, so column extractors need no null-check.
     */
    public record Form6ExportRow(Form6ReportNodeResponse node, Map<String, Form6AgeGroupRowResponse> ageGroupsByCode) {
    }

    @Override
    public String exportType() {
        return "FORM6";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form6ExportFilter filter) {
        return buildRows(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form6ExportFilter filter, Consumer<Form6ExportRow> rowProcessor) {
        buildRows(filter).forEach(rowProcessor);
    }

    private List<Form6ExportRow> buildRows(Form6ExportFilter filter) {
        List<Form6ReportNodeResponse> root = form6ReportQueryService.getRoot(
                filter.from(), filter.to(), filter.diagnosisCode()
        );

        Map<String, Map<String, Form6AgeGroupRowResponse>> ageGroupsByNodeCode = new HashMap<>();

        List<Form6ReportNodeResponse> flattened = flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form6ReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to(), filter.diagnosisCode()
                ),
                Form6ReportNodeResponse::code,
                Form6ReportNodeResponse::hasChildren,
                (node, regionCode, districtCode) -> ageGroupsByNodeCode.put(
                        node.code(),
                        form6ReportQueryService.getAgeBreakdown(
                                        regionCode, districtCode, filter.from(), filter.to(), filter.diagnosisCode()
                                )
                                .rows().stream()
                                .collect(Collectors.toMap(Form6AgeGroupRowResponse::code, Function.identity()))
                )
        );

        return flattened.stream()
                .map(node -> new Form6ExportRow(node, ageGroupsByNodeCode.getOrDefault(node.code(), Map.of())))
                .toList();
    }

    @Override
    public List<ExcelColumn<Form6ExportRow>> availableColumns() {
        String countGroup = messageResolver.resolve("report.form6.export.group.count");
        String previousYear = messageResolver.resolve("report.export.block.previousYear");
        String currentYear = messageResolver.resolve("report.export.block.currentYear");
        String delta = messageResolver.resolve("report.export.block.delta");

        List<ExcelColumn<Form6ExportRow>> columns = new ArrayList<>(List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), row -> row.node().code()),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), row -> row.node().name()),
                ExcelColumn.of("previousYear", countGroup, previousYear, row -> row.node().previousYear()),
                ExcelColumn.of("currentYear", countGroup, currentYear, row -> row.node().currentYear()),
                ExcelColumn.of("delta", countGroup, delta, row -> row.node().delta())
        ));

        for (AgeGroupColumn ageGroup : form6ReportQueryService.ageGroupColumns()) {
            columns.add(ExcelColumn.of("ageGroup_" + ageGroup.code() + "_previousYear", ageGroup.label(), previousYear,
                    row -> ageGroupValue(row, ageGroup.code(), Form6AgeGroupRowResponse::previousYear)));
            columns.add(ExcelColumn.of("ageGroup_" + ageGroup.code() + "_currentYear", ageGroup.label(), currentYear,
                    row -> ageGroupValue(row, ageGroup.code(), Form6AgeGroupRowResponse::currentYear)));
            columns.add(ExcelColumn.of("ageGroup_" + ageGroup.code() + "_delta", ageGroup.label(), delta,
                    row -> ageGroupValue(row, ageGroup.code(), Form6AgeGroupRowResponse::delta)));
        }

        return columns;
    }

    private Long ageGroupValue(Form6ExportRow row, String ageGroupCode, ToLongFunction<Form6AgeGroupRowResponse> accessor) {
        Form6AgeGroupRowResponse ageGroup = row.ageGroupsByCode().get(ageGroupCode);
        return ageGroup == null ? null : accessor.applyAsLong(ageGroup);
    }

    @Override
    public List<ExcelTitleBlock> titleBlocks(Form6ExportFilter filter) {
        int lastCol = availableColumns().size() - 1;
        return List.of(
                new ExcelTitleBlock(messageResolver.resolve("report.form6.export.title"), 0, 0, 0, lastCol),
                new ExcelTitleBlock(periodText(filter), 1, 1, 0, lastCol)
        );
    }

    private String periodText(Form6ExportFilter filter) {
        String from = filter.from() == null ? "—" : DATE_FORMAT.format(filter.from());
        String to = filter.to() == null ? "—" : DATE_FORMAT.format(filter.to());
        return messageResolver.resolve("report.form6.export.period", from, to);
    }

    @Override
    public String sheetName() {
        return "Shakl 6";
    }

    @Override
    public String fileNamePrefix() {
        return "form6_export";
    }
}
