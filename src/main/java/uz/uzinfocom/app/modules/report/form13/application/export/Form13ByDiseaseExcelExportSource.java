package uz.uzinfocom.app.modules.report.form13.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form13.application.query.Form13ByDiseaseReportQueryService;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13ByDiseaseReportNodeResponse;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Form 13 by disease" (disease-first counterpart of Form
 * 13) — structurally identical to {@code Form12ExcelExportSource}.
 */
@Component
@RequiredArgsConstructor
public class Form13ByDiseaseExcelExportSource
        implements ExcelExportSource<Form13ByDiseaseExportFilter, Form13ByDiseaseReportNodeResponse> {

    private final Form13ByDiseaseReportQueryService form13ByDiseaseReportQueryService;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM13_BY_DISEASE";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form13ByDiseaseExportFilter filter) {
        return form13ByDiseaseReportQueryService.getRoot(filter.from(), filter.to()).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form13ByDiseaseExportFilter filter, Consumer<Form13ByDiseaseReportNodeResponse> rowProcessor) {
        form13ByDiseaseReportQueryService.getRoot(filter.from(), filter.to()).forEach(rowProcessor);
    }

    @Override
    public List<ExcelColumn<Form13ByDiseaseReportNodeResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("rowCode", messageResolver.resolve("report.formNoso.export.rowCode"), Form13ByDiseaseReportNodeResponse::rowCode),
                ExcelColumn.of("name", messageResolver.resolve("report.formNoso.export.name"), Form13ByDiseaseReportNodeResponse::name),
                ExcelColumn.of("icd10Display", messageResolver.resolve("report.formNoso.export.icd10Display"), Form13ByDiseaseReportNodeResponse::icd10Display),
                ExcelColumn.of("totalPreviousYear", messageResolver.resolve("report.formNoso.export.totalPreviousYear"), Form13ByDiseaseReportNodeResponse::totalPreviousYear),
                ExcelColumn.of("totalCurrentYear", messageResolver.resolve("report.formNoso.export.totalCurrentYear"), Form13ByDiseaseReportNodeResponse::totalCurrentYear),
                ExcelColumn.of("totalDelta", messageResolver.resolve("report.formNoso.export.totalDelta"), Form13ByDiseaseReportNodeResponse::totalDelta),
                ExcelColumn.of("under14PreviousYear", messageResolver.resolve("report.formNoso.export.under14PreviousYear"), Form13ByDiseaseReportNodeResponse::under14PreviousYear),
                ExcelColumn.of("under14CurrentYear", messageResolver.resolve("report.formNoso.export.under14CurrentYear"), Form13ByDiseaseReportNodeResponse::under14CurrentYear),
                ExcelColumn.of("under14Delta", messageResolver.resolve("report.formNoso.export.under14Delta"), Form13ByDiseaseReportNodeResponse::under14Delta),
                ExcelColumn.of("under18PreviousYear", messageResolver.resolve("report.formNoso.export.under18PreviousYear"), Form13ByDiseaseReportNodeResponse::under18PreviousYear),
                ExcelColumn.of("under18CurrentYear", messageResolver.resolve("report.formNoso.export.under18CurrentYear"), Form13ByDiseaseReportNodeResponse::under18CurrentYear),
                ExcelColumn.of("under18Delta", messageResolver.resolve("report.formNoso.export.under18Delta"), Form13ByDiseaseReportNodeResponse::under18Delta)
        );
    }

    @Override
    public String sheetName() {
        return "Shakl 13";
    }

    @Override
    public String fileNamePrefix() {
        return "form13_by_disease_export";
    }
}
