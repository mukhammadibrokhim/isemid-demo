package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class Api2AuthenticationException extends Api2Exception {

    public Api2AuthenticationException(String operation, Api2UpstreamError upstreamError) {
        super(
                HttpStatus.UNAUTHORIZED,
                "API2_UPSTREAM_UNAUTHORIZED",
                "API2 upstream rejected authentication.",
                operation,
                upstreamError
        );
    }
}
