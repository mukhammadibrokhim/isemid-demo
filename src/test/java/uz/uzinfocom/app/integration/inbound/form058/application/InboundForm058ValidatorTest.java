package uz.uzinfocom.app.integration.inbound.form058.application;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.integration.inbound.common.validation.PatientIdentifierFormatValidator;
import uz.uzinfocom.app.integration.inbound.form058.web.InboundCreateForm058Request;
import uz.uzinfocom.app.modules.patient.web.request.CreatePatientIdentifierRequest;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InboundForm058ValidatorTest {

    private final InboundForm058Validator validator = new InboundForm058Validator(new PatientIdentifierFormatValidator());

    @Test
    void acceptsProperlyOrderedDates() {
        assertThatCode(() -> validator.validate(request(
                LocalDateTime.of(2026, 5, 1, 8, 0),
                LocalDateTime.of(2026, 5, 1, 10, 0),
                LocalDateTime.of(2026, 5, 1, 12, 0),
                patientWithPinfl("51506123456785")
        ))).doesNotThrowAnyException();
    }

    @Test
    void rejectsDiseaseDateAfterFirstVisitDate() {
        assertThatThrownBy(() -> validator.validate(request(
                LocalDateTime.of(2026, 5, 2, 8, 0),
                LocalDateTime.of(2026, 5, 1, 10, 0),
                LocalDateTime.of(2026, 5, 1, 12, 0),
                patientWithPinfl("51506123456785")
        ))).isInstanceOf(InboundValidationException.class);
    }

    @Test
    void rejectsFirstVisitDateAfterVisitDate() {
        assertThatThrownBy(() -> validator.validate(request(
                LocalDateTime.of(2026, 5, 1, 8, 0),
                LocalDateTime.of(2026, 5, 1, 12, 0),
                LocalDateTime.of(2026, 5, 1, 10, 0),
                patientWithPinfl("51506123456785")
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

    private PatientRequest patientWithPinfl(String value) {
        return new PatientRequest(
                "First", "Last", null, null, null, null,
                null, null, null, null, null, null, null,
                List.of(new CreatePatientIdentifierRequest("PINFL", value, null, null)),
                List.of(),
                List.of()
        );
    }

    private InboundCreateForm058Request request(
            LocalDateTime diseaseDate,
            LocalDateTime firstVisitDate,
            LocalDateTime visitDate,
            PatientRequest patient
    ) {
        return new InboundCreateForm058Request(
                new InboundCreateForm058Request.DiagnosisInfo("A09", "Diarrhoea", null),
                patient,
                new InboundCreateForm058Request.ClinicalInfo(null, null),
                new InboundCreateForm058Request.DateInfo(null, diseaseDate, firstVisitDate, null, visitDate, visitDate),
                UUID.randomUUID(),
                null,
                new InboundCreateForm058Request.EpidemicInfo("PLACE_CODE", null, null),
                new InboundCreateForm058Request.ReportInfo("Notifier Full Name", "JOURNAL_CODE", null)
        );
    }
}
