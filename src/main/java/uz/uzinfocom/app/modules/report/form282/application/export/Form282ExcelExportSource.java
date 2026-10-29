package uz.uzinfocom.app.modules.report.form282.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form282.application.query.Form282ReportQueryService;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282ReportNodeResponse;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Form 28.2" (disease-first, single period, nosocomial
 * infection varaqa columns) — structurally identical to {@code
 * Form12ExcelExportSource}.
 */
@Component
@RequiredArgsConstructor
public class Form282ExcelExportSource implements ExcelExportSource<Form282ExportFilter, Form282ReportNodeResponse> {

    private final Form282ReportQueryService form282ReportQueryService;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM282";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form282ExportFilter filter) {
        return form282ReportQueryService.getRoot(filter.from(), filter.to()).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form282ExportFilter filter, Consumer<Form282ReportNodeResponse> rowProcessor) {
        form282ReportQueryService.getRoot(filter.from(), filter.to()).forEach(rowProcessor);
    }

    @Override
    public List<ExcelColumn<Form282ReportNodeResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("rowCode", messageResolver.resolve("report.formNoso.export.rowCode"), Form282ReportNodeResponse::rowCode),
                ExcelColumn.of("name", messageResolver.resolve("report.formNoso.export.name"), Form282ReportNodeResponse::name),
                ExcelColumn.of("icd10Display", messageResolver.resolve("report.formNoso.export.icd10Display"), Form282ReportNodeResponse::icd10Display),
                ExcelColumn.of("total", messageResolver.resolve("report.export.disease.total"), Form282ReportNodeResponse::total),
                ExcelColumn.of("under18", messageResolver.resolve("report.export.disease.upTo17"), Form282ReportNodeResponse::under18),
                ExcelColumn.of("underOneMonth", messageResolver.resolve("report.export.disease.upTo1Month"), Form282ReportNodeResponse::underOneMonth),
                ExcelColumn.of("oneMonthToUnderOneYear", messageResolver.resolve("report.export.disease.oneMonthToOneYear"), Form282ReportNodeResponse::oneMonthToUnderOneYear)
        );
    }

    @Override
    public String sheetName() {
        return "Shakl 28-2";
    }

    @Override
    public String fileNamePrefix() {
        return "form282_export";
    }
}
