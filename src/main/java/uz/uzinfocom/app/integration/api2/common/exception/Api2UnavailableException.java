package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class Api2UnavailableException extends Api2Exception {

    public Api2UnavailableException(String operation, Api2UpstreamError upstreamError) {
        super(
                HttpStatus.SERVICE_UNAVAILABLE,
                "API2_UPSTREAM_UNAVAILABLE",
                "API2 upstream service is unavailable.",
                operation,
                upstreamError
        );
    }

    public Api2UnavailableException(String operation, Throwable cause) {
        super(
                HttpStatus.SERVICE_UNAVAILABLE,
                "API2_UPSTREAM_UNAVAILABLE",
                "API2 upstream service is unavailable.",
                operation,
                null,
                cause
        );
    }
}
