package uz.uzinfocom.app.shared.excel;

import java.util.List;
import java.util.function.Function;

/**
 * @param key          stable machine identifier (e.g. {@code "patientFio"}), used by dev-panel
 *                     column-selection settings - independent of {@code header} so relabeling or
 *                     translating a column never breaks an existing configuration
 * @param groupHeaders top-level labels shared by a run of adjacent columns, outermost first (e.g.
 *                     {@code ["Joriy davr", "Umumiy aholi"]} renders "Joriy davr" spanning a wider
 *                     run on the first header row with "Umumiy aholi" spanning the narrower run
 *                     directly beneath it, above each column's own {@code header}) - a column
 *                     with fewer levels than the deepest column in the sheet has its own {@code
 *                     header} vertically merged down through the remaining rows instead, and an
 *                     empty list means {@code header} spans every header row. Consecutive columns
 *                     merge into the same run at a given level only when their {@code
 *                     groupHeaders} agree on every level up to and including it - reordering or
 *                     interleaving with a different group breaks the run.
 * @param header       display text rendered in the column's own header cell
 */
public record ExcelColumn<T>(String key, List<String> groupHeaders, String header, Function<T, Object> valueExtractor) {

    public ExcelColumn {
        groupHeaders = groupHeaders == null ? List.of() : List.copyOf(groupHeaders);
    }

    public static <T> ExcelColumn<T> of(String key, String header, Function<T, Object> valueExtractor) {
        return new ExcelColumn<>(key, List.of(), header, valueExtractor);
    }

    public static <T> ExcelColumn<T> of(String key, String groupHeader, String header, Function<T, Object> valueExtractor) {
        return new ExcelColumn<>(key, groupHeader == null ? List.of() : List.of(groupHeader), header, valueExtractor);
    }

    public static <T> ExcelColumn<T> of(String key, List<String> groupHeaders, String header, Function<T, Object> valueExtractor) {
        return new ExcelColumn<>(key, groupHeaders, header, valueExtractor);
    }
}
