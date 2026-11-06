package uz.uzinfocom.app.modules.report.form10.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form10.application.query.Form10ReportQueryService;
import uz.uzinfocom.app.modules.report.form10.application.query.dto.Form10Metric;
import uz.uzinfocom.app.modules.report.form10.application.query.dto.Form10ReportNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Excel export for "Form 10" (geography-first, "Joriy davr"/"Yig'ma" two-
 * block period model) — mirrors {@code Form1ExcelExportSource}'s flatten
 * step, just over {@link Form10ReportNodeResponse}'s two {@code
 * Form10Block}s (total + under-14, each an absolute/intensive/growth% triple).
 */
@Component
@RequiredArgsConstructor
public class Form10ExcelExportSource implements ExcelExportSource<Form10ExportFilter, Form10ReportNodeResponse> {

    private final Form10ReportQueryService form10ReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM10";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form10ExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form10ExportFilter filter, Consumer<Form10ReportNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<Form10ReportNodeResponse> flattenAll(Form10ExportFilter filter) {
        List<Form10ReportNodeResponse> root = form10ReportQueryService.getRoot(
                filter.year(), filter.period(), filter.diagnosisCode(), filter.koef()
        );
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form10ReportQueryService.getChildren(
                        regionCode, districtCode, filter.year(), filter.period(), filter.diagnosisCode(), filter.koef()
                ),
                Form10ReportNodeResponse::code,
                Form10ReportNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<Form10ReportNodeResponse>> availableColumns() {
        String blockCurrent = messageResolver.resolve("report.form10.export.blockCurrent");
        String blockCumulative = messageResolver.resolve("report.form10.export.blockCumulative");
        String scopeTotal = messageResolver.resolve("report.form10.export.scopeTotal");
        String scopeChild = messageResolver.resolve("report.form10.export.scopeChild");

        List<ExcelColumn<Form10ReportNodeResponse>> columns = new ArrayList<>(List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form10ReportNodeResponse::code),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form10ReportNodeResponse::name)
        ));

        columns.addAll(scopeColumns("currentTotal", blockCurrent, scopeTotal, n -> n.current().total()));
        columns.addAll(scopeColumns("currentChild", blockCurrent, scopeChild, n -> n.current().child()));
        columns.addAll(scopeColumns("cumulativeTotal", blockCumulative, scopeTotal, n -> n.cumulative().total()));
        columns.addAll(scopeColumns("cumulativeChild", blockCumulative, scopeChild, n -> n.cumulative().child()));

        return columns;
    }

    /**
     * Builds the six metric columns for one (block, scope) combination, in the same
     * "previous year (abs./rate) — current year (abs./rate) — growth % (abs./rate)" order
     * the frontend table renders, each nested under its own three-level "block → scope →
     * period" group header - matching the frontend's own nested header rows exactly, instead
     * of folding those three levels into one concatenated label.
     */
    private List<ExcelColumn<Form10ReportNodeResponse>> scopeColumns(
            String keyPrefix, String blockLabel, String scopeLabel, Function<Form10ReportNodeResponse, Form10Metric> block
    ) {
        String previousYear = messageResolver.resolve("report.export.block.previousYear");
        String currentYear = messageResolver.resolve("report.export.block.currentYear");
        String growthPercent = messageResolver.resolve("report.form10.export.growthPercent");
        String absolute = messageResolver.resolve("report.form10.export.metricAbsolute");
        String intensive = messageResolver.resolve("report.form10.export.metricIntensive");

        return List.of(
                metric(keyPrefix + "AbsPrev", blockLabel, scopeLabel, previousYear, absolute, block, Form10Metric::absPreviousYear),
                metric(keyPrefix + "IntPrev", blockLabel, scopeLabel, previousYear, intensive, block, Form10Metric::intensivePreviousYear),
                metric(keyPrefix + "AbsCurr", blockLabel, scopeLabel, currentYear, absolute, block, Form10Metric::absCurrentYear),
                metric(keyPrefix + "IntCurr", blockLabel, scopeLabel, currentYear, intensive, block, Form10Metric::intensiveCurrentYear),
                metric(keyPrefix + "AbsGrowth", blockLabel, scopeLabel, growthPercent, absolute, block, Form10Metric::absGrowthPercent),
                metric(keyPrefix + "IntGrowth", blockLabel, scopeLabel, growthPercent, intensive, block, Form10Metric::intensiveGrowthPercent)
        );
    }

    /** Composes a block extractor + metric-field extractor into one Excel column, to keep the 24 metric columns above legible. */
    private ExcelColumn<Form10ReportNodeResponse> metric(
            String key, String blockLabel, String scopeLabel, String periodLabel, String header,
            Function<Form10ReportNodeResponse, Form10Metric> block, Function<Form10Metric, Object> field
    ) {
        return ExcelColumn.of(key, List.of(blockLabel, scopeLabel, periodLabel), header, row -> field.apply(block.apply(row)));
    }

    @Override
    public String sheetName() {
        return "Shakl 10";
    }

    @Override
    public String fileNamePrefix() {
        return "form10_export";
    }
}
