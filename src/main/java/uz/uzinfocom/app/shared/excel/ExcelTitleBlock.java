package uz.uzinfocom.app.shared.excel;

/**
 * A merged, word-wrapped text region rendered above the column headers on the first sheet -
 * e.g. an institution letterhead, an approving-order reference, or a report title.
 * Rows/columns are 0-based and inclusive, matching POI's own indexing.
 * <p>
 * {@link Kind} picks the look; the 5-argument constructor keeps the original bold-centered
 * look ({@link Kind#CENTER}).
 */
public record ExcelTitleBlock(String text, int firstRow, int lastRow, int firstCol, int lastCol, Kind kind) {

    public enum Kind {
        /** Bold, centered (institution letterhead on the left). */
        CENTER,
        /** Regular weight, smaller, right-aligned (approving-order reference in the top-right corner). */
        RIGHT_NOTE,
        /** Italic, centered (period / filter line). */
        SUBTITLE,
        /** Large bold centered report title with a thin underline. */
        TITLE
    }

    public ExcelTitleBlock(String text, int firstRow, int lastRow, int firstCol, int lastCol) {
        this(text, firstRow, lastRow, firstCol, lastCol, Kind.CENTER);
    }
}
