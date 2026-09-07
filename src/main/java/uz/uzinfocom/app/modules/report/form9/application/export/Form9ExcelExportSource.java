package uz.uzinfocom.app.modules.report.form9.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form9.application.query.Form9ReportQueryService;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9ReportNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Form 9" (geography-first, year-over-year, two metrics
 * per node — registered + hospitalized) — mirrors {@code
 * Form6ExcelExportSource}; the per-node monthly breakdown is intentionally
 * not expanded into extra columns.
 */
@Component
@RequiredArgsConstructor
public class Form9ExcelExportSource implements ExcelExportSource<Form9ExportFilter, Form9ReportNodeResponse> {

    private final Form9ReportQueryService form9ReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM9";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form9ExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form9ExportFilter filter, Consumer<Form9ReportNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<Form9ReportNodeResponse> flattenAll(Form9ExportFilter filter) {
        List<Form9ReportNodeResponse> root = form9ReportQueryService.getRoot(
                filter.from(), filter.to(), filter.diagnosisCode()
        );
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form9ReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to(), filter.diagnosisCode()
                ),
                Form9ReportNodeResponse::code,
                Form9ReportNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<Form9ReportNodeResponse>> availableColumns() {
        String registered = messageResolver.resolve("report.export.block.registered");
        String hospitalized = messageResolver.resolve("report.export.block.hospitalized");
        return List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form9ReportNodeResponse::code),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form9ReportNodeResponse::name),
                ExcelColumn.of("registeredPreviousYear", metric(registered, "report.export.block.previousYear"), Form9ReportNodeResponse::registeredPreviousYear),
                ExcelColumn.of("registeredCurrentYear", metric(registered, "report.export.block.currentYear"), Form9ReportNodeResponse::registeredCurrentYear),
                ExcelColumn.of("registeredDelta", metric(registered, "report.export.block.difference"), Form9ReportNodeResponse::registeredDelta),
                ExcelColumn.of("hospitalizedPreviousYear", metric(hospitalized, "report.export.block.previousYear"), Form9ReportNodeResponse::hospitalizedPreviousYear),
                ExcelColumn.of("hospitalizedCurrentYear", metric(hospitalized, "report.export.block.currentYear"), Form9ReportNodeResponse::hospitalizedCurrentYear),
                ExcelColumn.of("hospitalizedDelta", metric(hospitalized, "report.export.block.difference"), Form9ReportNodeResponse::hospitalizedDelta)
        );
    }

    private String metric(String prefix, String suffixKey) {
        return prefix + " — " + messageResolver.resolve(suffixKey);
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
