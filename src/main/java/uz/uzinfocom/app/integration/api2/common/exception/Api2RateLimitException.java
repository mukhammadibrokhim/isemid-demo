package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class Api2RateLimitException extends Api2Exception {

    public Api2RateLimitException(String operation, Api2UpstreamError upstreamError) {
        super(
                HttpStatus.TOO_MANY_REQUESTS,
                "API2_UPSTREAM_RATE_LIMITED",
                "API2 upstream rate limit was reached.",
                operation,
                upstreamError
        );
    }
}
