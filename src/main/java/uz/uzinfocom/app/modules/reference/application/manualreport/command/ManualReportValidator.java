package uz.uzinfocom.app.modules.reference.application.manualreport.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.reference.domain.Icd10;
import uz.uzinfocom.app.modules.reference.domain.ManualReportType;
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

    private final Icd10Repository icd10Repository;

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
