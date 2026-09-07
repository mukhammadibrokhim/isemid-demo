package uz.uzinfocom.app.modules.report.form281.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form281.application.query.Form281ReportQueryService;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281ReportNodeResponse;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Form 28.1" (disease-first, single period, varaqa
 * age/gender/rural columns) — structurally identical to {@code
 * Form12ExcelExportSource}, just over {@link Form281ReportNodeResponse}'s
 * wider column set.
 */
@Component
@RequiredArgsConstructor
public class Form281ExcelExportSource implements ExcelExportSource<Form281ExportFilter, Form281ReportNodeResponse> {

    private final Form281ReportQueryService form281ReportQueryService;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM281";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form281ExportFilter filter) {
        return form281ReportQueryService.getRoot(filter.from(), filter.to()).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form281ExportFilter filter, Consumer<Form281ReportNodeResponse> rowProcessor) {
        form281ReportQueryService.getRoot(filter.from(), filter.to()).forEach(rowProcessor);
    }

    @Override
    public List<ExcelColumn<Form281ReportNodeResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("rowCode", messageResolver.resolve("report.formNoso.export.rowCode"), Form281ReportNodeResponse::rowCode),
                ExcelColumn.of("name", messageResolver.resolve("report.formNoso.export.name"), Form281ReportNodeResponse::name),
                ExcelColumn.of("icd10Display", messageResolver.resolve("report.formNoso.export.icd10Display"), Form281ReportNodeResponse::icd10Display),
                ExcelColumn.of("total", messageResolver.resolve("report.export.disease.total"), Form281ReportNodeResponse::total),
                ExcelColumn.of("female", messageResolver.resolve("report.export.disease.female"), Form281ReportNodeResponse::female),
                ExcelColumn.of("under18", messageResolver.resolve("report.export.disease.upTo17"), Form281ReportNodeResponse::under18),
                ExcelColumn.of("under15", messageResolver.resolve("report.export.disease.upTo14"), Form281ReportNodeResponse::under15),
                ExcelColumn.of("under1", messageResolver.resolve("report.export.disease.upTo1"), Form281ReportNodeResponse::under1),
                ExcelColumn.of("age1to2", messageResolver.resolve("report.export.disease.age1to2"), Form281ReportNodeResponse::age1to2),
                ExcelColumn.of("age3to5", messageResolver.resolve("report.export.disease.age3to5"), Form281ReportNodeResponse::age3to5),
                ExcelColumn.of("ruralTotal", messageResolver.resolve("report.export.disease.ruralTotal"), Form281ReportNodeResponse::ruralTotal),
                ExcelColumn.of("ruralUnder18", messageResolver.resolve("report.export.disease.ruralUpTo17"), Form281ReportNodeResponse::ruralUnder18),
                ExcelColumn.of("ruralUnder15", messageResolver.resolve("report.export.disease.ruralUpTo14"), Form281ReportNodeResponse::ruralUnder15),
                ExcelColumn.of("ruralUnder1", messageResolver.resolve("report.export.disease.ruralUpTo1"), Form281ReportNodeResponse::ruralUnder1),
                ExcelColumn.of("ruralAge1to2", messageResolver.resolve("report.export.disease.ruralAge1to2"), Form281ReportNodeResponse::ruralAge1to2),
                ExcelColumn.of("ruralAge3to5", messageResolver.resolve("report.export.disease.ruralAge3to5"), Form281ReportNodeResponse::ruralAge3to5)
        );
    }

    @Override
    public String sheetName() {
        return "Shakl 28-1";
    }

    @Override
    public String fileNamePrefix() {
        return "form281_export";
    }
}
