package uz.uzinfocom.app.integration.inbound.dmed.form0581.application;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.integration.inbound.common.validation.PatientIdentifierFormatValidator;
import uz.uzinfocom.app.integration.inbound.common.web.IntegrationPatientRequest;
import uz.uzinfocom.app.integration.inbound.dmed.form0581.web.DmedCreateForm0581Request;
import uz.uzinfocom.app.modules.patient.web.request.CreatePatientIdentifierRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DmedForm0581ValidatorTest {

    private final DmedForm0581Validator validator = new DmedForm0581Validator(new PatientIdentifierFormatValidator());

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

    @Test
    void rejectsAMalformedPinfl() {
        assertThatThrownBy(() -> validator.validate(request(
                LocalDateTime.of(2026, 5, 1, 8, 0),
                LocalDateTime.of(2026, 5, 1, 10, 0),
                "not-a-pinfl"
        ))).isInstanceOf(InboundValidationException.class);
    }

    private IntegrationPatientRequest patient(String pinfl) {
        return new IntegrationPatientRequest(
                "First", "Last", null, null, null, null,
                null, null, null, null, null, null, null,
                List.of(new CreatePatientIdentifierRequest("PINFL", pinfl, null, null)),
                List.of(),
                List.of()
        );
    }

    private DmedCreateForm0581Request request(LocalDateTime injuryDateTime, LocalDateTime dpuVisitDateTime) {
        return request(injuryDateTime, dpuVisitDateTime, "51506123456785");
    }

    private DmedCreateForm0581Request request(
            LocalDateTime injuryDateTime, LocalDateTime dpuVisitDateTime, String pinfl) {
        return new DmedCreateForm0581Request(
                "A82", "Rabies", null,
                injuryDateTime, dpuVisitDateTime, "REGION_CODE", "DISTRICT_CODE", null,
                null, null, null, null,
                null,
                patient(pinfl),
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                null,
                "Notifier Full Name",
                null,
                null
        );
    }
}
