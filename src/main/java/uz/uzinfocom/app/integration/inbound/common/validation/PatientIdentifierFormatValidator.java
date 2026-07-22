package uz.uzinfocom.app.integration.inbound.common.validation;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.modules.patient.web.request.CreatePatientIdentifierRequest;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shared, additive identifier-format check for every inbound-integration
 * form type — real data uses both the {@code PASSPORT}/{@code PINFL} and
 * {@code PPN}/{@code NNUZB} type-code conventions (see
 * {@code Form058PdfMapper}), plus {@code JSHSHIR} - the native Uzbek name for
 * the same identifier PINFL names in Russian - so all three are recognized
 * here too. The message text itself uses NNUZB/JSHSHIR rather than "PINFL",
 * per locale (see i18n bundles) - the caller sent one of those three type
 * codes, never literally "PINFL" as a label a human reads.
 * <p>
 * Beyond "14 digits", a genuine NNUZB/JSHSHIR encodes a gender/century
 * digit, a day-of-month, a month, and a mod-10 check digit over the first 13
 * digits. All of those structural rules report one shared "invalid" message
 * rather than a separate one per rule - which specific rule failed is an
 * internal detail an integrating system doesn't need broken out, only "this
 * isn't a genuine NNUZB/JSHSHIR" vs. "the value isn't even 14 digits."
 */
@Component
public class PatientIdentifierFormatValidator {

    private static final Set<String> PINFL_TYPES = Set.of("PINFL", "NNUZB", "JSHSHIR");
    private static final Pattern DIGITS_14_PATTERN = Pattern.compile("\\d{14}");
    private static final int[] CHECK_DIGIT_WEIGHTS = {7, 3, 1, 7, 3, 1, 7, 3, 1, 7, 3, 1, 7};

    public void validate(PatientRequest patient) {
        List<CreatePatientIdentifierRequest> identifiers = patient.identifiers();

        if (identifiers == null) {
            return;
        }

        for (CreatePatientIdentifierRequest identifier : identifiers) {
            if (identifier.type() == null || !PINFL_TYPES.contains(identifier.type().toUpperCase())) {
                continue;
            }

            validatePinfl(identifier.value());
        }
    }

    private void validatePinfl(String value) {
        if (value == null || !DIGITS_14_PATTERN.matcher(value).matches()) {
            throw new InboundValidationException("integration.patient.identifier.pinfl-format");
        }

        if (!isStructurallyValid(value)) {
            throw new InboundValidationException("integration.patient.identifier.pinfl-invalid");
        }
    }

    private boolean isStructurallyValid(String value) {
        int genderCenturyIndex = Character.digit(value.charAt(0), 10);
        if (genderCenturyIndex < 1 || genderCenturyIndex > 6) {
            return false;
        }

        int day = Integer.parseInt(value.substring(1, 3));
        if (day < 1 || day > 31) {
            return false;
        }

        int month = Integer.parseInt(value.substring(3, 5));
        if (month < 1 || month > 12) {
            return false;
        }

        int checkDigit = Character.digit(value.charAt(13), 10);
        int calculatedCheckDigit = 0;
        for (int i = 0; i < CHECK_DIGIT_WEIGHTS.length; i++) {
            calculatedCheckDigit += Character.digit(value.charAt(i), 10) * CHECK_DIGIT_WEIGHTS[i];
        }

        return calculatedCheckDigit % 10 == checkDigit;
    }
}
