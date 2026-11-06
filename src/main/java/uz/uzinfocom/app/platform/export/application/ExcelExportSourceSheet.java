package uz.uzinfocom.app.platform.export.application;

import uz.uzinfocom.app.shared.excel.ExcelColumn;
import uz.uzinfocom.app.shared.excel.ExcelRowWriter;
import uz.uzinfocom.app.shared.excel.ExcelStyleSettings;
import uz.uzinfocom.app.shared.excel.ExcelWorkbookWriter;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * Adapts an existing single-sheet {@link ExcelExportSource} into one {@link
 * ExcelExportSheet} of a {@link MultiSheetExcelExportSource} - lets a
 * combined export reuse a module's already-wired source (its count/forEachRow
 * logic, plus columns/style already resolved by the caller via {@code
 * ExcelExportSettingsResolver} against the source's own dev-panel settings)
 * instead of duplicating it.
 * <p>
 * {@code F != CF} in general (the wrapped source keeps its own filter record,
 * e.g. {@code Form12ByTerritoryExportFilter}, distinct from the combined
 * export's own {@code CF}) - {@code filterMapper} bridges the two, typically
 * just copying a couple of shared fields (e.g. {@code from}/{@code to}) into
 * the wrapped source's filter shape.
 *
 * @param <CF> the combined export's own filter type
 * @param <F>  the wrapped source's own filter type
 * @param <T>  the wrapped source's row type
 */
public final class ExcelExportSourceSheet<CF, F, T> implements ExcelExportSheet<CF> {

    private final ExcelExportSource<F, T> source;
    private final Function<CF, F> filterMapper;
    private final List<ExcelColumn<T>> columns;
    private final ExcelStyleSettings style;

    public ExcelExportSourceSheet(
            ExcelExportSource<F, T> source,
            Function<CF, F> filterMapper,
            List<ExcelColumn<T>> columns,
            ExcelStyleSettings style
    ) {
        this.source = source;
        this.filterMapper = filterMapper;
        this.columns = columns;
        this.style = style;
    }

    @Override
    public long count(CF combinedFilter) {
        return source.count(filterMapper.apply(combinedFilter));
    }

    @Override
    public void writeInto(CF combinedFilter, ExcelWorkbookWriter workbook, Runnable onRowWritten) throws IOException {
        F filter = filterMapper.apply(combinedFilter);

        try (ExcelRowWriter<T> writer = workbook.newSheet(source.sheetName(), source.titleBlocks(filter), columns, style)) {
            source.forEachRow(filter, row -> {
                writer.write(row);
                onRowWritten.run();
            });
        }
    }
}
