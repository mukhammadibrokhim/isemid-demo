package uz.uzinfocom.app.integration.api2.citizen.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.api.dto.FieldValidationError;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;

import java.util.List;

public class CitizenLookupValidationException extends Api2Exception {

    private final List<FieldValidationError> fieldErrors;

    public CitizenLookupValidationException(List<FieldValidationError> fieldErrors) {
        super(
                HttpStatus.BAD_REQUEST,
                "CITIZEN_LOOKUP_VALIDATION_FAILED",
                "Citizen lookup request is invalid.",
                "CITIZEN_LOOKUP",
                null
        );
        this.fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    public List<FieldValidationError> fieldErrors() {
        return fieldErrors;
    }
}
