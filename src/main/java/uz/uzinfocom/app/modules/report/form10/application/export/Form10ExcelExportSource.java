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
        return List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form10ReportNodeResponse::code),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form10ReportNodeResponse::name),
                metric("currentTotalAbsPrev", h("blockCurrent", "scopeTotal", "absPrev"), n -> n.current().total(), Form10Metric::absPreviousYear),
                metric("currentTotalAbsCurr", h("blockCurrent", "scopeTotal", "absCurr"), n -> n.current().total(), Form10Metric::absCurrentYear),
                metric("currentTotalAbsGrowth", h("blockCurrent", "scopeTotal", "absGrowth"), n -> n.current().total(), Form10Metric::absGrowthPercent),
                metric("currentTotalIntPrev", h("blockCurrent", "scopeTotal", "intPrev"), n -> n.current().total(), Form10Metric::intensivePreviousYear),
                metric("currentTotalIntCurr", h("blockCurrent", "scopeTotal", "intCurr"), n -> n.current().total(), Form10Metric::intensiveCurrentYear),
                metric("currentTotalIntGrowth", h("blockCurrent", "scopeTotal", "intGrowth"), n -> n.current().total(), Form10Metric::intensiveGrowthPercent),
                metric("currentChildAbsPrev", h("blockCurrent", "scopeChild", "absPrev"), n -> n.current().child(), Form10Metric::absPreviousYear),
                metric("currentChildAbsCurr", h("blockCurrent", "scopeChild", "absCurr"), n -> n.current().child(), Form10Metric::absCurrentYear),
                metric("currentChildAbsGrowth", h("blockCurrent", "scopeChild", "absGrowth"), n -> n.current().child(), Form10Metric::absGrowthPercent),
                metric("currentChildIntPrev", h("blockCurrent", "scopeChild", "intPrev"), n -> n.current().child(), Form10Metric::intensivePreviousYear),
                metric("currentChildIntCurr", h("blockCurrent", "scopeChild", "intCurr"), n -> n.current().child(), Form10Metric::intensiveCurrentYear),
                metric("currentChildIntGrowth", h("blockCurrent", "scopeChild", "intGrowth"), n -> n.current().child(), Form10Metric::intensiveGrowthPercent),
                metric("cumulativeTotalAbsPrev", h("blockCumulative", "scopeTotal", "absPrev"), n -> n.cumulative().total(), Form10Metric::absPreviousYear),
                metric("cumulativeTotalAbsCurr", h("blockCumulative", "scopeTotal", "absCurr"), n -> n.cumulative().total(), Form10Metric::absCurrentYear),
                metric("cumulativeTotalAbsGrowth", h("blockCumulative", "scopeTotal", "absGrowth"), n -> n.cumulative().total(), Form10Metric::absGrowthPercent),
                metric("cumulativeTotalIntPrev", h("blockCumulative", "scopeTotal", "intPrev"), n -> n.cumulative().total(), Form10Metric::intensivePreviousYear),
                metric("cumulativeTotalIntCurr", h("blockCumulative", "scopeTotal", "intCurr"), n -> n.cumulative().total(), Form10Metric::intensiveCurrentYear),
                metric("cumulativeTotalIntGrowth", h("blockCumulative", "scopeTotal", "intGrowth"), n -> n.cumulative().total(), Form10Metric::intensiveGrowthPercent),
                metric("cumulativeChildAbsPrev", h("blockCumulative", "scopeChild", "absPrev"), n -> n.cumulative().child(), Form10Metric::absPreviousYear),
                metric("cumulativeChildAbsCurr", h("blockCumulative", "scopeChild", "absCurr"), n -> n.cumulative().child(), Form10Metric::absCurrentYear),
                metric("cumulativeChildAbsGrowth", h("blockCumulative", "scopeChild", "absGrowth"), n -> n.cumulative().child(), Form10Metric::absGrowthPercent),
                metric("cumulativeChildIntPrev", h("blockCumulative", "scopeChild", "intPrev"), n -> n.cumulative().child(), Form10Metric::intensivePreviousYear),
                metric("cumulativeChildIntCurr", h("blockCumulative", "scopeChild", "intCurr"), n -> n.cumulative().child(), Form10Metric::intensiveCurrentYear),
                metric("cumulativeChildIntGrowth", h("blockCumulative", "scopeChild", "intGrowth"), n -> n.cumulative().child(), Form10Metric::intensiveGrowthPercent)
        );
    }

    /** Composes a block extractor + metric-field extractor into one Excel column, to keep the 24 metric columns above legible. */
    private ExcelColumn<Form10ReportNodeResponse> metric(
            String key, String header, Function<Form10ReportNodeResponse, Form10Metric> block, Function<Form10Metric, Object> field
    ) {
        return ExcelColumn.of(key, header, row -> field.apply(block.apply(row)));
    }

    /** Builds a "&lt;block&gt; — &lt;scope&gt; — &lt;metric&gt;" header from three {@code report.form10.export.*} fragment keys. */
    private String h(String blockKey, String scopeKey, String metricKey) {
        return messageResolver.resolve("report.form10.export." + blockKey)
                + " — " + messageResolver.resolve("report.form10.export." + scopeKey)
                + " — " + messageResolver.resolve("report.form10.export." + metricKey);
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
