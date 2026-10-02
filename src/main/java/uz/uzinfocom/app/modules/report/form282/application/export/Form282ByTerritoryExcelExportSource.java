package uz.uzinfocom.app.modules.report.form282.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.ManualReportQueryService;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.modules.report.form282.application.query.Form282ByTerritoryReportQueryService;
import uz.uzinfocom.app.modules.report.form282.application.query.Form282ReportQueryService;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282ByTerritoryNodeResponse;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282DiseaseCellResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Form 28.2 by territory" (geography-first rows, one
 * column group per {@code FORM_28_2}-tagged disease — total/under18/
 * underOneMonth/oneMonthToUnderOneYear, single period). Data-driven column
 * set — see {@code Form12ByTerritoryExcelExportSource}'s Javadoc.
 */
@Component
@RequiredArgsConstructor
public class Form282ByTerritoryExcelExportSource
        implements ExcelExportSource<Form282ByTerritoryExportFilter, Form282ByTerritoryNodeResponse> {

    private final Form282ByTerritoryReportQueryService form282ByTerritoryReportQueryService;
    private final ManualReportQueryService manualReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM282_BY_TERRITORY";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form282ByTerritoryExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form282ByTerritoryExportFilter filter, Consumer<Form282ByTerritoryNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<Form282ByTerritoryNodeResponse> flattenAll(Form282ByTerritoryExportFilter filter) {
        List<Form282ByTerritoryNodeResponse> root = form282ByTerritoryReportQueryService.getRoot(filter.from(), filter.to());
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form282ByTerritoryReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to()
                ),
                Form282ByTerritoryNodeResponse::code,
                Form282ByTerritoryNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<Form282ByTerritoryNodeResponse>> availableColumns() {
        List<ExcelColumn<Form282ByTerritoryNodeResponse>> columns = new ArrayList<>();
        columns.add(ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form282ByTerritoryNodeResponse::code));
        columns.add(ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form282ByTerritoryNodeResponse::name));

        String total = messageResolver.resolve("report.export.disease.total");
        String upTo17 = messageResolver.resolve("report.export.disease.upTo17");
        String upTo1Month = messageResolver.resolve("report.export.disease.upTo1Month");
        String oneMonthToOneYear = messageResolver.resolve("report.export.disease.oneMonthToOneYear");

        List<ManualReportResponse> entries = orderedEntries();
        for (int i = 0; i < entries.size(); i++) {
            int index = i;
            ManualReportResponse entry = entries.get(i);
            String label = entry.code() != null ? entry.code() : String.valueOf(entry.id());

            columns.add(ExcelColumn.of("disease" + index + "Total", label + " — " + total, row -> cell(row, index).total()));
            columns.add(ExcelColumn.of("disease" + index + "Under18", label + " — " + upTo17, row -> cell(row, index).under18()));
            columns.add(ExcelColumn.of("disease" + index + "UnderOneMonth", label + " — " + upTo1Month, row -> cell(row, index).underOneMonth()));
            columns.add(ExcelColumn.of("disease" + index + "OneMonthToUnderOneYear", label + " — " + oneMonthToOneYear, row -> cell(row, index).oneMonthToUnderOneYear()));
        }

        return columns;
    }

    private Form282DiseaseCellResponse cell(Form282ByTerritoryNodeResponse row, int index) {
        return row.diseases().get(index);
    }

    /** Same {@code FORM_28_2} catalog entries, in the same code-sorted order, as {@link Form282ByTerritoryReportQueryService}. */
    private List<ManualReportResponse> orderedEntries() {
        return manualReportQueryService.findByReportType(Form282ReportQueryService.MANUAL_REPORT_TYPE).stream()
                .sorted((a, b) -> nullSafe(a.code()).compareToIgnoreCase(nullSafe(b.code())))
                .toList();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public String sheetName() {
        return "Shakl 28-2 (hududlar)";
    }

    @Override
    public String fileNamePrefix() {
        return "form282_by_territory_export";
    }
}
