package uz.uzinfocom.app.shared.excel;

import java.util.function.Function;

/**
 * @param key stable machine identifier (e.g. {@code "patientFio"}), used by dev-panel
 *            column-selection settings - independent of {@code header} so relabeling or
 *            translating a column never breaks an existing configuration
 * @param header display text rendered in the column header cell
 */
public record ExcelColumn<T>(String key, String header, Function<T, Object> valueExtractor) {

    public static <T> ExcelColumn<T> of(String key, String header, Function<T, Object> valueExtractor) {
        return new ExcelColumn<>(key, header, valueExtractor);
    }
}
