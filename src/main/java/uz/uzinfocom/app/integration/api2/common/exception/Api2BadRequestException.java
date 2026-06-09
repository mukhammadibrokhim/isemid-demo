package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.api.dto.FieldValidationError;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

import java.util.List;

public class Api2BadRequestException extends Api2Exception {

    public Api2BadRequestException(String operation, Api2UpstreamError upstreamError) {
        this(operation, upstreamError, List.of());
    }

    public Api2BadRequestException(
            String operation,
            Api2UpstreamError upstreamError,
            List<FieldValidationError> fieldErrors
    ) {
        super(
                HttpStatus.BAD_REQUEST,
                "API2_UPSTREAM_BAD_REQUEST",
                "api2.error.bad_request",
                operation,
                upstreamError,
                fieldErrors
        );
    }
}
