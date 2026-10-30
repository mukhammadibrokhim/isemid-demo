package uz.uzinfocom.app.modules.patient.application.query.mapper.helper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;
import uz.uzinfocom.app.platform.security.mask.SensitiveIdentifierMasker;

/**
 * Masks a patient identifier's value in responses for every identifier type listed in
 * {@link SensitiveIdentifierMasker#SENSITIVE_TYPES}, unless the caller is a super administrator.
 */
@Component
@RequiredArgsConstructor
public class PatientIdentifierMaskingHelper {

    private final SensitiveIdentifierMasker masker;

    @Named("maskIdentifierValue")
    public String maskIdentifierValue(PatientIdentifier identifier) {
        if (identifier == null) {
            return null;
        }
        return masker.mask(identifier.getTypeCode(), identifier.getValue());
    }
}
