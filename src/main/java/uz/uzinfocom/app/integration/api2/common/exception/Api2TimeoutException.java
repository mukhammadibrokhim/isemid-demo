package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class Api2TimeoutException extends Api2Exception {

    public Api2TimeoutException(String operation, Api2UpstreamError upstreamError) {
        super(
                HttpStatus.GATEWAY_TIMEOUT,
                "API2_TIMEOUT",
                "api2.error.timeout",
                operation,
                upstreamError
        );
    }

    public Api2TimeoutException(String operation, Throwable cause) {
        super(
                HttpStatus.GATEWAY_TIMEOUT,
                "API2_TIMEOUT",
                "api2.error.timeout",
                operation,
                null,
                cause
        );
    }
}
