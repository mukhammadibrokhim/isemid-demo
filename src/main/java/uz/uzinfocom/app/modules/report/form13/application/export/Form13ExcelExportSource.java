package uz.uzinfocom.app.modules.report.form13.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.ManualReportQueryService;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.modules.report.form13.application.query.Form13ReportQueryService;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13DiseaseCellResponse;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13ReportNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Form 13" (geography-first rows, one column group per
 * {@code FORM_13}-tagged disease: prev/current × total/under14/under18, no
 * delta). Data-driven column set — see {@code
 * Form12ByTerritoryExcelExportSource}'s Javadoc for why {@link
 * #availableColumns()} re-derives the same ordered catalog entries the query
 * service itself uses, and looks up each row's matching cell by index.
 */
@Component
@RequiredArgsConstructor
public class Form13ExcelExportSource implements ExcelExportSource<Form13ExportFilter, Form13ReportNodeResponse> {

    private final Form13ReportQueryService form13ReportQueryService;
    private final ManualReportQueryService manualReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM13";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form13ExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form13ExportFilter filter, Consumer<Form13ReportNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<Form13ReportNodeResponse> flattenAll(Form13ExportFilter filter) {
        List<Form13ReportNodeResponse> root = form13ReportQueryService.getRoot(filter.from(), filter.to());
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form13ReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to()
                ),
                Form13ReportNodeResponse::code,
                Form13ReportNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<Form13ReportNodeResponse>> availableColumns() {
        List<ExcelColumn<Form13ReportNodeResponse>> columns = new ArrayList<>();
        columns.add(ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form13ReportNodeResponse::code));
        columns.add(ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form13ReportNodeResponse::name));

        String totalPrev = messageResolver.resolve("report.export.disease.totalPrevYear");
        String totalCurr = messageResolver.resolve("report.export.disease.totalCurrYear");
        String under14Prev = messageResolver.resolve("report.export.disease.upTo14PrevYear");
        String under14Curr = messageResolver.resolve("report.export.disease.upTo14CurrYear");
        String under18Prev = messageResolver.resolve("report.export.disease.upTo18PrevYear");
        String under18Curr = messageResolver.resolve("report.export.disease.upTo18CurrYear");

        List<ManualReportResponse> entries = orderedEntries();
        for (int i = 0; i < entries.size(); i++) {
            int index = i;
            ManualReportResponse entry = entries.get(i);
            String label = entry.code() != null ? entry.code() : String.valueOf(entry.id());

            columns.add(ExcelColumn.of("disease" + index + "TotalPrev", label + " — " + totalPrev, row -> cell(row, index).totalPreviousYear()));
            columns.add(ExcelColumn.of("disease" + index + "TotalCurr", label + " — " + totalCurr, row -> cell(row, index).totalCurrentYear()));
            columns.add(ExcelColumn.of("disease" + index + "Under14Prev", label + " — " + under14Prev, row -> cell(row, index).under14PreviousYear()));
            columns.add(ExcelColumn.of("disease" + index + "Under14Curr", label + " — " + under14Curr, row -> cell(row, index).under14CurrentYear()));
            columns.add(ExcelColumn.of("disease" + index + "Under18Prev", label + " — " + under18Prev, row -> cell(row, index).under18PreviousYear()));
            columns.add(ExcelColumn.of("disease" + index + "Under18Curr", label + " — " + under18Curr, row -> cell(row, index).under18CurrentYear()));
        }

        return columns;
    }

    private Form13DiseaseCellResponse cell(Form13ReportNodeResponse row, int index) {
        return row.diseases().get(index);
    }

    /** Same {@code FORM_13} catalog entries, in the same code-sorted order, as {@link Form13ReportQueryService}. */
    private List<ManualReportResponse> orderedEntries() {
        return manualReportQueryService.findByReportType(Form13ReportQueryService.MANUAL_REPORT_TYPE).stream()
                .sorted((a, b) -> nullSafe(a.code()).compareToIgnoreCase(nullSafe(b.code())))
                .toList();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public String sheetName() {
        return "Shakl 13";
    }

    @Override
    public String fileNamePrefix() {
        return "form13_export";
    }
}
