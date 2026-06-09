package uz.uzinfocom.app.integration.api2.citizen.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.api.dto.FieldValidationError;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;

import java.util.List;

public class UnsupportedCitizenLookupTypeException extends Api2Exception {

    private final List<FieldValidationError> fieldErrors;

    public UnsupportedCitizenLookupTypeException() {
        super(
                HttpStatus.BAD_REQUEST,
                "CITIZEN_LOOKUP_TYPE_UNSUPPORTED",
                "api2.citizen.error.unsupported_type",
                "CITIZEN_LOOKUP",
                null,
                List.of(new FieldValidationError(
                        "type",
                        "validation.citizen_type.allowed"
                ))
        );
        this.fieldErrors = List.of(new FieldValidationError(
                "type",
                "validation.citizen_type.allowed"
        ));
    }

    public List<FieldValidationError> fieldErrors() {
        return fieldErrors;
    }
}
