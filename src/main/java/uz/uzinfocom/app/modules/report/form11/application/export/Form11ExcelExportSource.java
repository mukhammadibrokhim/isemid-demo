package uz.uzinfocom.app.modules.report.form11.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form11.application.query.Form11ReportQueryService;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11ReportNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Excel export for "Form 11" (geography-first, year-over-year morbidity
 * indicators + urban/rural/under-18 cuts) — mirrors {@code
 * Form1ExcelExportSource}'s flatten step, over {@link
 * Form11ReportNodeResponse}'s 16 fields.
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
                filter.from(), filter.to(), filter.diagnosisCode(), filter.koef(), filter.population()
        );
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form11ReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to(),
                        filter.diagnosisCode(), filter.koef(), filter.population()
                ),
                Form11ReportNodeResponse::code,
                Form11ReportNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<Form11ReportNodeResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form11ReportNodeResponse::code),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form11ReportNodeResponse::name),
                col("absPreviousYear", "report.form11.export.absPreviousYear", Form11ReportNodeResponse::absPreviousYear),
                col("absCurrentYear", "report.form11.export.absCurrentYear", Form11ReportNodeResponse::absCurrentYear),
                col("absGrowthPercent", "report.form11.export.absGrowthPercent", Form11ReportNodeResponse::absGrowthPercent),
                col("intensivePreviousYear", "report.form11.export.intensivePreviousYear", Form11ReportNodeResponse::intensivePreviousYear),
                col("intensiveCurrentYear", "report.form11.export.intensiveCurrentYear", Form11ReportNodeResponse::intensiveCurrentYear),
                col("intensiveGrowthPercent", "report.form11.export.intensiveGrowthPercent", Form11ReportNodeResponse::intensiveGrowthPercent),
                col("cityAbs", "report.form11.export.cityAbs", Form11ReportNodeResponse::cityAbs),
                col("cityIntensive", "report.form11.export.cityIntensive", Form11ReportNodeResponse::cityIntensive),
                col("citySharePercent", "report.form11.export.citySharePercent", Form11ReportNodeResponse::citySharePercent),
                col("ruralAbs", "report.form11.export.ruralAbs", Form11ReportNodeResponse::ruralAbs),
                col("ruralIntensive", "report.form11.export.ruralIntensive", Form11ReportNodeResponse::ruralIntensive),
                col("ruralSharePercent", "report.form11.export.ruralSharePercent", Form11ReportNodeResponse::ruralSharePercent),
                col("childAbs", "report.form11.export.childAbs", Form11ReportNodeResponse::childAbs),
                col("childIntensive", "report.form11.export.childIntensive", Form11ReportNodeResponse::childIntensive)
        );
    }

    private ExcelColumn<Form11ReportNodeResponse> col(String key, String headerKey, Function<Form11ReportNodeResponse, Object> extractor) {
        return ExcelColumn.of(key, messageResolver.resolve(headerKey), extractor);
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
