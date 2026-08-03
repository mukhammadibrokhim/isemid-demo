package uz.uzinfocom.app.modules.form058.application.export;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form058.application.query.Form058Filter;
import uz.uzinfocom.app.modules.form058.application.query.Form058QueryService;
import uz.uzinfocom.app.modules.form058.application.query.dto.pdf.Form058PdfAddressResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.pdf.Form058PdfResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.pdf.Form058PdfWorkplaceResponse;
import uz.uzinfocom.app.modules.form058.application.query.mapper.Form058PdfMapper;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.shared.excel.ExcelColumn;
import uz.uzinfocom.app.shared.excel.ExcelTitleBlock;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reproduces the official "060-рақамли тиббий ҳужжат" infectious-disease registration
 * journal layout for Form058, backed by the same {@link Form058PdfMapper} the single-record
 * {@code /pdf} endpoint already uses (so every coded field - region, gender, disease place -
 * is resolved to its display name the same way).
 * <p>
 * The date-range/diagnosis/address combining rules below are a best-effort reading of a
 * real exported journal file, not a specification handed down by a domain expert - treat
 * the exact wording of combined cells as adjustable, not load-bearing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Form058ExcelExportSource implements ExcelExportSource<Form058Filter, Form058PdfResponse> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String[] UZBEK_MONTHS = {
            "январь", "февраль", "март", "апрель", "май", "июнь",
            "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"
    };

    private final Form058JpaRepository repository;
    private final Form058QueryService form058QueryService;
    private final Form058PdfMapper form058PdfMapper;

    @Override
    public String exportType() {
        return "FORM058";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form058Filter filter) {
        return repository.count(form058QueryService.resolveSpecification(filter));
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(
            Form058Filter filter,
            Consumer<Form058PdfResponse> rowProcessor
    ) {
        Specification<Form058> spec = form058QueryService.resolveSpecification(filter);
        Sort sort = form058QueryService.resolvePageable(filter).getSort();

        try (Stream<Form058> entities = repository.findBy(spec, query -> query.sortBy(sort).stream())) {
            entities.forEach(entity -> {
                try {
                    rowProcessor.accept(form058PdfMapper.toPdfResponse(entity, List.of()));
                } catch (Exception mappingFailure) {
                    // A row referencing an organization that's since been deactivated/merged
                    // (common in years-old historical data - see OrganizationIdResolver) would
                    // otherwise abort the whole export on one bad record. Skipped, not
                    // silently: logged with the form058 id so the underlying data issue can
                    // still be investigated separately.
                    log.warn("event=export_row_skipped exportType=FORM058 form058Id={} reason={}",
                            entity.getId(), mappingFailure.getMessage(), mappingFailure);
                }
            });
        }
    }

    @Override
    public List<ExcelColumn<Form058PdfResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("id", "№", Form058PdfResponse::id),
                ExcelColumn.of("notifyInfo", "Бирламчи хабар вақти ва ким юборган/олган", this::notifyInfo),
                ExcelColumn.of("senderOrg", "Хабар берувчи муассаса", Form058PdfResponse::institutionName),
                ExcelColumn.of("patientFio", "Бемор ФИШ", row -> row.patient() == null ? null : row.patient().fullName()),
                ExcelColumn.of("age", "Ёши", this::ageCell),
                ExcelColumn.of("address", "Манзили", row -> formatAddress(row.permanentAddress(), row.currentAddress())),
                ExcelColumn.of("workplace", "Иш/ўқиш жойи", this::workplace),
                ExcelColumn.of("diseaseDate", "Касал бўлган кун", row -> row.dateInfo() == null ? null : row.dateInfo().diseaseDate()),
                ExcelColumn.of("firstVisitDate", "Биринчи мурожаат санаси", row -> row.dateInfo() == null ? null : row.dateInfo().firstVisitDate()),
                ExcelColumn.of("hospitalization", "Ётқизилган жойи, куни", this::hospitalization),
                ExcelColumn.of("diagnosis", "Ташхис ва қўйилган сана", this::diagnosis),
                ExcelColumn.of("finalDiagnosis", "Якуний ташхис ва сана", this::finalDiagnosis),
                ExcelColumn.of("epidemicInvestigation", "Эпидемиологик текширув", this::epidemicInvestigation),
                ExcelColumn.of("notifiedOrg", "Хабар қилинган ташкилот", Form058PdfResponse::sanepidOrganizationName),
                ExcelColumn.of("labResult", "Лаборатория натижаси", row -> Boolean.TRUE.equals(row.labConfirmation()) ? "Тасдиқланган" : null),
                ExcelColumn.of("note", "Эслатма", Form058PdfResponse::comment)
        );
    }

    @Override
    public List<ExcelTitleBlock> titleBlocks(Form058Filter filter) {
        int lastCol = availableColumns().size() - 1;

        return List.of(
                new ExcelTitleBlock("Ўзбекистон Республикаси\nСоғлиқни сақлаш вазирлиги", 0, 4, 0, Math.min(6, lastCol)),
                new ExcelTitleBlock(
                        "Ўзбекистон Республикаси\nСоғлиқни сақлаш вазирлигининг\n2020 йил 31 декабрдаги 363-сонли\nбуйруғи билан тасдиқланган\n060-рақамли тиббий ҳужжат шакли",
                        0, 4, Math.min(7, lastCol), lastCol
                ),
                new ExcelTitleBlock(dateRangeText(filter), 5, 5, 0, lastCol),
                new ExcelTitleBlock("ЮҚУМЛИ КАСАЛЛИКЛАРНИ ҚАЙД ҚИЛИШ ЖУРНАЛИ", 6, 6, 0, lastCol)
        );
    }

    @Override
    public String sheetName() {
        return "Юкумли касалликлар";
    }

    @Override
    public String fileNamePrefix() {
        return "form058_export";
    }

    private String notifyInfo(Form058PdfResponse row) {
        if (row.dateInfo() == null || row.dateInfo().initialReportDateTime() == null) {
            return null;
        }
        return row.dateInfo().initialReportDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    private String ageCell(Form058PdfResponse row) {
        if (row.patient() == null || row.patient().birthDate() == null) {
            return null;
        }

        LocalDate birthDate = row.patient().birthDate();
        Period age = Period.between(birthDate, LocalDate.now());

        String ageText = age.getYears() > 0
                ? age.getYears() + " йош"
                : age.getMonths() > 0
                        ? age.getMonths() + " ойлик"
                        : age.getDays() + " кунлик";

        return DATE_FORMAT.format(birthDate) + " — " + ageText;
    }

    private String workplace(Form058PdfResponse row) {
        Form058PdfWorkplaceResponse workplace = row.workplace() != null ? row.workplace() : row.educationalInstitution();
        return workplace == null ? null : workplace.organizationName();
    }

    private String formatAddress(Form058PdfAddressResponse permanent, Form058PdfAddressResponse current) {
        String permanentText = formatSingleAddress(permanent);
        String currentText = formatSingleAddress(current);

        StringBuilder result = new StringBuilder();
        if (StringUtils.hasText(permanentText)) {
            result.append("Доимий: ").append(permanentText);
        }
        if (StringUtils.hasText(currentText)) {
            if (!result.isEmpty()) {
                result.append("\n\n");
            }
            result.append("Яшаш: ").append(currentText);
        }

        return result.isEmpty() ? null : result.toString();
    }

    private String formatSingleAddress(Form058PdfAddressResponse address) {
        if (address == null) {
            return null;
        }

        return Stream.of(
                        address.regionName(), address.districtName(), address.neighborhoodName(),
                        address.streetAddress(), address.houseNumber()
                )
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(", "));
    }

    private String hospitalization(Form058PdfResponse row) {
        String place = row.hospitalPlaceName();
        LocalDate admissionDate = row.dateInfo() == null ? null : row.dateInfo().admissionDate();

        if (!StringUtils.hasText(place) && admissionDate == null) {
            return null;
        }

        return joinNonBlank(place, admissionDate == null ? null : DATE_FORMAT.format(admissionDate));
    }

    private String diagnosis(Form058PdfResponse row) {
        if (row.diagnosisInfo() == null) {
            return null;
        }

        String codeAndName = joinCodeAndName(row.diagnosisInfo().icd10Code(), row.diagnosisInfo().icd10Name());
        LocalDate diagnosisDate = row.dateInfo() == null ? null : row.dateInfo().diagnosisDate();

        return joinNonBlank(codeAndName, diagnosisDate == null ? null : DATE_FORMAT.format(diagnosisDate));
    }

    private String finalDiagnosis(Form058PdfResponse row) {
        if (row.diagnosisInfo() == null || !StringUtils.hasText(row.diagnosisInfo().finalIcd10Code())) {
            return null;
        }

        String codeAndName = joinCodeAndName(row.diagnosisInfo().finalIcd10Code(), row.diagnosisInfo().finalIcd10Name());
        LocalDate diagnosisDate = row.dateInfo() == null ? null : row.dateInfo().diagnosisDate();

        return joinNonBlank(codeAndName, diagnosisDate == null ? null : DATE_FORMAT.format(diagnosisDate));
    }

    private String epidemicInvestigation(Form058PdfResponse row) {
        String initialReport = row.dateInfo() == null || row.dateInfo().initialReportDateTime() == null
                ? null
                : row.dateInfo().initialReportDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

        return joinNonBlank(row.diseasePlaceName(), row.notifierFullName(), initialReport);
    }

    private String joinCodeAndName(String code, String name) {
        if (!StringUtils.hasText(code) && !StringUtils.hasText(name)) {
            return null;
        }
        if (!StringUtils.hasText(name)) {
            return code;
        }
        return StringUtils.hasText(code) ? code + "-" + name : name;
    }

    private String joinNonBlank(String... parts) {
        String joined = Stream.of(parts)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(", "));
        return joined.isEmpty() ? null : joined;
    }

    private String dateRangeText(Form058Filter filter) {
        String from = filter.dateFrom() == null ? "—" : formatUzbekDate(filter.dateFrom());
        String to = filter.dateTo() == null ? "—" : formatUzbekDate(filter.dateTo());
        return "Бошланган сана " + from + "   Тугатилган сана " + to;
    }

    private String formatUzbekDate(LocalDate date) {
        return "«" + date.getDayOfMonth() + "» " + UZBEK_MONTHS[date.getMonthValue() - 1] + " " + date.getYear() + " йил";
    }
}
