package uz.uzinfocom.app.modules.report.form11.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form11.application.query.Form11ReportQueryService;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11ReportNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;
import uz.uzinfocom.app.shared.excel.ExcelTitleBlock;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Excel export for "Form 11" (geography-first, year-over-year morbidity
 * indicators + urban/rural/under-18 cuts) — mirrors {@code
 * Form1ExcelExportSource}'s flatten step, over {@link
 * Form11ReportNodeResponse}'s 16 fields, with a two-row grouped header
 * ({@code Form6ExcelExportSource}/{@code Form8ExcelExportSource}-style) so
 * the sheet reproduces the frontend table's "Абсолют кўрсаткич" / "Интенсив
 * кўрсаткич" / "Шаҳар аҳолиси" / "Қишлоқ аҳолиси" / "18 ёшгача болалар"
 * column groups instead of one flat header row.
 */
@Component
@RequiredArgsConstructor
public class Form11ExcelExportSource implements ExcelExportSource<Form11ExportFilter, Form11ReportNodeResponse> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Form11ReportQueryService form11ReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM11";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form11ExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form11ExportFilter filter, Consumer<Form11ReportNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<Form11ReportNodeResponse> flattenAll(Form11ExportFilter filter) {
        List<Form11ReportNodeResponse> root = form11ReportQueryService.getRoot(
                filter.from(), filter.to(), filter.diagnosisCode(), filter.koef(), filter.population()
        );
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form11ReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to(),
                        filter.diagnosisCode(), filter.koef(), filter.population()
                ),
                Form11ReportNodeResponse::code,
                Form11ReportNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<Form11ReportNodeResponse>> availableColumns() {
        String previousYear = messageResolver.resolve("report.export.block.previousYear");
        String currentYear = messageResolver.resolve("report.export.block.currentYear");
        String growthPercent = messageResolver.resolve("report.export.block.growthPercent");
        String sharePercent = messageResolver.resolve("report.export.block.sharePercent");
        String metricAbsolute = messageResolver.resolve("report.export.block.metricAbsolute");
        String metricIntensive = messageResolver.resolve("report.export.block.metricIntensive");
        String groupUrban = messageResolver.resolve("report.form11.export.group.urban");
        String groupRural = messageResolver.resolve("report.form11.export.group.rural");
        String groupChild = messageResolver.resolve("report.form11.export.group.child");

        return List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form11ReportNodeResponse::code),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form11ReportNodeResponse::name),
                col("absPreviousYear", metricAbsolute, previousYear, Form11ReportNodeResponse::absPreviousYear),
                col("absCurrentYear", metricAbsolute, currentYear, Form11ReportNodeResponse::absCurrentYear),
                col("absGrowthPercent", metricAbsolute, growthPercent, Form11ReportNodeResponse::absGrowthPercent),
                col("intensivePreviousYear", metricIntensive, previousYear, Form11ReportNodeResponse::intensivePreviousYear),
                col("intensiveCurrentYear", metricIntensive, currentYear, Form11ReportNodeResponse::intensiveCurrentYear),
                col("intensiveGrowthPercent", metricIntensive, growthPercent, Form11ReportNodeResponse::intensiveGrowthPercent),
                col("cityAbs", groupUrban, metricAbsolute, Form11ReportNodeResponse::cityAbs),
                col("cityIntensive", groupUrban, metricIntensive, Form11ReportNodeResponse::cityIntensive),
                col("citySharePercent", groupUrban, sharePercent, Form11ReportNodeResponse::citySharePercent),
                col("ruralAbs", groupRural, metricAbsolute, Form11ReportNodeResponse::ruralAbs),
                col("ruralIntensive", groupRural, metricIntensive, Form11ReportNodeResponse::ruralIntensive),
                col("ruralSharePercent", groupRural, sharePercent, Form11ReportNodeResponse::ruralSharePercent),
                col("childAbs", groupChild, metricAbsolute, Form11ReportNodeResponse::childAbs),
                col("childIntensive", groupChild, metricIntensive, Form11ReportNodeResponse::childIntensive)
        );
    }

    private ExcelColumn<Form11ReportNodeResponse> col(
            String key, String groupHeader, String header, Function<Form11ReportNodeResponse, Object> extractor
    ) {
        return ExcelColumn.of(key, groupHeader, header, extractor);
    }

    @Override
    public List<ExcelTitleBlock> titleBlocks(Form11ExportFilter filter) {
        int lastCol = availableColumns().size() - 1;
        return List.of(
                new ExcelTitleBlock(messageResolver.resolve("report.form11.export.title"), 0, 0, 0, lastCol),
                new ExcelTitleBlock(periodText(filter), 1, 1, 0, lastCol)
        );
    }

    private String periodText(Form11ExportFilter filter) {
        String from = filter.from() == null ? "—" : DATE_FORMAT.format(filter.from());
        String to = filter.to() == null ? "—" : DATE_FORMAT.format(filter.to());
        return messageResolver.resolve("report.form11.export.period", from, to);
    }

    @Override
    public String sheetName() {
        return "Shakl 11";
    }

    @Override
    public String fileNamePrefix() {
        return "form11_export";
    }
}
