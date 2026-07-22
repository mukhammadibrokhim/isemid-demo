package uz.uzinfocom.app.integration.inbound.dmed.form058.application;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.integration.inbound.common.validation.PatientIdentifierFormatValidator;
import uz.uzinfocom.app.integration.inbound.dmed.form058.web.DmedCreateForm058Request;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;
import uz.uzinfocom.app.modules.patient.web.request.CreatePatientAddressRequest;
import uz.uzinfocom.app.modules.patient.web.request.CreatePatientIdentifierRequest;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DmedForm058ValidatorTest {

    private final DmedForm058Validator validator = new DmedForm058Validator(new PatientIdentifierFormatValidator());

    @Test
    void acceptsProperlyOrderedDates() {
        assertThatCode(() -> validator.validate(request(
                LocalDateTime.of(2026, 5, 1, 8, 0),
                LocalDateTime.of(2026, 5, 1, 10, 0),
                LocalDateTime.of(2026, 5, 1, 12, 0),
                patientWithPinfl("12345678901234")
        ))).doesNotThrowAnyException();
    }

    @Test
    void rejectsDiseaseDateAfterFirstVisitDate() {
        assertThatThrownBy(() -> validator.validate(request(
                LocalDateTime.of(2026, 5, 2, 8, 0),
                LocalDateTime.of(2026, 5, 1, 10, 0),
                LocalDateTime.of(2026, 5, 1, 12, 0),
                patientWithPinfl("12345678901234")
        ))).isInstanceOf(InboundValidationException.class);
    }

    @Test
    void rejectsFirstVisitDateAfterVisitDate() {
        assertThatThrownBy(() -> validator.validate(request(
                LocalDateTime.of(2026, 5, 1, 8, 0),
                LocalDateTime.of(2026, 5, 1, 12, 0),
                LocalDateTime.of(2026, 5, 1, 10, 0),
                patientWithPinfl("12345678901234")
        ))).isInstanceOf(InboundValidationException.class);
    }

    @Test
    void rejectsAMalformedPinfl() {
        assertThatThrownBy(() -> validator.validate(request(
                LocalDateTime.of(2026, 5, 1, 8, 0),
                LocalDateTime.of(2026, 5, 1, 10, 0),
                LocalDateTime.of(2026, 5, 1, 12, 0),
                patientWithPinfl("not-a-pinfl")
        ))).isInstanceOf(InboundValidationException.class);
    }

    @Test
    void rejectsAPatientWithNoPermanentAddress() {
        PatientRequest patient = new PatientRequest(
                "First", "Last", null, null, null, null,
                null, null, null, null, null, null, null,
                List.of(new CreatePatientIdentifierRequest("PINFL", "12345678901234", null, null)),
                List.of(new CreatePatientAddressRequest(
                        AddressType.TEMPORARY, "UZ-TK", "TK-283", null, null, null, null)),
                List.of()
        );

        assertThatThrownBy(() -> validator.validate(request(
                LocalDateTime.of(2026, 5, 1, 8, 0),
                LocalDateTime.of(2026, 5, 1, 10, 0),
                LocalDateTime.of(2026, 5, 1, 12, 0),
                patient
        ))).isInstanceOf(InboundValidationException.class);
    }

    private PatientRequest patientWithPinfl(String value) {
        return new PatientRequest(
                "First", "Last", null, null, null, null,
                null, null, null, null, null, null, null,
                List.of(new CreatePatientIdentifierRequest("PINFL", value, null, null)),
                List.of(new CreatePatientAddressRequest(
                        AddressType.PERMANENT, "UZ-TK", "TK-283", null, null, null, null)),
                List.of()
        );
    }

    private DmedCreateForm058Request request(
            LocalDateTime diseaseDate,
            LocalDateTime firstVisitDate,
            LocalDateTime visitDate,
            PatientRequest patient
    ) {
        return new DmedCreateForm058Request(
                "A09", "Diarrhoea", null,
                null, null,
                patient,
                null, null,
                null, diseaseDate, firstVisitDate, null, visitDate,
                UUID.randomUUID(),
                null,
                "PLACE_CODE",
                visitDate,
                null, null,
                "Notifier Full Name",
                "JOURNAL_CODE",
                null
        );
    }
}
