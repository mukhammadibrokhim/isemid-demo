package uz.uzinfocom.app.modules.report.form12.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form12.application.query.Form12ReportQueryService;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12ReportNodeResponse;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Form 12" (disease-first: rows = {@code FORM_12}-tagged
 * manual-report catalog entries, plus a trailing "Jami" row). Unlike a
 * flat-table export, the whole result is already a small, bounded list
 * produced by one {@link Form12ReportQueryService#getRoot} call — no JPA
 * stream/specification involved, so {@code count()} and {@code forEachRow()}
 * simply call it (twice, independently — see {@code
 * ReportHierarchyExportFlattener}'s Javadoc for why this trades one extra
 * cheap call for avoiding shared mutable state on this singleton bean).
 */
@Component
@RequiredArgsConstructor
public class Form12ExcelExportSource implements ExcelExportSource<Form12ExportFilter, Form12ReportNodeResponse> {

    private final Form12ReportQueryService form12ReportQueryService;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM12";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form12ExportFilter filter) {
        return form12ReportQueryService.getRoot(filter.from(), filter.to()).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form12ExportFilter filter, Consumer<Form12ReportNodeResponse> rowProcessor) {
        form12ReportQueryService.getRoot(filter.from(), filter.to()).forEach(rowProcessor);
    }

    @Override
    public List<ExcelColumn<Form12ReportNodeResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("rowCode", messageResolver.resolve("report.formNoso.export.rowCode"), Form12ReportNodeResponse::rowCode),
                ExcelColumn.of("name", messageResolver.resolve("report.formNoso.export.name"), Form12ReportNodeResponse::name),
                ExcelColumn.of("icd10Display", messageResolver.resolve("report.formNoso.export.icd10Display"), Form12ReportNodeResponse::icd10Display),
                ExcelColumn.of("totalPreviousYear", messageResolver.resolve("report.formNoso.export.totalPreviousYear"), Form12ReportNodeResponse::totalPreviousYear),
                ExcelColumn.of("totalCurrentYear", messageResolver.resolve("report.formNoso.export.totalCurrentYear"), Form12ReportNodeResponse::totalCurrentYear),
                ExcelColumn.of("totalDelta", messageResolver.resolve("report.formNoso.export.totalDelta"), Form12ReportNodeResponse::totalDelta),
                ExcelColumn.of("under14PreviousYear", messageResolver.resolve("report.formNoso.export.under14PreviousYear"), Form12ReportNodeResponse::under14PreviousYear),
                ExcelColumn.of("under14CurrentYear", messageResolver.resolve("report.formNoso.export.under14CurrentYear"), Form12ReportNodeResponse::under14CurrentYear),
                ExcelColumn.of("under14Delta", messageResolver.resolve("report.formNoso.export.under14Delta"), Form12ReportNodeResponse::under14Delta),
                ExcelColumn.of("under18PreviousYear", messageResolver.resolve("report.formNoso.export.under18PreviousYear"), Form12ReportNodeResponse::under18PreviousYear),
                ExcelColumn.of("under18CurrentYear", messageResolver.resolve("report.formNoso.export.under18CurrentYear"), Form12ReportNodeResponse::under18CurrentYear),
                ExcelColumn.of("under18Delta", messageResolver.resolve("report.formNoso.export.under18Delta"), Form12ReportNodeResponse::under18Delta)
        );
    }

    @Override
    public String sheetName() {
        return "Shakl 12";
    }

    @Override
    public String fileNamePrefix() {
        return "form12_export";
    }
}
