package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;

public class Api2MissingBearerTokenException extends Api2Exception {

    public Api2MissingBearerTokenException() {
        super(
                HttpStatus.UNAUTHORIZED,
                "API2_BEARER_TOKEN_MISSING",
                "api2.error.bearer_token_missing",
                "API2_BEARER_TOKEN_FORWARDING",
                null
        );
    }
}
