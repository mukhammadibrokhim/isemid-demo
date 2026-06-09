package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class Api2AuthenticationException extends Api2Exception {

    public Api2AuthenticationException(String operation, Api2UpstreamError upstreamError) {
        super(
                HttpStatus.BAD_GATEWAY,
                "API2_UPSTREAM_UNAUTHORIZED",
                "api2.error.upstream_unauthorized",
                operation,
                upstreamError
        );
    }
}
