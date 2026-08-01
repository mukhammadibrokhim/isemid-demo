package uz.uzinfocom.app.shared.excel;

import java.io.Closeable;
import java.io.IOException;

/**
 * A single open workbook, written to one row at a time and finalized on {@link #close()}.
 * <p>
 * Deliberately push-style (caller drives each {@link #write}, rather than the writer pulling
 * from a {@code Stream}) so a caller whose rows come from a lazily-loaded JPA association
 * (e.g. a patient's addresses/affiliations) can keep its own transaction open for the whole
 * call - see {@code ExcelExportSource.forEachRow}, which handing a {@code Stream} across a
 * transaction boundary would break once that transaction commits.
 */
public interface ExcelRowWriter<T> extends Closeable {

    void write(T row);
}
