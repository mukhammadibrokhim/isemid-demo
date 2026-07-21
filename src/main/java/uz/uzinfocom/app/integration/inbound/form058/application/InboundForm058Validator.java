package uz.uzinfocom.app.integration.inbound.form058.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.integration.inbound.common.validation.PatientIdentifierFormatValidator;
import uz.uzinfocom.app.integration.inbound.form058.web.InboundCreateForm058Request;

import java.time.LocalDateTime;

/**
 * Additional validation applied only to the inbound-integration form058
 * submission path, on top of (not instead of) the existing, unmodified
 * {@code Form058CreateValidator} that {@code CreateForm058Service} always
 * runs. Stricter than the frontend create flow: cross-field date ordering
 * and patient-identifier format, neither of which the frontend enforces today.
 */
@Component
@RequiredArgsConstructor
public class InboundForm058Validator {

    private final PatientIdentifierFormatValidator patientIdentifierFormatValidator;

    public void validate(InboundCreateForm058Request request) {
        validateDateOrdering(request);
        patientIdentifierFormatValidator.validate(request.patient());
    }

    private void validateDateOrdering(InboundCreateForm058Request request) {
        InboundCreateForm058Request.DateInfo dateInfo = request.dateInfo();
        requireNotAfter(dateInfo.diseaseDate(), dateInfo.firstVisitDate(),
                "integration.form058.disease-date-after-first-visit-date");
        requireNotAfter(dateInfo.firstVisitDate(), dateInfo.visitDate(),
                "integration.form058.first-visit-date-after-visit-date");
        requireNotBefore(dateInfo.admissionDate(), dateInfo.diseaseDate(),
                "integration.form058.admission-date-before-disease-date");
        requireNotBefore(dateInfo.diagnosisDate(), dateInfo.diseaseDate(),
                "integration.form058.diagnosis-date-before-disease-date");
        requireNotBefore(dateInfo.initialReportDateTime(), dateInfo.diseaseDate(),
                "integration.form058.initial-report-date-time-before-disease-date");
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
