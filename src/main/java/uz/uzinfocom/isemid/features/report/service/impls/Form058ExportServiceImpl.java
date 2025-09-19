package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.enums.AddressType;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;
import uz.uzinfocom.isemid.features.report.service.Form058ExportService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class Form058ExportServiceImpl implements Form058ExportService {
    private final Form058Repository form058Repository;
    private final OrganizationRepository organizationRepository;
    private final CatalogService catalogService;

    @Transactional
    @Override
    public ResponseEntity<?> allExport(User user) {
        return getResponse(user, FilterType.ALL);
    }

    @Transactional
    @Override
    public ResponseEntity<?> receivedExport(User user) {
        return getResponse(user, FilterType.RECEIVED);
    }


    protected ResponseEntity<?> getResponse(User user, FilterType filterType) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);

        Sheet sheet = workbook.createSheet("Юқумли касалликлар");
        Map<String, CellStyle> styles = createStyles(workbook);
        generateHeader(sheet, styles);

        for (int i = 0; i < 16; i++) {
            sheet.setColumnWidth(i, 4000);
        }

        Row headerRow = sheet.createRow(9);
        String[] headers = {
                "№",
                "Бирналмчи тезкор ҳабар номини телефонда ҳабар бериш (қабул қилиш) куни, соати ва жўнатиш (олиш) куни, ким юборган ким олган",
                "Хабар берувчи даволаш профилактика муассасаси номи",
                "Беморнинг фамилия исми шарифи",
                "Ёши (3 ёшгача болган болалар учун туғилган йили ва ойи курсатилсин)",
                "Уй манзили (шахар, қишлоқ, кўча, уй №, кв №)",
                "Иш жойи, ўқиш, мактабгача болалар муассасалари номи, гурухи, синфи, сўнгги марта қатнашган куни",
                "Касал бўлган кун",
                "Биринчи марта мурожаат қилнган кун",
                "Ётқизилган жойи, куни",
                "Ташхис (диагноз) ва у қойилган кун ",
                "Ўзгарган (аниқланган) ташхис (диагноз) ва у тасдиқланган кун",
                "Эпидимиологик текширув ўтказилаган кун, текширув отказган шахс фамилияси)",
                "Касаллик ҳақида ҳабар қилнган (доимий яшаш жойи бўйича ДСEНМга, болалар муассасалари, ўқиш иш жойлари в.б)",
                "Лаборатория текшируи ва унинг натижаси",
                "Эслатма"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        AtomicInteger rowIdx = new AtomicInteger(10);

        try (Stream<Form058> stream = form058Repository.streamAll(getFilteredSpecification(user, filterType))) {
            stream.forEach(form -> {
                Row row = sheet.createRow(rowIdx.get());

                String fullName = Optional.ofNullable(form.getPatient())
                        .map(p -> p.getLastName() + " " + p.getFirstName() + " " + Optional.ofNullable(p.getMiddleName()).orElse(""))
                        .orElse("");

                String ageString = "";
                if (form.getPatient() != null) {
                    if (form.getPatient().getBirthDate() != null) {
                        ageString = form.getPatient().getBirthDate().toString();
                    } else {
                        Integer ageYears = form.getPatient().getAgeYears();
                        Integer ageMonths = form.getPatient().getAgeMonths();
                        if (ageYears != null) ageString += ageYears + " йил";
                        if (ageMonths != null) ageString += " " + ageMonths + " ой";
                    }
                }

                String address = Optional.ofNullable(form.getPatient())
                        .map(p -> p.getAddresses().stream()
                                .filter(a -> AddressType.PERMANENT.equals(a.getType()))
                                .findFirst()
                                .map(a -> Stream.of(
                                        catalogService.findByTypeAndCode(CatalogType.state, a.getStateCode()).map(Catalog::getNameUzCyril).orElse(""),
                                        catalogService.findByTypeAndCode(CatalogType.city, a.getCityCode()).map(Catalog::getNameUzCyril).orElse(""),
                                        catalogService.findByTypeAndCode(CatalogType.neighborhood, a.getNeighborhoodCode()).map(Catalog::getNameUzCyril).orElse(""),
                                        a.getHouseNumber(),
                                        a.getApartmentNumber()
                                ).filter(Objects::nonNull).filter(s -> !s.isBlank()).collect(Collectors.joining(", ")))
                                .orElse("")
                        ).orElse("");

                String icd10 = form.getMkb10Code() + "-" + form.getMkb10Name();
                String icd10Final = form.getFinalMkb10Code() + "-" + form.getFinalMkb10Name();
                String diagnosisDate = form.getDiagnosisDate() != null ? formatter.format(form.getDiagnosisDate()) : "";

                String hospitalName = Optional.ofNullable(form.getHospitalPlaceId())
                        .flatMap(organizationRepository::findById)
                        .map(Organization::getName)
                        .orElse("");
                String hospitalAndDate = String.join(", ", hospitalName, form.getAdmissionDate() != null ? formatter.format(form.getAdmissionDate()) : "");

                Timestamp timestamp = form.getCreatedAt(); // bu java.sql.Timestamp
                LocalDateTime localDateTime = timestamp.toLocalDateTime();
                String createdOrgAndDocName = String.join(", ", form.getCreatedOrg().getName(), form.getNotifierFullName(), formatter.format(localDateTime));

                String receiverORg = Optional.ofNullable(form.getReceiverOrganizationId())
                        .flatMap(organizationRepository::findById)
                        .map(Organization::getName)
                        .orElse(null);

                int index = rowIdx.get() - 9;

                String[] values = new String[]{
                        String.valueOf(index),
                        form.getFirstVisitDate() != null ? formatter.format(form.getFirstVisitDate()) : "",
                        Optional.ofNullable(form.getCreatedOrg()).map(Organization::getName).orElse(""),
                        fullName,
                        ageString,
                        address,
//                        form.getPatient() != null ? Optional.ofNullable(form.getPatient().getWorkPlace()).orElse("") : "",
                        form.getDiseaseDate() != null ? formatter.format(form.getDiseaseDate()) : "",
                        form.getFirstVisitDate() != null ? formatter.format(form.getFirstVisitDate()) : "",
                        hospitalAndDate,
                        String.join(", ", icd10, diagnosisDate),
                        String.join(", ", icd10Final, diagnosisDate),
                        createdOrgAndDocName,
                        receiverORg, "", ""
                };

                for (int i = 0; i < values.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(values[i]);
                    cell.setCellStyle(styles.get("data"));
                }

                rowIdx.incrementAndGet();
            });
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            workbook.dispose();

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentDisposition(ContentDisposition.attachment()
                    .filename("journal_060_" + LocalDate.now() + ".xlsx").build());
            responseHeaders.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(out.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while writing to Excel.");
        }
    }

    private void generateHeader(Sheet sheet, Map<String, CellStyle> styles) {
        for (int i = 0; i <= 4; i++) sheet.createRow(i);

        // Chap va o‘ng qismlarni merge qilish
        sheet.addMergedRegion(new CellRangeAddress(0, 4, 0, 6));  // Chap blok
        sheet.addMergedRegion(new CellRangeAddress(0, 4, 8, 15)); // O‘ng blok
        sheet.addMergedRegion(new CellRangeAddress(0, 4, 7, 7)); // O‘ng blok


        Cell cellLeft = sheet.getRow(0).createCell(0);
        cellLeft.setCellValue("     Ўзбекистон Республикаси\nСоғлиқни сақлаш Вазирлиги\n\n___________________________\nМуассаса номи");
        cellLeft.setCellStyle(styles.get("left"));

        Cell cellRight = sheet.getRow(0).createCell(8);
        cellRight.setCellValue("Ўзбекистон Республикаси\nСоғлиқни сақлаш вазирлигининг\n2020 йил 31 декабрдаги 363 -сонли\nбуйруғи билан тасдиқланган\n060- рақамли тиббий ҳужжат шакли     ");
        cellRight.setCellStyle(styles.get("right"));

        // 5-qator: Бошланган сана
        Row row5 = sheet.createRow(5);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 15));
        Cell bothDates = row5.createCell(0);
        bothDates.setCellValue("      Бошланган сана «____» __________ 20__ йил           Тугатилган сана «____» __________ 20__ йил");
        bothDates.setCellStyle(styles.get("center"));
        row5.setHeightInPoints(30);

        // 6-qator: Тугатилган сана
        sheet.createRow(6);
        sheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 15));

        // 7-qator: Журнал номи
        Row row7 = sheet.createRow(7);
        sheet.addMergedRegion(new CellRangeAddress(7, 7, 0, 15));
        Cell titleCell = row7.createCell(0);
        titleCell.setCellValue("ЮҚУМЛИ КАСАЛЛИКЛАРНИ ҚАЙД ҚИЛИШ\nЖУРНАЛИ");
        titleCell.setCellStyle(styles.get("center"));
        row7.setHeightInPoints(30);

        // 8-qator bo‘sh (ajratish uchun, optional)
        sheet.createRow(8);
    }

    private Map<String, CellStyle> createStyles(Workbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();

        Font boldFont = workbook.createFont();
        boldFont.setBold(true);


        CellStyle left = workbook.createCellStyle();
        left.setFont(boldFont);
        left.setAlignment(HorizontalAlignment.LEFT);
        left.setVerticalAlignment(VerticalAlignment.CENTER);
        left.setBorderTop(BorderStyle.THIN);
        left.setBorderBottom(BorderStyle.THIN);
        left.setBorderLeft(BorderStyle.THIN);
        left.setBorderRight(BorderStyle.THIN);
        left.setWrapText(true);
        styles.put("left", left);

        CellStyle right = workbook.createCellStyle();
        right.setFont(boldFont);
        right.setAlignment(HorizontalAlignment.RIGHT);
        right.setVerticalAlignment(VerticalAlignment.CENTER);
        right.setBorderTop(BorderStyle.THIN);
        right.setBorderBottom(BorderStyle.THIN);
        right.setBorderLeft(BorderStyle.THIN);
        right.setBorderRight(BorderStyle.THIN);
        right.setWrapText(true);
        styles.put("right", right);

        CellStyle center = workbook.createCellStyle();
        center.setFont(boldFont);
        center.setAlignment(HorizontalAlignment.CENTER);
        center.setVerticalAlignment(VerticalAlignment.CENTER);
        center.setWrapText(true);
        center.setBorderTop(BorderStyle.THIN);
        center.setBorderBottom(BorderStyle.THIN);
        center.setBorderLeft(BorderStyle.THIN);
        center.setBorderRight(BorderStyle.THIN);
        styles.put("center", center);

        CellStyle header = workbook.createCellStyle();
        header.setFont(boldFont);
        header.setAlignment(HorizontalAlignment.CENTER);
        header.setVerticalAlignment(VerticalAlignment.CENTER);
        header.setWrapText(true);
        header.setBorderTop(BorderStyle.THIN);
        header.setBorderBottom(BorderStyle.THIN);
        header.setBorderLeft(BorderStyle.THIN);
        header.setBorderRight(BorderStyle.THIN);
        header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("header", header);

        CellStyle data = workbook.createCellStyle();
        data.setWrapText(true);
        data.setVerticalAlignment(VerticalAlignment.TOP);
        data.setBorderTop(BorderStyle.THIN);
        data.setBorderBottom(BorderStyle.THIN);
        data.setBorderLeft(BorderStyle.THIN);
        data.setBorderRight(BorderStyle.THIN);
        styles.put("data", data);

        return styles;
    }

    public static Specification<Form058> getFilteredSpecification(
            User currentUser,
            FilterType filterType
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (currentUser.getCurrentOrganization() != null) {
                UUID orgId = currentUser.getCurrentOrganization().getUuid();

                if (filterType == FilterType.RECEIVED) {
                    predicates.add(criteriaBuilder.equal(root.get("receiverOrganizationId"), currentUser.getCurrentOrganization().getId()));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("createdOrgUuid"), orgId));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
