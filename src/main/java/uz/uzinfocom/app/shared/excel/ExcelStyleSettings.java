package uz.uzinfocom.app.shared.excel;

/**
 * Visual knobs for {@link ExcelExportWriter}, meant to be resolved from runtime-editable
 * settings (see {@code SystemSettingResolver}) rather than hardcoded per export - so the
 * look of a generated workbook can be tuned from the dev/admin panel without a deploy.
 */
public record ExcelStyleSettings(
        String headerFillColorHex,
        String fontName,
        int fontSize,
        boolean headerBold,
        boolean thinBorder,
        int rowsPerSheet
) {
    private static final String DEFAULT_HEADER_FILL = "C0C0C0";
    private static final String DEFAULT_FONT = "Calibri";
    private static final int DEFAULT_FONT_SIZE = 11;
    private static final int DEFAULT_ROWS_PER_SHEET = 100_000;

    public static ExcelStyleSettings defaults() {
        return new ExcelStyleSettings(DEFAULT_HEADER_FILL, DEFAULT_FONT, DEFAULT_FONT_SIZE, true, true, DEFAULT_ROWS_PER_SHEET);
    }
}
