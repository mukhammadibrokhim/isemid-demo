package uz.uzinfocom.app.modules.report.form1.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form1.application.query.Form1ReportQueryService;
import uz.uzinfocom.app.modules.report.form1.application.query.dto.Form1ReportNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Form 1" (geography-first: republic→region→district→
 * organization). Unlike a flat-table export, the report's own {@code
 * getRoot}/{@code getChildren} only ever return one hierarchy level per call
 * — {@link ReportHierarchyExportFlattener} walks the whole accessible tree
 * down to organization level and flattens it into one row list, which {@code
 * count()}/{@code forEachRow()} then size/stream (two independent walks, see
 * the flattener's Javadoc for why).
 */
@Component
@RequiredArgsConstructor
public class Form1ExcelExportSource implements ExcelExportSource<Form1ExportFilter, Form1ReportNodeResponse> {

    private final Form1ReportQueryService form1ReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM1";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form1ExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form1ExportFilter filter, Consumer<Form1ReportNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<Form1ReportNodeResponse> flattenAll(Form1ExportFilter filter) {
        List<Form1ReportNodeResponse> root = form1ReportQueryService.getRoot(
                filter.from(), filter.to(), filter.diagnosisCode()
        );
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form1ReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to(), filter.diagnosisCode()
                ),
                Form1ReportNodeResponse::code,
                Form1ReportNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<Form1ReportNodeResponse>> availableColumns() {
        String confirmed = messageResolver.resolve("report.export.block.confirmed");
        String primary = messageResolver.resolve("report.export.block.primary");
        return List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form1ReportNodeResponse::code),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form1ReportNodeResponse::name),
                ExcelColumn.of("confirmedTotal", block(confirmed, "report.export.block.total"), row -> row.confirmed().total()),
                ExcelColumn.of("confirmedUnder14", block(confirmed, "report.export.block.under14"), row -> row.confirmed().under14()),
                ExcelColumn.of("confirmedUnder18", block(confirmed, "report.export.block.under18"), row -> row.confirmed().under18()),
                ExcelColumn.of("confirmedAdult", block(confirmed, "report.export.block.adult"), row -> row.confirmed().adult()),
                ExcelColumn.of("confirmedFemale", block(confirmed, "report.export.block.female"), row -> row.confirmed().female()),
                ExcelColumn.of("diagnosisChangePercent", messageResolver.resolve("report.form1.export.diagnosisChangePercent"), Form1ReportNodeResponse::diagnosisChangePercent),
                ExcelColumn.of("primaryTotal", block(primary, "report.export.block.total"), row -> row.primary().total()),
                ExcelColumn.of("primaryUnder14", block(primary, "report.export.block.under14"), row -> row.primary().under14()),
                ExcelColumn.of("primaryUnder18", block(primary, "report.export.block.under18"), row -> row.primary().under18()),
                ExcelColumn.of("primaryAdult", block(primary, "report.export.block.adult"), row -> row.primary().adult()),
                ExcelColumn.of("primaryFemale", block(primary, "report.export.block.female"), row -> row.primary().female())
        );
    }

    private String block(String prefix, String suffixKey) {
        return prefix + " — " + messageResolver.resolve(suffixKey);
    }

    @Override
    public String sheetName() {
        return "Shakl 1";
    }

    @Override
    public String fileNamePrefix() {
        return "form1_export";
    }
}
