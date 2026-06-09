package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class Api2RateLimitException extends Api2Exception {

    public Api2RateLimitException(String operation, Api2UpstreamError upstreamError) {
        super(
                HttpStatus.SERVICE_UNAVAILABLE,
                "API2_RATE_LIMITED",
                "api2.error.rate_limited",
                operation,
                upstreamError
        );
    }
}
