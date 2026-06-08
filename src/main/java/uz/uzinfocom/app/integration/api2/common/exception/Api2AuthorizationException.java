package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class Api2AuthorizationException extends Api2Exception {

    public Api2AuthorizationException(String operation, Api2UpstreamError upstreamError) {
        super(
                HttpStatus.FORBIDDEN,
                "API2_UPSTREAM_FORBIDDEN",
                "API2 upstream denied access.",
                operation,
                upstreamError
        );
    }
}
