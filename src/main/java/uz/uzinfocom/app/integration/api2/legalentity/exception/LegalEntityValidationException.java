package uz.uzinfocom.app.integration.api2.legalentity.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.api.dto.FieldValidationError;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;

import java.util.List;

public class LegalEntityValidationException extends Api2Exception {

    private final List<FieldValidationError> fieldErrors;

    public LegalEntityValidationException(List<FieldValidationError> fieldErrors) {
        super(
                HttpStatus.BAD_REQUEST,
                "LEGAL_ENTITY_VALIDATION_FAILED",
                "Legal entity lookup request is invalid.",
                "LEGAL_ENTITY_TIN_LOOKUP",
                null
        );
        this.fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    public List<FieldValidationError> fieldErrors() {
        return fieldErrors;
    }
}
