package uz.uzinfocom.app.modules.report.form6.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form6.application.query.Form6ReportQueryService;
import uz.uzinfocom.app.modules.report.form6.application.query.dto.Form6ReportNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Form 6" (geography-first, year-over-year) — mirrors
 * {@code Form1ExcelExportSource}. Exports only the geography tree, the same
 * rows/columns {@code root}/{@code children} return; the per-node age-group
 * breakdown ({@code ageBreakdown}) is a separate drill-down endpoint and is
 * intentionally not expanded into extra export columns (would need one
 * column set per node, per age group — out of scope for this pass).
 */
@Component
@RequiredArgsConstructor
public class Form6ExcelExportSource implements ExcelExportSource<Form6ExportFilter, Form6ReportNodeResponse> {

    private final Form6ReportQueryService form6ReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM6";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form6ExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form6ExportFilter filter, Consumer<Form6ReportNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<Form6ReportNodeResponse> flattenAll(Form6ExportFilter filter) {
        List<Form6ReportNodeResponse> root = form6ReportQueryService.getRoot(
                filter.from(), filter.to(), filter.diagnosisCode()
        );
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form6ReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to(), filter.diagnosisCode()
                ),
                Form6ReportNodeResponse::code,
                Form6ReportNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<Form6ReportNodeResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form6ReportNodeResponse::code),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form6ReportNodeResponse::name),
                ExcelColumn.of("previousYear", messageResolver.resolve("report.export.block.previousYear"), Form6ReportNodeResponse::previousYear),
                ExcelColumn.of("currentYear", messageResolver.resolve("report.export.block.currentYear"), Form6ReportNodeResponse::currentYear),
                ExcelColumn.of("delta", messageResolver.resolve("report.export.block.delta"), Form6ReportNodeResponse::delta)
        );
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
