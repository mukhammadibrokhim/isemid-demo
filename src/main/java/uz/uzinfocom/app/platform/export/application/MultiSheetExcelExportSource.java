package uz.uzinfocom.app.platform.export.application;

import java.util.List;

/**
 * Plugin contract for a background Excel export whose file holds more than
 * one sheet - e.g. two of a report's views (disease-first, territory-first)
 * merged into a single workbook so a caller downloads one file instead of
 * two. Same idea as {@link ExcelExportSource} (a module implements this,
 * {@code ExportJobService.submitMultiSheet} runs it as a background job with
 * the same job tracking / progress / "my files" / SSE / retention pipeline),
 * generalized from exactly one sheet to a caller-supplied list of {@link
 * ExcelExportSheet}s, typically each one built from an existing {@link
 * ExcelExportSource} via {@link ExcelExportSourceSheet} rather than
 * duplicating that source's count/forEachRow/columns logic.
 *
 * @param <F> the combined export's own filter type, shared by every sheet
 */
public interface MultiSheetExcelExportSource<F> {

    /** Stable identifier for this export kind, stored on {@code ExportJob.exportType}. */
    String exportType();

    String fileNamePrefix();

    /**
     * The sheets to write, in the order they should appear in the workbook. Called once per
     * export, on the submitting request's own thread (before the background job is queued) -
     * safe to resolve each sheet's dev-panel-configurable columns/style here, since (unlike
     * the background worker thread) the caller's locale is already correctly set.
     */
    List<ExcelExportSheet<F>> sheets(F filter);
}
