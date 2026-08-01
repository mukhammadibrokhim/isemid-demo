package uz.uzinfocom.app.shared.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Writes tabular data to an .xlsx file using POI's SXSSF streaming API, so exporting a
 * large filtered result set doesn't hold every row in memory at once - only a small
 * window of rows lives in memory, the rest is flushed to disk-backed temp files as they're
 * written and reassembled into the final workbook when the returned {@link ExcelRowWriter}
 * is closed.
 * <p>
 * Supports an optional letterhead-style {@link ExcelTitleBlock} region (rendered once, on
 * the first sheet only) and automatically rolls over to a new sheet - repeating the column
 * header row - every {@link ExcelStyleSettings#rowsPerSheet()} data rows, since a single
 * sheet holding hundreds of thousands of rows is technically valid but unwieldy to open.
 */
@Component
public class ExcelExportWriter {

    private static final int ROW_ACCESS_WINDOW_SIZE = 200;
    private static final int MAX_SHEET_NAME_LENGTH = 31;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * Opens a new workbook for writing. The caller must {@link ExcelRowWriter#write} every
     * row and then {@link ExcelRowWriter#close()} it (ideally via try-with-resources) to
     * flush the workbook to {@code out} - nothing is written to {@code out} before then.
     */
    public <T> ExcelRowWriter<T> open(
            OutputStream out,
            String sheetBaseName,
            List<ExcelTitleBlock> titleBlocks,
            List<ExcelColumn<T>> columns,
            ExcelStyleSettings style
    ) {
        return new SheetCursor<>(out, sheetBaseName, titleBlocks, columns, style);
    }

    /**
     * Mutable, single-use cursor tracking which sheet is currently being written to and
     * how many data rows have landed on it - rolls over to a new sheet transparently once
     * {@link ExcelStyleSettings#rowsPerSheet()} is reached.
     */
    private final class SheetCursor<T> implements ExcelRowWriter<T> {
        private final OutputStream out;
        private final SXSSFWorkbook workbook;
        private final String sheetBaseName;
        private final List<ExcelTitleBlock> titleBlocks;
        private final List<ExcelColumn<T>> columns;
        private final CellStyle titleStyle;
        private final CellStyle headerStyle;
        private final CellStyle dataStyle;
        private final int rowsPerSheet;

        private Sheet sheet;
        private int sheetIndex;
        private int nextRowIndex;
        private int rowsOnCurrentSheet;

        private SheetCursor(
                OutputStream out,
                String sheetBaseName,
                List<ExcelTitleBlock> titleBlocks,
                List<ExcelColumn<T>> columns,
                ExcelStyleSettings style
        ) {
            this.out = out;
            this.workbook = new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE);
            this.workbook.setCompressTempFiles(true);
            this.sheetBaseName = sheetBaseName;
            this.titleBlocks = titleBlocks;
            this.columns = columns;
            this.titleStyle = titleStyle(workbook, style);
            this.headerStyle = headerStyle(workbook, style);
            this.dataStyle = dataStyle(workbook, style);
            this.rowsPerSheet = Math.max(1, style.rowsPerSheet());

            startNewSheet();
        }

        @Override
        public void write(T item) {
            if (rowsOnCurrentSheet >= rowsPerSheet) {
                startNewSheet();
            }

            Row row = sheet.createRow(nextRowIndex++);
            for (int i = 0; i < columns.size(); i++) {
                writeCell(row.createCell(i), columns.get(i).valueExtractor().apply(item), dataStyle);
            }
            rowsOnCurrentSheet++;
        }

        @Override
        public void close() throws IOException {
            try {
                workbook.write(out);
            } finally {
                workbook.dispose();
            }
        }

        private void startNewSheet() {
            sheetIndex++;
            sheet = workbook.createSheet(sheetName());
            nextRowIndex = 0;
            rowsOnCurrentSheet = 0;

            if (sheetIndex == 1 && !titleBlocks.isEmpty()) {
                nextRowIndex = writeTitleBlocks(sheet, titleBlocks, titleStyle);
            }

            nextRowIndex = writeHeader(sheet, columns, headerStyle, nextRowIndex);
        }

        private String sheetName() {
            String base = sanitizeSheetName(sheetBaseName);
            String candidate = sheetIndex == 1 ? base : base + " " + sheetIndex;
            return candidate.length() > MAX_SHEET_NAME_LENGTH
                    ? candidate.substring(0, MAX_SHEET_NAME_LENGTH)
                    : candidate;
        }
    }

    private int writeTitleBlocks(Sheet sheet, List<ExcelTitleBlock> titleBlocks, CellStyle titleStyle) {
        int maxRow = 0;

        for (ExcelTitleBlock block : titleBlocks) {
            for (int r = block.firstRow(); r <= block.lastRow(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    row = sheet.createRow(r);
                }
                for (int c = block.firstCol(); c <= block.lastCol(); c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellStyle(titleStyle);
                    if (r == block.firstRow() && c == block.firstCol()) {
                        cell.setCellValue(block.text());
                    }
                }
            }

            if (block.firstRow() != block.lastRow() || block.firstCol() != block.lastCol()) {
                sheet.addMergedRegion(new CellRangeAddress(block.firstRow(), block.lastRow(), block.firstCol(), block.lastCol()));
            }

            maxRow = Math.max(maxRow, block.lastRow());
        }

        return maxRow + 1;
    }

    private <T> int writeHeader(Sheet sheet, List<ExcelColumn<T>> columns, CellStyle headerStyle, int rowIndex) {
        Row headerRow = sheet.createRow(rowIndex);

        for (int i = 0; i < columns.size(); i++) {
            String header = columns.get(i).header();

            Cell cell = headerRow.createCell(i);
            cell.setCellValue(header);
            cell.setCellStyle(headerStyle);

            sheet.setColumnWidth(i, Math.min(60, Math.max(12, header.length() + 4)) * 256);
        }

        return rowIndex + 1;
    }

    private CellStyle titleStyle(SXSSFWorkbook workbook, ExcelStyleSettings style) {
        Font font = font(workbook, style, true);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setWrapText(true);

        return cellStyle;
    }

    private CellStyle headerStyle(SXSSFWorkbook workbook, ExcelStyleSettings style) {
        Font font = font(workbook, style, style.headerBold());

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setWrapText(true);
        applyFillColor(cellStyle, style.headerFillColorHex());
        if (style.thinBorder()) {
            applyThinBorder(cellStyle);
        }

        return cellStyle;
    }

    private CellStyle dataStyle(SXSSFWorkbook workbook, ExcelStyleSettings style) {
        Font font = font(workbook, style, false);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyle.setWrapText(true);
        if (style.thinBorder()) {
            applyThinBorder(cellStyle);
        }

        return cellStyle;
    }

    private Font font(SXSSFWorkbook workbook, ExcelStyleSettings style, boolean bold) {
        Font font = workbook.createFont();
        font.setFontName(style.fontName());
        font.setFontHeightInPoints((short) style.fontSize());
        font.setBold(bold);
        return font;
    }

    private void applyFillColor(CellStyle cellStyle, String hex) {
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFColor color = parseHexColor(hex);
        if (color != null && cellStyle instanceof XSSFCellStyle xssfCellStyle) {
            xssfCellStyle.setFillForegroundColor(color);
        } else {
            cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        }
    }

    private XSSFColor parseHexColor(String hex) {
        if (hex == null || hex.isBlank()) {
            return null;
        }

        try {
            Color awtColor = Color.decode("#" + hex.replace("#", ""));
            return new XSSFColor(awtColor, null);
        } catch (NumberFormatException invalidHex) {
            return null;
        }
    }

    private void applyThinBorder(CellStyle cellStyle) {
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
    }

    private void writeCell(Cell cell, Object value, CellStyle style) {
        cell.setCellStyle(style);

        switch (value) {
            case null -> cell.setBlank();
            case Number number -> cell.setCellValue(number.doubleValue());
            case Boolean bool -> cell.setCellValue(bool);
            case Instant instant -> cell.setCellValue(DATE_TIME_FORMAT.format(instant.atZone(ZoneId.systemDefault())));
            case LocalDateTime dateTime -> cell.setCellValue(DATE_TIME_FORMAT.format(dateTime));
            case LocalDate date -> cell.setCellValue(DATE_FORMAT.format(date));
            case Enum<?> enumValue -> cell.setCellValue(enumValue.name());
            default -> cell.setCellValue(value.toString());
        }
    }

    private String sanitizeSheetName(String sheetName) {
        String sanitized = sheetName.replaceAll("[\\\\/*?\\[\\]:]", " ").trim();
        return sanitized.isEmpty() ? "Sheet1" : sanitized;
    }
}
