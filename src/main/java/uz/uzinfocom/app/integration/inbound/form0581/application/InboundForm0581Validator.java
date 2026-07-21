package uz.uzinfocom.app.integration.inbound.form0581.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.integration.inbound.common.validation.PatientIdentifierFormatValidator;
import uz.uzinfocom.app.integration.inbound.form0581.web.InboundCreateForm0581Request;

import java.time.LocalDateTime;

/**
 * Additional validation applied only to the inbound-integration form0581
 * submission path, on top of (not instead of) the existing, unmodified
 * {@code Form0581CreateValidator} that {@code CreateForm0581Service} always
 * runs.
 */
@Component
@RequiredArgsConstructor
public class InboundForm0581Validator {

    private final PatientIdentifierFormatValidator patientIdentifierFormatValidator;

    public void validate(InboundCreateForm0581Request request) {
        validateDateOrdering(request);
        patientIdentifierFormatValidator.validate(request.patient());
    }

    private void validateDateOrdering(InboundCreateForm0581Request request) {
        InboundCreateForm0581Request.IncidentInfo incidentInfo = request.incidentInfo();
        requireNotAfter(incidentInfo.injuryDateTime(), incidentInfo.dpuVisitDateTime(),
                "integration.form0581.injury-date-time-after-dpu-visit-date-time");

        InboundCreateForm0581Request.HospitalizationInfo hospitalizationInfo = request.hospitalizationInfo();
        if (hospitalizationInfo != null) {
            requireNotBefore(hospitalizationInfo.hospitalizedAt(), incidentInfo.injuryDateTime(),
                    "integration.form0581.hospitalized-at-before-injury-date-time");
        }
    }

    private void requireNotAfter(LocalDateTime earlier, LocalDateTime later, String messageCode) {
        if (earlier != null && later != null && earlier.isAfter(later)) {
            throw new InboundValidationException(messageCode);
        }
    }

    private void requireNotBefore(LocalDateTime value, LocalDateTime reference, String messageCode) {
        if (value != null && reference != null && value.isBefore(reference)) {
            throw new InboundValidationException(messageCode);
        }
    }
}
