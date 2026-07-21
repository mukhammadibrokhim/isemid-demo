package uz.uzinfocom.app.integration.inbound.form0581.application;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.integration.inbound.common.validation.PatientIdentifierFormatValidator;
import uz.uzinfocom.app.integration.inbound.form0581.web.InboundCreateForm0581Request;
import uz.uzinfocom.app.modules.patient.web.request.CreatePatientIdentifierRequest;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InboundForm0581ValidatorTest {

    private final InboundForm0581Validator validator =
            new InboundForm0581Validator(new PatientIdentifierFormatValidator());

    @Test
    void acceptsProperlyOrderedDates() {
        assertThatCode(() -> validator.validate(request(
                LocalDateTime.of(2026, 5, 1, 8, 0),
                LocalDateTime.of(2026, 5, 1, 10, 0)
        ))).doesNotThrowAnyException();
    }

    @Test
    void rejectsInjuryDateTimeAfterDpuVisitDateTime() {
        assertThatThrownBy(() -> validator.validate(request(
                LocalDateTime.of(2026, 5, 1, 12, 0),
                LocalDateTime.of(2026, 5, 1, 10, 0)
        ))).isInstanceOf(InboundValidationException.class);
    }

    private PatientRequest patient() {
        return new PatientRequest(
                "First", "Last", null, null, null, null,
                null, null, null, null, null, null, null,
                List.of(new CreatePatientIdentifierRequest("PINFL", "12345678901234", null, null)),
                List.of(),
                List.of()
        );
    }

    private InboundCreateForm0581Request request(LocalDateTime injuryDateTime, LocalDateTime dpuVisitDateTime) {
        return new InboundCreateForm0581Request(
                new InboundCreateForm0581Request.DiagnosisInfo("A82", "Rabies", null),
                new InboundCreateForm0581Request.IncidentInfo(
                        injuryDateTime, dpuVisitDateTime, "REGION_CODE", "DISTRICT_CODE", null),
                null,
                null,
                patient(),
                UUID.randomUUID(),
                null,
                null,
                null,
                new InboundCreateForm0581Request.ReportInfo(null, "Notifier Full Name", null, null)
        );
    }
}
