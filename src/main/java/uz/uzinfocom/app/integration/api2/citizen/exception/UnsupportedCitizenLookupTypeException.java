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
                "Unsupported citizen lookup type.",
                "CITIZEN_LOOKUP",
                null
        );
        this.fieldErrors = List.of(new FieldValidationError(
                "type",
                "type must be one of NNUZB, PPN, CZ."
        ));
    }

    public List<FieldValidationError> fieldErrors() {
        return fieldErrors;
    }
}
