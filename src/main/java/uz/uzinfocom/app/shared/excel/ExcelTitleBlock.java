package uz.uzinfocom.app.shared.excel;

/**
 * A merged, bold, centered, word-wrapped text region rendered above the column headers on
 * the first sheet - e.g. an institution letterhead, an approving-order reference, or a
 * report title. Rows/columns are 0-based and inclusive, matching POI's own indexing.
 */
public record ExcelTitleBlock(String text, int firstRow, int lastRow, int firstCol, int lastCol) {
}
