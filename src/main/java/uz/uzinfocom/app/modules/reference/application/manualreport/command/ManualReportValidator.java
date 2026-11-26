package uz.uzinfocom.app.modules.reference.application.manualreport.command;

import uz.uzinfocom.app.modules.reference.domain.ManualReportAgeUnit;
import uz.uzinfocom.app.modules.reference.domain.ManualReportCaseSource;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.reference.domain.Icd10;
import uz.uzinfocom.app.modules.reference.domain.ManualReportType;
import uz.uzinfocom.app.modules.reference.repository.CatalogRepository;
import uz.uzinfocom.app.modules.reference.repository.Icd10Repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Checks only the values a request <em>adds</em>. Values already stored on the
 * entry are accepted as they are: the catalog was seeded from legacy data, and an
 * old tag or code must not block an unrelated edit such as renaming the entry.
 */
@Component
@RequiredArgsConstructor
class ManualReportValidator {

    private static final Set<String> ALLOWED_REPORT_TYPES = Arrays.stream(ManualReportType.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    private static final LocalDate AGE_REFERENCE_DATE = LocalDate.of(2001, 1, 1);

    private static final String DISEASE_PLACE_CATALOG = "DISEASE_PLACE";

    private final Icd10Repository icd10Repository;
    private final CatalogRepository catalogRepository;

    /**
     * @param reportTypes normalized (trimmed, upper-case) report types from the request
     * @param icd10Codes normalized (trimmed, upper-case) ICD-10 codes from the request
     * @param storedReportTypes report types the entry already has; empty on create
     * @param storedIcd10Codes ICD-10 codes the entry already has; empty on create
     */
    void validate(
            Set<String> reportTypes,
            Set<String> icd10Codes,
            Collection<String> storedReportTypes,
            Collection<String> storedIcd10Codes
    ) {
        if (icd10Codes.isEmpty()) {
            throw ManualReportValidationException.icd10CodesRequired();
        }

        validateReportTypes(added(reportTypes, storedReportTypes));
        validateIcd10Codes(added(icd10Codes, storedIcd10Codes));
    }

    /** A bound needs its unit (and vice versa), and the lower bound must fall before the upper one. */
    void validateAgeBounds(
            Integer ageFrom, ManualReportAgeUnit ageFromUnit, Integer ageTo, ManualReportAgeUnit ageToUnit
    ) {
        if ((ageFrom == null) != (ageFromUnit == null) || (ageTo == null) != (ageToUnit == null)) {
            throw ManualReportValidationException.ageUnitMismatch();
        }

        if (ageFrom != null && ageTo != null
                && !dateAtAge(ageFrom, ageFromUnit).isBefore(dateAtAge(ageTo, ageToUnit))) {
            throw ManualReportValidationException.ageBoundsOrder();
        }
    }

    /** Form 058-1 has no lab-confirmation field, so such a row could never count anything. */
    void validateCaseFilters(ManualReportCaseSource caseSource, Boolean labConfirmation) {
        if (caseSource == ManualReportCaseSource.FORM_058_1 && labConfirmation != null) {
            throw ManualReportValidationException.labConfirmationUnavailable();
        }
    }

    /**
     * Disease places exist only on Form 058, and a newly added one must be in the catalog.
     *
     * @param requested normalized (trimmed, upper-case) codes from the request
     * @param stored codes the entry already has; empty on create
     */
    void validateDiseasePlaces(Set<String> requested, Collection<String> stored, ManualReportCaseSource caseSource) {
        if (caseSource == ManualReportCaseSource.FORM_058_1 && !requested.isEmpty()) {
            throw ManualReportValidationException.diseasePlaceUnavailable();
        }

        List<String> unknown = added(requested, stored).stream()
                .filter(code -> catalogRepository
                        .findByTypeAndCodeAndDeletedFalse(DISEASE_PLACE_CATALOG, code)
                        .isEmpty())
                .toList();

        if (!unknown.isEmpty()) {
            throw ManualReportValidationException.unknownDiseasePlaces(String.join(", ", unknown));
        }
    }

    /** The day a person born on a fixed reference date reaches this age - makes mixed units comparable. */
    private static LocalDate dateAtAge(int value, ManualReportAgeUnit unit) {
        return switch (unit) {
            case DAY -> AGE_REFERENCE_DATE.plusDays(value);
            case MONTH -> AGE_REFERENCE_DATE.plusMonths(value);
            case YEAR -> AGE_REFERENCE_DATE.plusYears(value);
        };
    }

    private void validateReportTypes(Set<String> added) {
        List<String> unknown = added.stream()
                .filter(type -> !ALLOWED_REPORT_TYPES.contains(type))
                .toList();

        if (!unknown.isEmpty()) {
            throw ManualReportValidationException.unknownReportTypes(
                    String.join(", ", unknown),
                    Arrays.stream(ManualReportType.values()).map(Enum::name).collect(Collectors.joining(", "))
            );
        }
    }

    private void validateIcd10Codes(Set<String> added) {
        if (added.isEmpty()) {
            return;
        }

        Set<String> unknown = new TreeSet<>(added);
        icd10Repository.findAllByCodeInAndDeletedFalse(added).stream()
                .map(Icd10::getCode)
                .forEach(unknown::remove);

        if (!unknown.isEmpty()) {
            throw ManualReportValidationException.unknownIcd10Codes(String.join(", ", unknown));
        }
    }

    private static Set<String> added(Set<String> requested, Collection<String> stored) {
        Set<String> storedUpper = stored.stream()
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());

        return requested.stream()
                .filter(value -> !storedUpper.contains(value))
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
