package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class Api2ConflictException extends Api2Exception {

    public Api2ConflictException(String operation, Api2UpstreamError upstreamError) {
        super(
                HttpStatus.CONFLICT,
                "API2_CONFLICT",
                "api2.error.conflict",
                operation,
                upstreamError
        );
    }
}
