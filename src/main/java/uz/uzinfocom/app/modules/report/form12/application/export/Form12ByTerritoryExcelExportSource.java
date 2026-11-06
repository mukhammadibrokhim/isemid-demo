package uz.uzinfocom.app.modules.report.form12.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.ManualReportQueryService;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.modules.report.form12.application.query.Form12ByTerritoryReportQueryService;
import uz.uzinfocom.app.modules.report.form12.application.query.Form12ReportQueryService;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12ByTerritoryNodeResponse;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12DiseaseCellResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Form 12 by territory" (geography-first rows, one column
 * group — prev/current × total/under14/under18, no delta — per {@code
 * FORM_12}-tagged nosological form, same shape as "Form 13"). Unlike the
 * fixed columns of every other geography-first export, this report's column
 * set is data-driven: {@link #availableColumns()} queries the same {@code
 * FORM_12} catalog entries {@link Form12ByTerritoryReportQueryService}
 * itself uses, in the same stable sort order, so each generated column's
 * index-based lookup into a row's {@code diseases[]} lines up with what that
 * row actually contains.
 */
@Component
@RequiredArgsConstructor
public class Form12ByTerritoryExcelExportSource
        implements ExcelExportSource<Form12ByTerritoryExportFilter, Form12ByTerritoryNodeResponse> {

    private final Form12ByTerritoryReportQueryService form12ByTerritoryReportQueryService;
    private final ManualReportQueryService manualReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM12_BY_TERRITORY";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form12ByTerritoryExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form12ByTerritoryExportFilter filter, Consumer<Form12ByTerritoryNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<Form12ByTerritoryNodeResponse> flattenAll(Form12ByTerritoryExportFilter filter) {
        List<Form12ByTerritoryNodeResponse> root = form12ByTerritoryReportQueryService.getRoot(filter.from(), filter.to());
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form12ByTerritoryReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to()
                ),
                Form12ByTerritoryNodeResponse::code,
                Form12ByTerritoryNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<Form12ByTerritoryNodeResponse>> availableColumns() {
        List<ExcelColumn<Form12ByTerritoryNodeResponse>> columns = new ArrayList<>();
        columns.add(ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form12ByTerritoryNodeResponse::code));
        columns.add(ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form12ByTerritoryNodeResponse::name));

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

    private Form12DiseaseCellResponse cell(Form12ByTerritoryNodeResponse row, int index) {
        return row.diseases().get(index);
    }

    /** Same {@code FORM_12} catalog entries, in the same code-sorted order, as {@link Form12ByTerritoryReportQueryService}. */
    private List<ManualReportResponse> orderedEntries() {
        return manualReportQueryService.findByReportType(Form12ReportQueryService.MANUAL_REPORT_TYPE).stream()
                .sorted((a, b) -> nullSafe(a.code()).compareToIgnoreCase(nullSafe(b.code())))
                .toList();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public String sheetName() {
        return "Shakl 12 (hududlar)";
    }

    @Override
    public String fileNamePrefix() {
        return "form12_by_territory_export";
    }
}
