package uz.uzinfocom.app.integration.inbound.dmed.form0581.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.integration.inbound.common.validation.PatientIdentifierFormatValidator;
import uz.uzinfocom.app.integration.inbound.dmed.form0581.web.DmedCreateForm0581Request;

import java.time.LocalDateTime;

/**
 * Additional validation applied only to the DMED form058-1 submission path,
 * on top of (not instead of) the existing, unmodified
 * {@code Form0581CreateValidator} that {@code CreateForm0581Service} always
 * runs. Same rules as the generic inbound-integration path
 * ({@code InboundForm0581Validator}), just applied to DMED's flat request
 * shape instead of the nested one.
 */
@Component
@RequiredArgsConstructor
public class DmedForm0581Validator {

    private final PatientIdentifierFormatValidator patientIdentifierFormatValidator;

    public void validate(DmedCreateForm0581Request request) {
        validateDateOrdering(request);
        patientIdentifierFormatValidator.validate(request.patient().identifiers());
    }

    private void validateDateOrdering(DmedCreateForm0581Request request) {
        requireNotAfter(request.injuryDateTime(), request.dpuVisitDateTime(),
                "integration.form0581.injury-date-time-after-dpu-visit-date-time");
        requireNotBefore(request.hospitalizedAt(), request.injuryDateTime(),
                "integration.form0581.hospitalized-at-before-injury-date-time");
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
