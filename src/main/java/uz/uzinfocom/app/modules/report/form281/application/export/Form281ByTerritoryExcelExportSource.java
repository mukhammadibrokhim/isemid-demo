package uz.uzinfocom.app.modules.report.form281.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.ManualReportQueryService;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.modules.report.form281.application.query.Form281ByTerritoryReportQueryService;
import uz.uzinfocom.app.modules.report.form281.application.query.Form281ReportQueryService;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281ByTerritoryNodeResponse;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281DiseaseCellResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Form 28.1 by territory" (geography-first rows, one
 * column group per {@code FORM_28_1}-tagged disease — the reference form's
 * own 13 varaqa metrics, single period, no delta). Data-driven column set —
 * see {@code Form12ByTerritoryExcelExportSource}'s Javadoc.
 */
@Component
@RequiredArgsConstructor
public class Form281ByTerritoryExcelExportSource
        implements ExcelExportSource<Form281ByTerritoryExportFilter, Form281ByTerritoryNodeResponse> {

    private final Form281ByTerritoryReportQueryService form281ByTerritoryReportQueryService;
    private final ManualReportQueryService manualReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM281_BY_TERRITORY";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form281ByTerritoryExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form281ByTerritoryExportFilter filter, Consumer<Form281ByTerritoryNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<Form281ByTerritoryNodeResponse> flattenAll(Form281ByTerritoryExportFilter filter) {
        List<Form281ByTerritoryNodeResponse> root = form281ByTerritoryReportQueryService.getRoot(filter.from(), filter.to());
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form281ByTerritoryReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to()
                ),
                Form281ByTerritoryNodeResponse::code,
                Form281ByTerritoryNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<Form281ByTerritoryNodeResponse>> availableColumns() {
        List<ExcelColumn<Form281ByTerritoryNodeResponse>> columns = new ArrayList<>();
        columns.add(ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form281ByTerritoryNodeResponse::code));
        columns.add(ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form281ByTerritoryNodeResponse::name));

        String total = messageResolver.resolve("report.export.disease.total");
        String female = messageResolver.resolve("report.export.disease.female");
        String upTo17 = messageResolver.resolve("report.export.disease.upTo17");
        String upTo14 = messageResolver.resolve("report.export.disease.upTo14");
        String upTo1 = messageResolver.resolve("report.export.disease.upTo1");
        String age1to2 = messageResolver.resolve("report.export.disease.age1to2");
        String age3to5 = messageResolver.resolve("report.export.disease.age3to5");
        String ruralTotal = messageResolver.resolve("report.export.disease.ruralTotal");
        String ruralUpTo17 = messageResolver.resolve("report.export.disease.ruralUpTo17");
        String ruralUpTo14 = messageResolver.resolve("report.export.disease.ruralUpTo14");
        String ruralUpTo1 = messageResolver.resolve("report.export.disease.ruralUpTo1");
        String ruralAge1to2 = messageResolver.resolve("report.export.disease.ruralAge1to2");
        String ruralAge3to5 = messageResolver.resolve("report.export.disease.ruralAge3to5");

        List<ManualReportResponse> entries = orderedEntries();
        for (int i = 0; i < entries.size(); i++) {
            int index = i;
            ManualReportResponse entry = entries.get(i);
            String label = entry.code() != null ? entry.code() : String.valueOf(entry.id());

            columns.add(ExcelColumn.of("disease" + index + "Total", label + " — " + total, row -> cell(row, index).total()));
            columns.add(ExcelColumn.of("disease" + index + "Female", label + " — " + female, row -> cell(row, index).female()));
            columns.add(ExcelColumn.of("disease" + index + "Under18", label + " — " + upTo17, row -> cell(row, index).under18()));
            columns.add(ExcelColumn.of("disease" + index + "Under15", label + " — " + upTo14, row -> cell(row, index).under15()));
            columns.add(ExcelColumn.of("disease" + index + "Under1", label + " — " + upTo1, row -> cell(row, index).under1()));
            columns.add(ExcelColumn.of("disease" + index + "Age1to2", label + " — " + age1to2, row -> cell(row, index).age1to2()));
            columns.add(ExcelColumn.of("disease" + index + "Age3to5", label + " — " + age3to5, row -> cell(row, index).age3to5()));
            columns.add(ExcelColumn.of("disease" + index + "RuralTotal", label + " — " + ruralTotal, row -> cell(row, index).ruralTotal()));
            columns.add(ExcelColumn.of("disease" + index + "RuralUnder18", label + " — " + ruralUpTo17, row -> cell(row, index).ruralUnder18()));
            columns.add(ExcelColumn.of("disease" + index + "RuralUnder15", label + " — " + ruralUpTo14, row -> cell(row, index).ruralUnder15()));
            columns.add(ExcelColumn.of("disease" + index + "RuralUnder1", label + " — " + ruralUpTo1, row -> cell(row, index).ruralUnder1()));
            columns.add(ExcelColumn.of("disease" + index + "RuralAge1to2", label + " — " + ruralAge1to2, row -> cell(row, index).ruralAge1to2()));
            columns.add(ExcelColumn.of("disease" + index + "RuralAge3to5", label + " — " + ruralAge3to5, row -> cell(row, index).ruralAge3to5()));
        }

        return columns;
    }

    private Form281DiseaseCellResponse cell(Form281ByTerritoryNodeResponse row, int index) {
        return row.diseases().get(index);
    }

    /** Same {@code FORM_28_1} catalog entries, in the same code-sorted order, as {@link Form281ByTerritoryReportQueryService}. */
    private List<ManualReportResponse> orderedEntries() {
        return manualReportQueryService.findByReportType(Form281ReportQueryService.MANUAL_REPORT_TYPE).stream()
                .sorted((a, b) -> nullSafe(a.code()).compareToIgnoreCase(nullSafe(b.code())))
                .toList();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public String sheetName() {
        return "Shakl 28-1 (hududlar)";
    }

    @Override
    public String fileNamePrefix() {
        return "form281_by_territory_export";
    }
}
