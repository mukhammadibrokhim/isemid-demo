package uz.uzinfocom.app.integration.inbound.common.validation;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.modules.patient.web.request.CreatePatientIdentifierRequest;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatientIdentifierFormatValidatorTest {

    private static final String VALID_PINFL = "51506123456785";

    private final PatientIdentifierFormatValidator validator = new PatientIdentifierFormatValidator();

    @Test
    void acceptsAStructurallyValidPinfl() {
        assertThatCode(() -> validator.validate(patientWithIdentifier("PINFL", VALID_PINFL)))
                .doesNotThrowAnyException();
    }

    @Test
    void acceptsTheSameValueUnderTheNnuzbTypeCode() {
        assertThatCode(() -> validator.validate(patientWithIdentifier("NNUZB", VALID_PINFL)))
                .doesNotThrowAnyException();
    }

    @Test
    void isCaseInsensitiveOnTheTypeCode() {
        assertThatCode(() -> validator.validate(patientWithIdentifier("pinfl", VALID_PINFL)))
                .doesNotThrowAnyException();
    }

    @Test
    void acceptsTheSameValueUnderTheJshshirTypeCode() {
        // JSHSHIR is the native Uzbek name for the same identifier PINFL names in Russian.
        assertThatCode(() -> validator.validate(patientWithIdentifier("JSHSHIR", VALID_PINFL)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsAValueThatIsNotFourteenDigits() {
        assertThatThrownBy(() -> validator.validate(patientWithIdentifier("PINFL", "5150612345678")))
                .isInstanceOf(InboundValidationException.class)
                .hasMessage("integration.patient.identifier.pinfl-format");
    }

    @Test
    void rejectsAGenderCenturyDigitOutsideOneToSixWithTheGenericInvalidMessage() {
        assertThatThrownBy(() -> validator.validate(patientWithIdentifier("PINFL", "01506123456780")))
                .isInstanceOf(InboundValidationException.class)
                .hasMessage("integration.patient.identifier.pinfl-invalid");
    }

    @Test
    void rejectsADayOutsideOneToThirtyOneWithTheGenericInvalidMessage() {
        assertThatThrownBy(() -> validator.validate(patientWithIdentifier("PINFL", "53206123456780")))
                .isInstanceOf(InboundValidationException.class)
                .hasMessage("integration.patient.identifier.pinfl-invalid");
    }

    @Test
    void rejectsAMonthOutsideOneToTwelveWithTheGenericInvalidMessage() {
        assertThatThrownBy(() -> validator.validate(patientWithIdentifier("PINFL", "51513123456780")))
                .isInstanceOf(InboundValidationException.class)
                .hasMessage("integration.patient.identifier.pinfl-invalid");
    }

    @Test
    void rejectsAStructurallyValidValueWithAWrongCheckDigitWithTheGenericInvalidMessage() {
        // Same day/month/century as VALID_PINFL - 14 digits, valid structure - only the
        // trailing check digit differs. This must report a distinct message from the
        // "must be 14 digits" one, but not break out which specific structural rule
        // failed - that's an internal detail, not something an integrating system needs.
        assertThatThrownBy(() -> validator.validate(patientWithIdentifier("PINFL", "51506123456780")))
                .isInstanceOf(InboundValidationException.class)
                .hasMessage("integration.patient.identifier.pinfl-invalid");
    }

    @Test
    void doesNotFormatCheckIdentifierTypesOtherThanPinflOrNnuzb() {
        assertThatCode(() -> validator.validate(patientWithIdentifier("PASSPORT", "not-a-pinfl-shaped-value")))
                .doesNotThrowAnyException();
    }

    private PatientRequest patientWithIdentifier(String type, String value) {
        return new PatientRequest(
                "First", "Last", null, null, null, null,
                null, null, null, null, null, null, null,
                List.of(new CreatePatientIdentifierRequest(type, value, null, null)),
                List.of(),
                List.of()
        );
    }
}
