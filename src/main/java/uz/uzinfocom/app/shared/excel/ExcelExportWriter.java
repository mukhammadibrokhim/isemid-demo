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
     * Opens a new workbook for writing one sheet (rolling over to further same-named
     * sheets automatically past {@link ExcelStyleSettings#rowsPerSheet()}). The caller must
     * {@link ExcelRowWriter#write} every row and then {@link ExcelRowWriter#close()} it
     * (ideally via try-with-resources) to flush the workbook to {@code out} - nothing is
     * written to {@code out} before then.
     */
    public <T> ExcelRowWriter<T> open(
            OutputStream out,
            String sheetBaseName,
            List<ExcelTitleBlock> titleBlocks,
            List<ExcelColumn<T>> columns,
            ExcelStyleSettings style
    ) {
        SXSSFWorkbook workbook = newWorkbook();
        SheetCursor<T> cursor = new SheetCursor<>(workbook, sheetBaseName, titleBlocks, columns, style);

        return new ExcelRowWriter<T>() {
            @Override
            public void write(T item) {
                cursor.write(item);
            }

            @Override
            public void close() throws IOException {
                try {
                    workbook.write(out);
                } finally {
                    workbook.close();
                }
            }
        };
    }

    /**
     * Opens a new workbook that can hold several independent sheets - see
     * {@link ExcelWorkbookWriter} for how it differs from {@link #open}. Nothing is written
     * to {@code out} until the returned writer's own {@code close()}.
     */
    public ExcelWorkbookWriter openWorkbook(OutputStream out) {
        SXSSFWorkbook workbook = newWorkbook();

        return new ExcelWorkbookWriter() {
            @Override
            public <T> ExcelRowWriter<T> newSheet(
                    String sheetBaseName, List<ExcelTitleBlock> titleBlocks, List<ExcelColumn<T>> columns, ExcelStyleSettings style
            ) {
                SheetCursor<T> cursor = new SheetCursor<>(workbook, sheetBaseName, titleBlocks, columns, style);
                return new ExcelRowWriter<T>() {
                    @Override
                    public void write(T item) {
                        cursor.write(item);
                    }

                    @Override
                    public void close() {
                        // No-op: this workbook's own close() below finalizes the whole file
                        // once, after every sheet has been written.
                    }
                };
            }

            @Override
            public void close() throws IOException {
                try {
                    workbook.write(out);
                } finally {
                    workbook.close();
                }
            }
        };
    }

    private SXSSFWorkbook newWorkbook() {
        SXSSFWorkbook workbook = new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE);
        workbook.setCompressTempFiles(true);
        return workbook;
    }

    /**
     * Mutable, single-use cursor tracking which sheet is currently being written to and
     * how many data rows have landed on it - rolls over to a new sheet transparently once
     * {@link ExcelStyleSettings#rowsPerSheet()} is reached. Writes into a workbook it is
     * handed, never creates or finalizes one itself - that is the caller's job ({@link #open}
     * or {@link #openWorkbook}), since a multi-sheet workbook holds several of these.
     */
    private final class SheetCursor<T> {
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
                SXSSFWorkbook workbook,
                String sheetBaseName,
                List<ExcelTitleBlock> titleBlocks,
                List<ExcelColumn<T>> columns,
                ExcelStyleSettings style
        ) {
            this.workbook = workbook;
            this.sheetBaseName = sheetBaseName;
            this.titleBlocks = titleBlocks;
            this.columns = columns;
            this.titleStyle = titleStyle(workbook, style);
            this.headerStyle = headerStyle(workbook, style);
            this.dataStyle = dataStyle(workbook, style);
            this.rowsPerSheet = Math.max(1, style.rowsPerSheet());

            startNewSheet();
        }

        private void write(T item) {
            if (rowsOnCurrentSheet >= rowsPerSheet) {
                startNewSheet();
            }

            Row row = sheet.createRow(nextRowIndex++);
            for (int i = 0; i < columns.size(); i++) {
                writeCell(row.createCell(i), columns.get(i).valueExtractor().apply(item), dataStyle);
            }
            rowsOnCurrentSheet++;
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
        int maxDepth = columns.stream().mapToInt(column -> column.groupHeaders().size()).max().orElse(0);
        return maxDepth == 0
                ? writeFlatHeader(sheet, columns, headerStyle, rowIndex)
                : writeGroupedHeader(sheet, columns, headerStyle, rowIndex, maxDepth);
    }

    private <T> int writeFlatHeader(Sheet sheet, List<ExcelColumn<T>> columns, CellStyle headerStyle, int rowIndex) {
        Row headerRow = sheet.createRow(rowIndex);

        for (int i = 0; i < columns.size(); i++) {
            String header = columns.get(i).header();

            Cell cell = headerRow.createCell(i);
            cell.setCellValue(header);
            cell.setCellStyle(headerStyle);

            sheet.setColumnWidth(i, Math.clamp(header.length() + 4, 12, 60) * 256);
        }

        return rowIndex + 1;
    }

    /**
     * {@code maxDepth}-deep header for exports whose columns fall into nested named groups
     * (e.g. "Joriy davr" over "Umumiy aholi" over "2025"/"2026"/"O'sish %", each with its own
     * Absolyut/Intensiv sub-columns) - one merged cell per run of adjacent columns sharing the
     * same group label at each level, matching however the frontend table nests the same
     * headers. A column shallower than {@code maxDepth} (including one with no groups at all)
     * has its own {@code header} vertically merged from the row after its last group level down
     * through the leaf row, so it lines up with the deepest column's combined height - this is
     * the {@code maxDepth == 1} case's "group == null -> header spans both rows" behavior,
     * generalized to any depth.
     */
    private <T> int writeGroupedHeader(Sheet sheet, List<ExcelColumn<T>> columns, CellStyle headerStyle, int rowIndex, int maxDepth) {
        Row[] rows = new Row[maxDepth + 1];
        for (int r = 0; r <= maxDepth; r++) {
            rows[r] = sheet.createRow(rowIndex + r);
        }

        for (int level = 0; level < maxDepth; level++) {
            int i = 0;
            while (i < columns.size()) {
                List<String> path = columns.get(i).groupHeaders();
                if (path.size() <= level) {
                    i++;
                    continue;
                }

                int runEnd = i;
                while (runEnd + 1 < columns.size() && sharesGroupPrefix(columns.get(runEnd + 1).groupHeaders(), path, level)) {
                    runEnd++;
                }

                setHeaderCell(rows[level].createCell(i), path.get(level), headerStyle);
                for (int c = i + 1; c <= runEnd; c++) {
                    setHeaderCell(rows[level].createCell(c), null, headerStyle);
                }
                if (runEnd > i) {
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex + level, rowIndex + level, i, runEnd));
                }

                i = runEnd + 1;
            }
        }

        for (int c = 0; c < columns.size(); c++) {
            ExcelColumn<T> column = columns.get(c);
            int depth = column.groupHeaders().size();

            if (depth == maxDepth) {
                setHeaderCell(rows[maxDepth].createCell(c), column.header(), headerStyle);
            } else {
                setHeaderCell(rows[depth].createCell(c), column.header(), headerStyle);
                for (int r = depth + 1; r <= maxDepth; r++) {
                    setHeaderCell(rows[r].createCell(c), null, headerStyle);
                }
                sheet.addMergedRegion(new CellRangeAddress(rowIndex + depth, rowIndex + maxDepth, c, c));
            }

            sheet.setColumnWidth(c, Math.clamp(column.header().length() + 4, 12, 60) * 256);
        }

        return rowIndex + maxDepth + 1;
    }

    /** Whether two columns' group paths agree on every level up to and including {@code level}. */
    private boolean sharesGroupPrefix(List<String> candidate, List<String> path, int level) {
        if (candidate.size() <= level) {
            return false;
        }
        for (int i = 0; i <= level; i++) {
            if (!java.util.Objects.equals(candidate.get(i), path.get(i))) {
                return false;
            }
        }
        return true;
    }

    private void setHeaderCell(Cell cell, String value, CellStyle style) {
        cell.setCellStyle(style);
        if (value != null) {
            cell.setCellValue(value);
        }
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
