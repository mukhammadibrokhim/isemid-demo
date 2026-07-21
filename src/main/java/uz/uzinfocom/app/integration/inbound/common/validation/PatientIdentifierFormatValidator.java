package uz.uzinfocom.app.integration.inbound.common.validation;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.modules.patient.web.request.CreatePatientIdentifierRequest;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shared, additive identifier-format check for every inbound-integration
 * form type — real data uses both the {@code PASSPORT}/{@code PINFL} and
 * {@code PPN}/{@code NNUZB} type-code conventions (see
 * {@code Form058PdfMapper}), so both are recognized here too.
 */
@Component
public class PatientIdentifierFormatValidator {

    private static final Set<String> PINFL_TYPES = Set.of("PINFL", "NNUZB");
    private static final Pattern PINFL_PATTERN = Pattern.compile("\\d{14}");

    public void validate(PatientRequest patient) {
        List<CreatePatientIdentifierRequest> identifiers = patient.identifiers();

        if (identifiers == null) {
            return;
        }

        for (CreatePatientIdentifierRequest identifier : identifiers) {
            if (identifier.type() == null || !PINFL_TYPES.contains(identifier.type().toUpperCase())) {
                continue;
            }

            if (identifier.value() == null || !PINFL_PATTERN.matcher(identifier.value()).matches()) {
                throw new InboundValidationException("integration.patient.identifier.pinfl-format");
            }
        }
    }
}
