package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.report.mapper.Form058RowMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class Form058ExcelWriter {
    private static final int HEADER_ROW_INDEX = 9;
    private final Form058RowMapper rowMapper;

    public byte[] write(Stream<Form058> formStream, LocalDate fromDate, LocalDate toDate,
                        String name) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Юқумли касалликлар");
            Map<String, CellStyle> styles = createStyles(workbook);

            generateHeader(sheet, workbook, fromDate, toDate, name);
            setColumnWidths(sheet);
            writeTableHeader(sheet, styles);
            writeDataRows(sheet, styles, formStream);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void writeTableHeader(Sheet sheet, Map<String, CellStyle> styles) {
        Row headerRow = sheet.createRow(HEADER_ROW_INDEX);
        String[] headers = {
                "№",
                "Бирламчи хабар вақти ва ким юборган/олган",
                "Хабар берувчи муассаса",
                "Бемор ФИШ",
                "Ёши",
                "Манзили",
                "Иш/ўқиш жойи",
                "Касал бўлган кун",
                "Биринчи мурожаат санаси",
                "Ётқизилган жойи, куни",
                "Ташхис ва қўйилган сана",
                "Якуний ташхис ва сана",
                "Эпидемиологик текширув",
                "Хабар қилинган ташкилот",
                "Лаборатория натижаси",
                "Эслатма"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
    }

    private void writeDataRows(Sheet sheet, Map<String, CellStyle> styles, Stream<Form058> stream) {
        AtomicInteger rowIdx = new AtomicInteger(HEADER_ROW_INDEX + 1);

        stream.forEach(form -> {
            Row row = sheet.createRow(rowIdx.get());

            String[] values = rowMapper.toRow(form);
            for (int i = 0; i < values.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(values[i]);
                cell.setCellStyle(styles.get("data"));
            }
            rowIdx.incrementAndGet();
        });
    }

    private void setColumnWidths(Sheet sheet) {
        for (int i = 0; i < 16; i++) {
            sheet.setColumnWidth(i, 4000);
        }
    }

    // ===== Header (no borders) =====
    private void generateHeader(Sheet sheet, SXSSFWorkbook workbook, LocalDate fromDate, LocalDate toDate,
                                String name) {
        for (int i = 0; i <= 8; i++) sheet.createRow(i);

        CellStyle centerNoBorder = createNoBorderStyle(workbook);
        sheet.setDisplayGridlines(false);
        // Chap yozuv
        CellRangeAddress leftRegion = new CellRangeAddress(0, 4, 0, 6);
        sheet.addMergedRegion(leftRegion);
        removeBorders(leftRegion, sheet);
        clearMergedRegionCells(sheet, workbook, leftRegion);

        String leftText = "Ўзбекистон Республикаси\n" +
                "Соғлиқни сақлаш Вазирлиги\n\n" +
                (name != null && !name.isBlank() ? name : "Муассаса номи") + "\n" +
                "___________________________";

        Cell cellLeft = sheet.getRow(0).createCell(0);
        cellLeft.setCellValue(leftText);
        cellLeft.setCellStyle(centerNoBorder);

        // Bo‘sh ustun (H ustun, index = 7)
        CellRangeAddress emptyRegion = new CellRangeAddress(0, 4, 7, 7);
        sheet.addMergedRegion(emptyRegion);
        removeBorders(emptyRegion, sheet);
        clearMergedRegionCells(sheet, workbook, emptyRegion);

        // O‘ng yozuv
        CellRangeAddress rightRegion = new CellRangeAddress(0, 4, 8, 15);
        sheet.addMergedRegion(rightRegion);
        removeBorders(rightRegion, sheet);
        clearMergedRegionCells(sheet, workbook, rightRegion);

        Cell cellRight = sheet.getRow(0).createCell(8);
        cellRight.setCellValue("Ўзбекистон Республикаси\nСоғлиқни сақлаш вазирлигининг\n2020 йил 31 декабрдаги 363-сонли\nбуйруғи билан тасдиқланган\n060-рақамли тиббий ҳужжат шакли");
        cellRight.setCellStyle(centerNoBorder);

        // Sanalar qatori (5-qator)
        Row row5 = sheet.getRow(5);
        if (row5 == null) row5 = sheet.createRow(5);
        CellRangeAddress dateRegion = new CellRangeAddress(5, 5, 0, 15);
        sheet.addMergedRegion(dateRegion);
        removeBorders(dateRegion, sheet);
        clearMergedRegionCells(sheet, workbook, dateRegion);

        Cell bothDates = row5.createCell(0);
        bothDates.setCellValue(buildDateText(fromDate, toDate));
        bothDates.setCellStyle(centerNoBorder);
        row5.setHeightInPoints(30);

        // Bo‘sh 6-qator
        Row row6 = sheet.getRow(6);
        if (row6 == null) row6 = sheet.createRow(6);
        CellRangeAddress emptyRow6 = new CellRangeAddress(6, 6, 0, 15);
        sheet.addMergedRegion(emptyRow6);
        removeBorders(emptyRow6, sheet);
        clearMergedRegionCells(sheet, workbook, emptyRow6);

        // Sarlavha (7-qator)
        Row row7 = sheet.getRow(7);
        if (row7 == null) row7 = sheet.createRow(7);
        CellRangeAddress titleRegion = new CellRangeAddress(7, 7, 0, 15);
        sheet.addMergedRegion(titleRegion);
        removeBorders(titleRegion, sheet);
        clearMergedRegionCells(sheet, workbook, titleRegion);

        Cell titleCell = row7.createCell(0);
        titleCell.setCellValue("ЮҚУМЛИ КАСАЛЛИКЛАРНИ ҚАЙД ҚИЛИШ\nЖУРНАЛИ");
        titleCell.setCellStyle(centerNoBorder);
        row7.setHeightInPoints(30);

        // Separator (8-qator)
        Row row8 = sheet.getRow(8);
        if (row8 == null) row8 = sheet.createRow(8);
        CellRangeAddress emptyRow = new CellRangeAddress(8, 8, 0, 15);
        sheet.addMergedRegion(emptyRow);
        removeBorders(emptyRow, sheet);
        clearMergedRegionCells(sheet, workbook, emptyRow);
    }

    private String buildDateText(LocalDate fromDate, LocalDate toDate) {
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd");
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMMM", new Locale("ru")); // кирилл
        DateTimeFormatter yearFmt = DateTimeFormatter.ofPattern("yyyy");

        // fromDate
        String fromDay = (fromDate != null) ? fromDate.format(dayFmt) : "____";
        String fromMonth = (fromDate != null) ? fromDate.format(monthFmt) : "________";
        String fromYear = (fromDate != null) ? fromDate.format(yearFmt) : "20__";

        // toDate
        String toDay = (toDate != null) ? toDate.format(dayFmt) : "____";
        String toMonth = (toDate != null) ? toDate.format(monthFmt) : "________";
        String toYear = (toDate != null) ? toDate.format(yearFmt) : "20__";

        return "Бошланган сана «" + fromDay + "» " + fromMonth + " " + fromYear + " йил   " +
                "Тугатилган сана «" + toDay + "» " + toMonth + " " + toYear + " йил";
    }


    private void removeBorders(CellRangeAddress region, Sheet sheet) {
        RegionUtil.setBorderTop(BorderStyle.NONE, region, sheet);
        RegionUtil.setBorderBottom(BorderStyle.NONE, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.NONE, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.NONE, region, sheet);
    }

    // Borderlarsiz style
    private CellStyle createNoBorderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);

        return style;
    }

    private void clearMergedRegionCells(Sheet sheet, Workbook workbook, CellRangeAddress region) {
        CellStyle whiteNoBorder = workbook.createCellStyle();
        whiteNoBorder.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        whiteNoBorder.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        whiteNoBorder.setBorderTop(BorderStyle.NONE);
        whiteNoBorder.setBorderBottom(BorderStyle.NONE);
        whiteNoBorder.setBorderLeft(BorderStyle.NONE);
        whiteNoBorder.setBorderRight(BorderStyle.NONE);

        for (int r = region.getFirstRow(); r <= region.getLastRow(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) row = sheet.createRow(r);
            for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
                Cell cell = row.getCell(c);
                if (cell == null) cell = row.createCell(c);
                cell.setCellStyle(whiteNoBorder);
            }
        }
    }

    // ===== Styles =====
    private Map<String, CellStyle> createStyles(Workbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();

        Font boldFont = workbook.createFont();
        boldFont.setBold(true);

        styles.put("left", buildStyle(workbook, boldFont, HorizontalAlignment.LEFT));
        styles.put("right", buildStyle(workbook, boldFont, HorizontalAlignment.RIGHT));
        styles.put("center", buildStyle(workbook, boldFont, HorizontalAlignment.CENTER));

        CellStyle header = buildStyle(workbook, boldFont, HorizontalAlignment.CENTER);
        header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("header", header);

        CellStyle data = workbook.createCellStyle();
        data.setWrapText(true);
        data.setVerticalAlignment(VerticalAlignment.TOP);
        setBorders(data);
        styles.put("data", data);

        return styles;
    }

    private CellStyle buildStyle(Workbook workbook, Font font, HorizontalAlignment alignment) {
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(alignment);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        setBorders(style);
        return style;
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
