package uz.uzinfocom.isemid.features.report.utils;

import com.sun.management.OperatingSystemMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;
import uz.uzinfocom.isemid.features.patient.models.Patient;
import uz.uzinfocom.isemid.features.patient.reposiotries.PatientRepository;
import uz.uzinfocom.isemid.features.report.mapper.Form058RowMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uz.uzinfocom.isemid.features.report.utils.ReportUtil.buildDateText;
import static uz.uzinfocom.isemid.features.report.utils.ReportUtil.catalogCacheKey;

@Slf4j
@Component
@RequiredArgsConstructor
public class Form058ExcelWriter {
    private static final int HEADER_ROW_INDEX = 9;
    private static final int BATCH_SIZE = 500;
    private static final List<CatalogType> ADDRESS_CATALOG_TYPES =
            List.of(CatalogType.state, CatalogType.city, CatalogType.neighborhood);

    private static final MemoryMXBean MEMORY_BEAN = ManagementFactory.getMemoryMXBean();
    private static final OperatingSystemMXBean OS_BEAN =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    private final Form058RowMapper rowMapper;
    private final PatientRepository patientRepository;
    private final OrganizationRepository organizationRepository;
    private final CatalogService catalogService;

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

    /**
     * {@code write}ning N+1'siz, disk-yozuvchi varianti (V3 fon jarayoni uchun). Har qator
     * uchun DB'ga alohida murojaat qilish o'rniga, streamni {@code BATCH_SIZE} bo'lakларга
     * bo'lib, har bo'lak uchun bemor/tashkilot ma'lumotlarini bitta so'rov bilan (batch)
     * oldindan yuklab, keyin qatorlarni yozadi. Katalog (state/city/neighborhood) nomlari
     * esa export boshida bir marta to'liq keshlanadi. Butun faylni
     * heap'da {@code byte[]} sifatida ushlab turmaydi — {@code workbook.write(...)} to'g'ridan-to'g'ri
     * berilgan fayl manziliga oqadi, shu bilan RAM sarfini kamaytiradi.
     */
    public void writeV3(Stream<Form058> formStream, LocalDate fromDate, LocalDate toDate,
                        String name, Path targetFile) throws IOException {
        long startedAt = System.currentTimeMillis();
        long cpuTimeStartNs = OS_BEAN.getProcessCpuTime();
        long heapBeforeMb = usedHeapMb();

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             OutputStream out = Files.newOutputStream(targetFile)) {

            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Юқумли касалликлар");
            Map<String, CellStyle> styles = createStyles(workbook);

            generateHeader(sheet, workbook, fromDate, toDate, name);
            setColumnWidths(sheet);
            writeTableHeader(sheet, styles);
            writeDataRowsV2(sheet, styles, formStream);

            workbook.write(out);

            long wallMs = System.currentTimeMillis() - startedAt;
            long cpuMs = (OS_BEAN.getProcessCpuTime() - cpuTimeStartNs) / 1_000_000;
            long heapAfterMb = usedHeapMb();
            double cpuLoadPercent = wallMs > 0
                    ? (cpuMs * 100.0) / (wallMs * Math.max(1, OS_BEAN.getAvailableProcessors()))
                    : 0;

            log.info("[PROFILE] Form058ExcelWriter.writeV3 (disk, {}): umumiy={} ms, heap oldin={} MB, heap keyin={} MB, delta={} MB, CPU vaqti={} ms, CPU load={}% ({} core)",
                    targetFile.getFileName(), wallMs, heapBeforeMb, heapAfterMb, heapAfterMb - heapBeforeMb, cpuMs,
                    String.format("%.1f", cpuLoadPercent), OS_BEAN.getAvailableProcessors());
        }
    }

    private long usedHeapMb() {
        return MEMORY_BEAN.getHeapMemoryUsage().getUsed() / (1024 * 1024);
    }

    private void writeDataRowsV2(Sheet sheet, Map<String, CellStyle> styles, Stream<Form058> stream) {
        AtomicInteger rowIdx = new AtomicInteger(HEADER_ROW_INDEX + 1);
        Map<String, String> catalogCache = buildCatalogCache();

        Iterator<Form058> iterator = stream.iterator();
        List<Form058> batch = new ArrayList<>(BATCH_SIZE);

        while (iterator.hasNext()) {
            batch.add(iterator.next());
            if (batch.size() == BATCH_SIZE) {
                writeBatchV2(sheet, styles, batch, catalogCache, rowIdx);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            writeBatchV2(sheet, styles, batch, catalogCache, rowIdx);
        }
    }

    private void writeBatchV2(Sheet sheet, Map<String, CellStyle> styles, List<Form058> batch,
                              Map<String, String> catalogCache, AtomicInteger rowIdx) {
        Set<Long> patientIds = new HashSet<>();
        Set<Long> orgIds = new HashSet<>();
        Set<UUID> orgUuids = new HashSet<>();

        for (Form058 form : batch) {
            if (form.getPatient() != null) {
                patientIds.add(form.getPatient().getId());
            }
            if (form.getHospitalPlaceId() != null) {
                orgIds.add(form.getHospitalPlaceId());
            }
            if (form.getReceiverOrganizationId() != null) {
                orgIds.add(form.getReceiverOrganizationId());
            }
            if (form.getCreatedOrgUuid() != null) {
                orgUuids.add(form.getCreatedOrgUuid());
            }
        }

        Map<Long, Patient> patientCache = patientRepository.findAllWithAddressesByIdIn(patientIds).stream()
                .collect(Collectors.toMap(Patient::getId, p -> p));

        Map<Long, Organization> orgByIdCache = organizationRepository.findAllById(orgIds).stream()
                .collect(Collectors.toMap(Organization::getId, o -> o));

        Map<UUID, Organization> orgByUuidCache = organizationRepository.findAllByUuidIn(orgUuids).stream()
                .collect(Collectors.toMap(Organization::getUuid, o -> o));

        for (Form058 form : batch) {
            Row row = sheet.createRow(rowIdx.get());

            String[] values = rowMapper.toRowV2(form, patientCache, orgByIdCache, orgByUuidCache, catalogCache);
            for (int i = 0; i < values.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(values[i]);
                cell.setCellStyle(styles.get("data"));
            }
            rowIdx.incrementAndGet();
        }
    }

    private Map<String, String> buildCatalogCache() {
        Map<String, String> cache = new HashMap<>();
        for (CatalogType type : ADDRESS_CATALOG_TYPES) {
            for (Catalog catalog : catalogService.findAllByType(type)) {
                cache.put(catalogCacheKey(type, catalog.getCode()), catalog.getNameUzCyril());
            }
        }
        return cache;
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

        removeGridlinesInHeaderArea(sheet, workbook);

        // Chap yozuv
        CellRangeAddress leftRegion = new CellRangeAddress(0, 4, 0, 6);
        sheet.addMergedRegion(leftRegion);
        removeBorders(leftRegion, sheet);
        clearMergedRegionCells(sheet, workbook, leftRegion);

        String leftText = "Ўзбекистон Республикаси\n" +
                "Соғлиқни сақлаш Вазирлиги\n\n" +
                (name != null && !name.isBlank() ? name : "Муассаса номи") + "\n" +
                "___________________________\nМуассаса номи";

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

    private void removeGridlinesInHeaderArea(Sheet sheet, Workbook workbook) {
        CellStyle whiteNoBorder = workbook.createCellStyle();
        whiteNoBorder.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        whiteNoBorder.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        whiteNoBorder.setBorderTop(BorderStyle.NONE);
        whiteNoBorder.setBorderBottom(BorderStyle.NONE);
        whiteNoBorder.setBorderLeft(BorderStyle.NONE);
        whiteNoBorder.setBorderRight(BorderStyle.NONE);

        for (int r = 0; r <= 8; r++) {
            Row row = sheet.getRow(r);
            if (row == null) row = sheet.createRow(r);
            for (int c = 0; c <= 15; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) cell = row.createCell(c);
                cell.setCellStyle(whiteNoBorder);
            }
        }
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
