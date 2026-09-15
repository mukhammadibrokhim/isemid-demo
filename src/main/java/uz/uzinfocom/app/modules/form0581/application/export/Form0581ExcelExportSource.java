package uz.uzinfocom.app.modules.form0581.application.export;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581Filter;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581QueryService;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581SortFields;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfAddressResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfLocationResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfOtherInjuredPersonResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfWorkplaceResponse;
import uz.uzinfocom.app.modules.form0581.application.query.mapper.Form0581PdfMapper;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.shared.excel.ExcelColumn;
import uz.uzinfocom.app.shared.excel.ExcelTitleBlock;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reproduces the same official-journal letterhead layout {@code Form058ExcelExportSource}
 * uses for Form058, backed by the same kind of print-oriented mapper - {@link Form0581PdfMapper} -
 * so every coded field (region, gender, animal category, marital status, etc.) is resolved to
 * its display name the same way the single-record {@code /pdf} endpoint already does.
 * <p>
 * The date-range/diagnosis/address combining rules below mirror {@code Form058ExcelExportSource}'s
 * best-effort reading of a real exported journal file - treat the exact wording of combined
 * cells as adjustable, not load-bearing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Form0581ExcelExportSource implements ExcelExportSource<Form0581Filter, Form0581PdfResponse> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final String[] UZBEK_MONTHS = {
            "январь", "февраль", "март", "апрель", "май", "июнь",
            "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"
    };

    /**
     * See {@code Form058ExcelExportSource} for the rationale - each row is built from a
     * fully-loaded {@link Form0581} plus its lazily fetched associations (patient, addresses,
     * incident/animal/owner info, other injured people), so the persistence context has to be
     * detached periodically to keep a large streamed export's heap use flat.
     */
    private static final int PERSISTENCE_CONTEXT_CLEAR_EVERY_N_ROWS = 500;

    private final Form0581JpaRepository repository;
    private final Form0581QueryService form0581QueryService;
    private final Form0581PdfMapper form0581PdfMapper;
    private final EntityManager entityManager;

    @Override
    public String exportType() {
        return "FORM0581";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form0581Filter filter) {
        return repository.count(form0581QueryService.resolveSpecification(filter));
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form0581Filter filter, Consumer<Form0581PdfResponse> rowProcessor) {
        Specification<Form0581> spec = form0581QueryService.resolveSpecification(filter);
        Sort sort = PageableUtils.of(filter, Form0581SortFields.ALLOWED).getSort();

        long[] processed = {0};
        try (Stream<Form0581> entities = repository.findBy(spec, query -> query.sortBy(sort).stream())) {
            entities.forEach(entity -> {
                try {
                    rowProcessor.accept(form0581PdfMapper.toPdfResponse(entity));
                } catch (Exception mappingFailure) {
                    // A row referencing an organization that's since been deactivated/merged
                    // would otherwise abort the whole export on one bad record. Skipped, not
                    // silently: logged with the form0581 id so the underlying data issue can
                    // still be investigated separately.
                    log.warn("event=export_row_skipped exportType=FORM0581 form0581Id={} reason={}",
                            entity.getId(), mappingFailure.getMessage(), mappingFailure);
                }

                // Detach after the row is mapped (the mapper has already resolved every
                // association it needs) so the context stays bounded across a large export.
                // Safe mid-stream: clearing the session doesn't close the underlying
                // server-side cursor the Stream reads from.
                if (++processed[0] % PERSISTENCE_CONTEXT_CLEAR_EVERY_N_ROWS == 0) {
                    entityManager.clear();
                }
            });
        }
    }

    @Override
    public List<ExcelColumn<Form0581PdfResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("id", "№", Form0581PdfResponse::id),
                ExcelColumn.of("notifyInfo", "Хабар юборилган вақти", this::notifyInfo),
                ExcelColumn.of("senderOrg", "Хабар берувчи муассаса", Form0581PdfResponse::institutionName),
                ExcelColumn.of("patientFio", "Бемор ФИШ", row -> row.patient() == null ? null : row.patient().fullName()),
                ExcelColumn.of("age", "Ёши", this::ageCell),
                ExcelColumn.of("address", "Манзили", row -> formatAddress(row.permanentAddress(), row.currentAddress())),
                ExcelColumn.of("workplace", "Иш/ўқиш жойи", this::workplace),
                ExcelColumn.of("injuryDate", "Жароҳатланган/чаққан сана", this::injuryDate),
                ExcelColumn.of("incidentAddress", "Ҳодиса содир бўлган жой", this::incidentAddress),
                ExcelColumn.of("dpuVisitDate", "ДПУга мурожаат санаси", this::dpuVisitDate),
                ExcelColumn.of("hospitalization", "Ётқизилган жойи, куни", this::hospitalization),
                ExcelColumn.of("diagnosis", "Ташхис (МКБ-10)", this::diagnosis),
                ExcelColumn.of("finalDiagnosis", "Якуний ташхис (МКБ-10)", this::finalDiagnosis),
                ExcelColumn.of("animalInfo", "Ҳайвон тўғрисида маълумот", this::animalInfo),
                ExcelColumn.of("animalOwner", "Ҳайвон эгаси", this::animalOwner),
                ExcelColumn.of("otherInjured", "Бошқа жабрланганлар", this::otherInjured),
                ExcelColumn.of("notifiedOrg", "Хабар қилинган ташкилот", Form0581PdfResponse::sanepidOrganizationName),
                ExcelColumn.of("antirabicAssistance", "Кўрсатилган антираб ёрдам", Form0581PdfResponse::antirabicAssistanceInfo),
                ExcelColumn.of("reportedBy", "Хабар берган/қабул қилган", this::reportedBy)
        );
    }

    @Override
    public List<ExcelTitleBlock> titleBlocks(Form0581Filter filter) {
        int lastCol = availableColumns().size() - 1;

        return List.of(
                new ExcelTitleBlock("Ўзбекистон Республикаси\nСоғлиқни сақлаш вазирлиги", 0, 4, 0, Math.min(6, lastCol)),
                new ExcelTitleBlock("058/1-рақамли тиббий ҳужжат шакли", 0, 4, Math.min(7, lastCol), lastCol),
                new ExcelTitleBlock(dateRangeText(filter), 5, 5, 0, lastCol),
                new ExcelTitleBlock("ҲАЙВОН ЧАҚҚАН/ЖАРОҲАТЛАНГАН ШАХСЛАРНИ ҚАЙД ҚИЛИШ ЖУРНАЛИ", 6, 6, 0, lastCol)
        );
    }

    @Override
    public String sheetName() {
        return "Форма 058-1";
    }

    @Override
    public String fileNamePrefix() {
        return "form0581_export";
    }

    private String notifyInfo(Form0581PdfResponse row) {
        return row.messageSentAt() == null ? null : DATE_TIME_FORMAT.format(row.messageSentAt());
    }

    /**
     * Age is computed as of the form's creation date, not the moment the export runs - see
     * {@code Form058ExcelExportSource#ageCell} for the rationale.
     */
    private String ageCell(Form0581PdfResponse row) {
        if (row.patient() == null || row.patient().birthDate() == null || row.createdAt() == null) {
            return null;
        }

        LocalDate birthDate = row.patient().birthDate();
        LocalDate referenceDate = row.createdAt().atZone(ZoneId.systemDefault()).toLocalDate();
        Period age = Period.between(birthDate, referenceDate);

        String ageText = age.getYears() > 0
                ? age.getYears() + " йош"
                : age.getMonths() > 0
                        ? age.getMonths() + " ойлик"
                        : age.getDays() + " кунлик";

        return DATE_FORMAT.format(birthDate) + " — " + ageText;
    }

    private String workplace(Form0581PdfResponse row) {
        Form0581PdfWorkplaceResponse workplace = row.workplace() != null ? row.workplace() : row.educationalInstitution();
        return workplace == null ? null : workplace.organizationName();
    }

    private String formatAddress(Form0581PdfAddressResponse permanent, Form0581PdfAddressResponse current) {
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

    private String formatSingleAddress(Form0581PdfAddressResponse address) {
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

    private String injuryDate(Form0581PdfResponse row) {
        LocalDateTime injuryDateTime = row.incidentInfo() == null ? null : row.incidentInfo().injuryDateTime();
        return injuryDateTime == null ? null : DATE_TIME_FORMAT.format(injuryDateTime);
    }

    private String dpuVisitDate(Form0581PdfResponse row) {
        LocalDateTime dpuVisitDateTime = row.incidentInfo() == null ? null : row.incidentInfo().dpuVisitDateTime();
        return dpuVisitDateTime == null ? null : DATE_TIME_FORMAT.format(dpuVisitDateTime);
    }

    private String incidentAddress(Form0581PdfResponse row) {
        if (row.incidentInfo() == null) {
            return null;
        }
        return joinNonBlank(
                row.incidentInfo().injuryRegionName(),
                row.incidentInfo().injuryDistrictName(),
                row.incidentInfo().injuryAddress()
        );
    }

    private String hospitalization(Form0581PdfResponse row) {
        if (row.hospitalizationInfo() == null) {
            return null;
        }

        String place = row.hospitalizationInfo().hospitalOrganizationName();
        LocalDateTime hospitalizedAt = row.hospitalizationInfo().hospitalizedAt();

        return joinNonBlank(place, hospitalizedAt == null ? null : DATE_TIME_FORMAT.format(hospitalizedAt));
    }

    private String diagnosis(Form0581PdfResponse row) {
        if (row.diagnosisInfo() == null) {
            return null;
        }
        return joinCodeAndName(row.diagnosisInfo().icd10Code(), row.diagnosisInfo().icd10Name());
    }

    private String finalDiagnosis(Form0581PdfResponse row) {
        if (row.diagnosisInfo() == null || !StringUtils.hasText(row.diagnosisInfo().finalIcd10Code())) {
            return null;
        }
        return joinCodeAndName(row.diagnosisInfo().finalIcd10Code(), row.diagnosisInfo().finalIcd10Name());
    }

    private String animalInfo(Form0581PdfResponse row) {
        if (row.animalInfo() == null) {
            return null;
        }
        return joinNonBlank(
                row.animalInfo().animalCategoryName(), row.animalInfo().animalType(),
                row.animalInfo().animalBreed(), row.animalInfo().animalColor()
        );
    }

    private String animalOwner(Form0581PdfResponse row) {
        if (row.animalOwnerInfo() == null) {
            return null;
        }
        return joinNonBlank(row.animalOwnerInfo().fullName(), formatLocation(row.animalOwnerInfo().address()));
    }

    private String otherInjured(Form0581PdfResponse row) {
        if (row.otherInjuredPeople() == null || row.otherInjuredPeople().isEmpty()) {
            return null;
        }

        String joined = row.otherInjuredPeople().stream()
                .map(this::formatOtherInjuredPerson)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("; "));

        return joined.isEmpty() ? null : joined;
    }

    private String formatOtherInjuredPerson(Form0581PdfOtherInjuredPersonResponse person) {
        return joinNonBlank(person.fullName(), formatLocation(person.address()));
    }

    private String formatLocation(Form0581PdfLocationResponse location) {
        if (location == null) {
            return null;
        }
        return Stream.of(
                        location.regionName(), location.districtName(), location.neighborhoodName(),
                        location.street(), location.houseNumber()
                )
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(", "));
    }

    private String reportedBy(Form0581PdfResponse row) {
        return joinNonBlank(row.notifierFullName(), row.receiverFullName());
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

    private String dateRangeText(Form0581Filter filter) {
        String from = filter.dateFrom() == null ? "—" : formatUzbekDate(filter.dateFrom());
        String to = filter.dateTo() == null ? "—" : formatUzbekDate(filter.dateTo());
        return "Бошланган сана " + from + "   Тугатилган сана " + to;
    }

    private String formatUzbekDate(LocalDate date) {
        return "«" + date.getDayOfMonth() + "» " + UZBEK_MONTHS[date.getMonthValue() - 1] + " " + date.getYear() + " йил";
    }
}
