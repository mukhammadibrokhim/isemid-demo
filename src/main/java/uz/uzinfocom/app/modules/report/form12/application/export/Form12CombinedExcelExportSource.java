package uz.uzinfocom.app.modules.report.form12.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.export.application.ExcelExportSettingsResolver;
import uz.uzinfocom.app.platform.export.application.ExcelExportSheet;
import uz.uzinfocom.app.platform.export.application.ExcelExportSourceSheet;
import uz.uzinfocom.app.platform.export.application.MultiSheetExcelExportSource;

import java.util.List;

/**
 * Combined Excel export for "Form 12" — one workbook with both of the
 * report's views, matching the frontend's two tabs ("Kasalliklar bo'yicha" /
 * "Hududlar bo'yicha") in one file instead of the two separate single-sheet
 * downloads {@link Form12ExcelExportSource} and {@link
 * Form12ByTerritoryExcelExportSource} already provide on their own (kept
 * as-is for anyone still using them directly).
 * <p>
 * Both sheets are the two existing sources unchanged, wrapped via {@link
 * ExcelExportSourceSheet} — same rows, same dev-panel-configurable columns/
 * style (resolved once, against each wrapped source's own {@code
 * FORM12}/{@code FORM12_BY_TERRITORY} settings), same disease set (the
 * {@code FORM_12}-tagged {@code modules.reference} manual-report catalog
 * entries each source already queries dynamically — nothing hardcoded here).
 * {@link Form12ExportFilter} doubles as the combined filter, since {@link
 * Form12ByTerritoryExportFilter} is structurally identical ({@code from}/
 * {@code to}) — the second sheet is built from a freshly copied one.
 */
@Component
@RequiredArgsConstructor
public class Form12CombinedExcelExportSource implements MultiSheetExcelExportSource<Form12ExportFilter> {

    private final Form12ExcelExportSource form12ExcelExportSource;
    private final Form12ByTerritoryExcelExportSource form12ByTerritoryExcelExportSource;
    private final ExcelExportSettingsResolver excelExportSettingsResolver;

    @Override
    public String exportType() {
        return "FORM12_COMBINED";
    }

    @Override
    public String fileNamePrefix() {
        return "form12_combined_export";
    }

    @Override
    public List<ExcelExportSheet<Form12ExportFilter>> sheets(Form12ExportFilter filter) {
        return List.of(
                new ExcelExportSourceSheet<>(
                        form12ExcelExportSource,
                        combined -> combined,
                        excelExportSettingsResolver.resolveColumns(form12ExcelExportSource),
                        excelExportSettingsResolver.resolveStyle(form12ExcelExportSource)
                ),
                new ExcelExportSourceSheet<>(
                        form12ByTerritoryExcelExportSource,
                        combined -> new Form12ByTerritoryExportFilter(combined.from(), combined.to()),
                        excelExportSettingsResolver.resolveColumns(form12ByTerritoryExcelExportSource),
                        excelExportSettingsResolver.resolveStyle(form12ByTerritoryExcelExportSource)
                )
        );
    }
}
