package uz.uzinfocom.app.platform.export.application;

import uz.uzinfocom.app.shared.excel.ExcelWorkbookWriter;

import java.io.IOException;

/**
 * One sheet of a {@link MultiSheetExcelExportSource}'s workbook. Deliberately
 * keeps its row type private to its own implementation (never appears in this
 * interface) so a {@link MultiSheetExcelExportSource} can hold sheets of
 * different row shapes - e.g. a combined export's disease-first and
 * territory-first views - in one plain {@code List<ExcelExportSheet<F>>},
 * the same way {@link ExcelExportSourceSheet} adapts an existing single-sheet
 * {@link ExcelExportSource}.
 *
 * @param <F> the combined export's own filter type
 */
public interface ExcelExportSheet<F> {

    /** Row count matching {@code filter} - summed across every sheet for the up-front max-rows check. */
    long count(F filter);

    /**
     * Starts this sheet on {@code workbook} (via {@link ExcelWorkbookWriter#newSheet}) and
     * writes every matching row into it, calling {@code onRowWritten} once per row for
     * progress tracking - same contract as {@link ExcelExportSource#forEachRow} otherwise
     * (single transaction/persistence-context spanning the whole call).
     */
    void writeInto(F filter, ExcelWorkbookWriter workbook, Runnable onRowWritten) throws IOException;
}
