package uz.uzinfocom.app.integration.inbound.dmed.form058.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.integration.inbound.common.validation.PatientIdentifierFormatValidator;
import uz.uzinfocom.app.integration.inbound.dmed.form058.web.DmedCreateForm058Request;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;
import uz.uzinfocom.app.modules.patient.web.request.CreatePatientAddressRequest;

import java.time.LocalDateTime;

/**
 * Additional validation applied only to the DMED form058 submission path, on
 * top of (not instead of) the existing, unmodified {@code Form058CreateValidator}
 * that {@code CreateForm058Service} always runs. Same rules as the generic
 * inbound-integration path ({@code InboundForm058Validator}), just applied to
 * DMED's flat request shape instead of the nested one.
 */
@Component
@RequiredArgsConstructor
public class DmedForm058Validator {

    private final PatientIdentifierFormatValidator patientIdentifierFormatValidator;

    public void validate(DmedCreateForm058Request request) {
        validateDateOrdering(request);
        patientIdentifierFormatValidator.validate(request.patient());
        requirePermanentAddress(request);
    }

    private void requirePermanentAddress(DmedCreateForm058Request request) {
        boolean hasPermanentAddress = request.patient().addresses().stream()
                .map(CreatePatientAddressRequest::type)
                .anyMatch(AddressType.PERMANENT::equals);

        if (!hasPermanentAddress) {
            throw new InboundValidationException("integration.patient.address.permanent-required");
        }
    }

    private void validateDateOrdering(DmedCreateForm058Request request) {
        requireNotAfter(request.diseaseDate(), request.firstVisitDate(),
                "integration.form058.disease-date-after-first-visit-date");
        requireNotAfter(request.firstVisitDate(), request.visitDate(),
                "integration.form058.first-visit-date-after-visit-date");
        requireNotBefore(request.admissionDate(), request.diseaseDate(),
                "integration.form058.admission-date-before-disease-date");
        requireNotBefore(request.diagnosisDate(), request.diseaseDate(),
                "integration.form058.diagnosis-date-before-disease-date");
        requireNotBefore(request.initialReportDateTime(), request.diseaseDate(),
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
