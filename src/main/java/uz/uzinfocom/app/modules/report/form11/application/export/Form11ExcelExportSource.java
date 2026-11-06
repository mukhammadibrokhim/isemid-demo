package uz.uzinfocom.app.modules.report.form11.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form11.application.query.Form11ReportQueryService;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11Metric;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11ReportNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Excel export for "Form 11" (geography-first, "Joriy davr"/"Yig'ma" two-
 * block period model) — mirrors {@code Form10ExcelExportSource}'s flatten
 * step and column layout exactly, just over {@link
 * Form11ReportNodeResponse}'s two {@code Form11Block}s (total + city + rural
 * + child, each an absolute/intensive/growth% triple).
 */
@Component
@RequiredArgsConstructor
public class Form11ExcelExportSource implements ExcelExportSource<Form11ExportFilter, Form11ReportNodeResponse> {

    private final Form11ReportQueryService form11ReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM11";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form11ExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form11ExportFilter filter, Consumer<Form11ReportNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<Form11ReportNodeResponse> flattenAll(Form11ExportFilter filter) {
        List<Form11ReportNodeResponse> root = form11ReportQueryService.getRoot(
                filter.year(), filter.period(), filter.diagnosisCode(), filter.koef()
        );
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form11ReportQueryService.getChildren(
                        regionCode, districtCode, filter.year(), filter.period(), filter.diagnosisCode(), filter.koef()
                ),
                Form11ReportNodeResponse::code,
                Form11ReportNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<Form11ReportNodeResponse>> availableColumns() {
        String blockCurrent = messageResolver.resolve("report.form10.export.blockCurrent");
        String blockCumulative = messageResolver.resolve("report.form10.export.blockCumulative");
        String scopeTotal = messageResolver.resolve("report.form11.export.scopeTotal");
        String scopeCity = messageResolver.resolve("report.form11.export.scopeCity");
        String scopeRural = messageResolver.resolve("report.form11.export.scopeRural");
        String scopeChild = messageResolver.resolve("report.form11.export.scopeChild");

        List<ExcelColumn<Form11ReportNodeResponse>> columns = new ArrayList<>(List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form11ReportNodeResponse::code),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form11ReportNodeResponse::name)
        ));

        columns.addAll(scopeColumns("currentTotal", blockCurrent, scopeTotal, n -> n.current().total()));
        columns.addAll(scopeColumns("currentCity", blockCurrent, scopeCity, n -> n.current().city()));
        columns.addAll(scopeColumns("currentRural", blockCurrent, scopeRural, n -> n.current().rural()));
        columns.addAll(scopeColumns("currentChild", blockCurrent, scopeChild, n -> n.current().child()));
        columns.addAll(scopeColumns("cumulativeTotal", blockCumulative, scopeTotal, n -> n.cumulative().total()));
        columns.addAll(scopeColumns("cumulativeCity", blockCumulative, scopeCity, n -> n.cumulative().city()));
        columns.addAll(scopeColumns("cumulativeRural", blockCumulative, scopeRural, n -> n.cumulative().rural()));
        columns.addAll(scopeColumns("cumulativeChild", blockCumulative, scopeChild, n -> n.cumulative().child()));

        return columns;
    }

    /**
     * Builds the six metric columns for one (block, scope) combination, in the same
     * "previous year (abs./rate) — current year (abs./rate) — growth % (abs./rate)" order
     * the frontend table renders, each nested under its own three-level "block → scope →
     * period" group header — matching {@code Form10ExcelExportSource#scopeColumns} exactly.
     */
    private List<ExcelColumn<Form11ReportNodeResponse>> scopeColumns(
            String keyPrefix, String blockLabel, String scopeLabel, Function<Form11ReportNodeResponse, Form11Metric> block
    ) {
        String previousYear = messageResolver.resolve("report.export.block.previousYear");
        String currentYear = messageResolver.resolve("report.export.block.currentYear");
        String growthPercent = messageResolver.resolve("report.form10.export.growthPercent");
        String absolute = messageResolver.resolve("report.form10.export.metricAbsolute");
        String intensive = messageResolver.resolve("report.form10.export.metricIntensive");

        return List.of(
                metric(keyPrefix + "AbsPrev", blockLabel, scopeLabel, previousYear, absolute, block, Form11Metric::absPreviousYear),
                metric(keyPrefix + "IntPrev", blockLabel, scopeLabel, previousYear, intensive, block, Form11Metric::intensivePreviousYear),
                metric(keyPrefix + "AbsCurr", blockLabel, scopeLabel, currentYear, absolute, block, Form11Metric::absCurrentYear),
                metric(keyPrefix + "IntCurr", blockLabel, scopeLabel, currentYear, intensive, block, Form11Metric::intensiveCurrentYear),
                metric(keyPrefix + "AbsGrowth", blockLabel, scopeLabel, growthPercent, absolute, block, Form11Metric::absGrowthPercent),
                metric(keyPrefix + "IntGrowth", blockLabel, scopeLabel, growthPercent, intensive, block, Form11Metric::intensiveGrowthPercent)
        );
    }

    /** Composes a block extractor + metric-field extractor into one Excel column, to keep the 48 metric columns above legible. */
    private ExcelColumn<Form11ReportNodeResponse> metric(
            String key, String blockLabel, String scopeLabel, String periodLabel, String header,
            Function<Form11ReportNodeResponse, Form11Metric> block, Function<Form11Metric, Object> field
    ) {
        return ExcelColumn.of(key, List.of(blockLabel, scopeLabel, periodLabel), header, row -> field.apply(block.apply(row)));
    }

    @Override
    public String sheetName() {
        return "Shakl 11";
    }

    @Override
    public String fileNamePrefix() {
        return "form11_export";
    }
}
