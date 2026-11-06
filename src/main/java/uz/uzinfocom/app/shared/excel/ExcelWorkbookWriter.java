package uz.uzinfocom.app.shared.excel;

import java.io.Closeable;
import java.util.List;

/**
 * One open workbook that can hold several independent, differently-shaped
 * sheets — each obtained via {@link #newSheet}, written one row at a time
 * exactly like a plain {@link ExcelRowWriter} — finalized (flushed to the
 * underlying stream and closed) only once, by this type's own {@link #close()}.
 * <p>
 * Complements {@link ExcelExportWriter#open}, which covers the common
 * single-sheet case (the returned {@link ExcelRowWriter} owns the whole
 * workbook and finalizes it on its own {@code close()}): a caller writing
 * more than one sheet into one file — e.g. a combined export merging two
 * reports' views — gets one of these from {@link ExcelExportWriter#openWorkbook}
 * instead, calls {@link #newSheet} once per sheet (in order), and closes this
 * writer, not the individual per-sheet writers, when done.
 */
public interface ExcelWorkbookWriter extends Closeable {

    /**
     * Starts a new named sheet in this workbook and returns a row writer
     * scoped to it. The returned {@link ExcelRowWriter#close()} is a no-op —
     * this workbook's own {@link #close()} is what actually flushes and
     * finalizes the file, once, after every sheet has been written.
     */
    <T> ExcelRowWriter<T> newSheet(
            String sheetBaseName,
            List<ExcelTitleBlock> titleBlocks,
            List<ExcelColumn<T>> columns,
            ExcelStyleSettings style
    );
}
