package uz.uzinfocom.app.platform.export.application;

import uz.uzinfocom.app.shared.excel.ExcelColumn;
import uz.uzinfocom.app.shared.excel.ExcelStyleSettings;
import uz.uzinfocom.app.shared.excel.ExcelTitleBlock;

import java.util.List;
import java.util.function.Consumer;

/**
 * Plugin contract a feature module implements to get background Excel export "for free"
 * from {@code ExportJobService}: filtered row counting/streaming, job tracking, progress,
 * dynamic dev-panel-configurable columns/styles, file storage and cleanup.
 * <p>
 * Wiring a new module only means implementing this interface (typically backed by the
 * module's existing filter/specification/query-service) and adding one thin controller
 * method that calls {@code ExportJobService.submit(source, filter)} - the rest of the
 * pipeline (async execution, progress, "my files" listing, SSE, download, retention
 * cleanup) is shared and requires no further changes.
 *
 * @param <F> the module's own filter/request type (already validated by its controller)
 * @param <T> the row type streamed to the Excel writer
 */
public interface ExcelExportSource<F, T> {

    /**
     * Stable identifier for this export kind (e.g. {@code "FORM058"}) - stored on
     * {@code ExportJob.exportType} and used as the prefix for its dev-panel-configurable
     * {@code system_settings} keys (see {@code ExcelExportSettingsResolver}).
     */
    String exportType();

    /**
     * Row count matching {@code filter} under the caller's current access scope - computed
     * before the job is queued so requests exceeding {@code app.export.max-rows} are
     * rejected immediately instead of after minutes of background work.
     */
    long count(F filter);

    /**
     * Invokes {@code rowProcessor} once per matching row, entirely within a single
     * transaction/persistence-context spanning the whole call - implementations must
     * annotate their concrete method {@code @Transactional(readOnly = true)} and keep the
     * underlying query's {@code Stream} open for the full iteration (try-with-resources),
     * rather than collecting or returning it. This matters because rows are typically built
     * from lazily-loaded JPA associations (e.g. a patient's addresses/affiliations): handing
     * back a not-yet-drained {@code Stream} would let its transaction commit - and the
     * session close - before the caller ever touches those associations.
     */
    void forEachRow(F filter, Consumer<T> rowProcessor);

    /**
     * All columns this export could ever show, in the module's own default order. The
     * subset/order actually rendered for a given run is resolved by
     * {@code ExcelExportSettingsResolver} against the dev-panel-configurable column-key list,
     * falling back to this full list when nothing is configured.
     */
    List<ExcelColumn<T>> availableColumns();

    /**
     * Letterhead-style merged title region rendered above the column headers on the first
     * sheet (e.g. institution name, approving order reference, the filter's own date range)
     * - empty by default; override for modules that need the official-document look.
     */
    default List<ExcelTitleBlock> titleBlocks(F filter) {
        return List.of();
    }

    /**
     * Default column/style presentation, used only when nothing is configured for
     * {@link #exportType()} in {@code system_settings} - see
     * {@code ExcelExportSettingsResolver}.
     */
    default ExcelStyleSettings defaultStyle() {
        return ExcelStyleSettings.defaults();
    }

    String sheetName();

    String fileNamePrefix();
}
