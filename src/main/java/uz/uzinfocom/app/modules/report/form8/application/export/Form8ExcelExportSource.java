package uz.uzinfocom.app.modules.report.form8.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form8.application.query.Form8ReportQueryService;
import uz.uzinfocom.app.modules.report.form8.application.query.dto.Form8ReportNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Form 8" (geography-first, year-over-year, confirmed
 * only) — mirrors {@code Form6ExcelExportSource}; the per-node social
 * category breakdown is intentionally not expanded into extra columns.
 */
@Component
@RequiredArgsConstructor
public class Form8ExcelExportSource implements ExcelExportSource<Form8ExportFilter, Form8ReportNodeResponse> {

    private final Form8ReportQueryService form8ReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM8";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form8ExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form8ExportFilter filter, Consumer<Form8ReportNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<Form8ReportNodeResponse> flattenAll(Form8ExportFilter filter) {
        List<Form8ReportNodeResponse> root = form8ReportQueryService.getRoot(
                filter.from(), filter.to(), filter.diagnosisCode()
        );
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form8ReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to(), filter.diagnosisCode()
                ),
                Form8ReportNodeResponse::code,
                Form8ReportNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<Form8ReportNodeResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form8ReportNodeResponse::code),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form8ReportNodeResponse::name),
                ExcelColumn.of("previousYear", messageResolver.resolve("report.export.block.previousYear"), Form8ReportNodeResponse::previousYear),
                ExcelColumn.of("currentYear", messageResolver.resolve("report.export.block.currentYear"), Form8ReportNodeResponse::currentYear),
                ExcelColumn.of("delta", messageResolver.resolve("report.export.block.delta"), Form8ReportNodeResponse::delta)
        );
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
