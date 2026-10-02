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
import uz.uzinfocom.app.shared.excel.ExcelTitleBlock;

import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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
        String confirmedGroup = messageResolver.resolve("report.form1.export.group.confirmed");
        String primaryGroup = messageResolver.resolve("report.form1.export.group.primary");
        String total = messageResolver.resolve("report.export.block.total");
        String under14 = messageResolver.resolve("report.export.block.under14");
        String under18 = messageResolver.resolve("report.export.block.under18");
        String adult = messageResolver.resolve("report.export.block.adult");
        String female = messageResolver.resolve("report.export.block.female");

        return List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form1ReportNodeResponse::code),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form1ReportNodeResponse::name),
                ExcelColumn.of("confirmedTotal", confirmedGroup, total, row -> row.confirmed().total()),
                ExcelColumn.of("confirmedUnder14", confirmedGroup, under14, row -> row.confirmed().under14()),
                ExcelColumn.of("confirmedUnder18", confirmedGroup, under18, row -> row.confirmed().under18()),
                ExcelColumn.of("confirmedAdult", confirmedGroup, adult, row -> row.confirmed().adult()),
                ExcelColumn.of("confirmedFemale", confirmedGroup, female, row -> row.confirmed().female()),
                ExcelColumn.of("primaryTotal", primaryGroup, total, row -> row.primary().total()),
                ExcelColumn.of("primaryUnder14", primaryGroup, under14, row -> row.primary().under14()),
                ExcelColumn.of("primaryUnder18", primaryGroup, under18, row -> row.primary().under18()),
                ExcelColumn.of("primaryAdult", primaryGroup, adult, row -> row.primary().adult()),
                ExcelColumn.of("primaryFemale", primaryGroup, female, row -> row.primary().female()),
                ExcelColumn.of("diagnosisChangePercent", messageResolver.resolve("report.form1.export.diagnosisChangePercent"), Form1ReportNodeResponse::diagnosisChangePercent)
        );
    }

    @Override
    public List<ExcelTitleBlock> titleBlocks(Form1ExportFilter filter) {
        int lastCol = availableColumns().size() - 1;
        return List.of(
                new ExcelTitleBlock(messageResolver.resolve("report.form1.export.title"), 0, 0, 0, lastCol),
                new ExcelTitleBlock(periodText(filter), 1, 1, 0, lastCol)
        );
    }

    private String periodText(Form1ExportFilter filter) {
        String from = filter.from() == null ? "—" : DATE_FORMAT.format(filter.from());
        String to = filter.to() == null ? "—" : DATE_FORMAT.format(filter.to());
        return messageResolver.resolve("report.form1.export.period", from, to);
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
