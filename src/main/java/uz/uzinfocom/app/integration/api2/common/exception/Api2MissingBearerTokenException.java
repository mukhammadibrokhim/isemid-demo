package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;

public class Api2MissingBearerTokenException extends Api2Exception {

    public Api2MissingBearerTokenException() {
        super(
                HttpStatus.UNAUTHORIZED,
                "API2_BEARER_TOKEN_MISSING",
                "Authenticated Bearer token is required for API2 integration.",
                "API2_BEARER_TOKEN_FORWARDING",
                null
        );
    }
}
