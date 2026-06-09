package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class Api2AuthorizationException extends Api2Exception {

    public Api2AuthorizationException(String operation, Api2UpstreamError upstreamError) {
        super(
                HttpStatus.BAD_GATEWAY,
                "API2_UPSTREAM_FORBIDDEN",
                "api2.error.upstream_forbidden",
                operation,
                upstreamError
        );
    }
}
