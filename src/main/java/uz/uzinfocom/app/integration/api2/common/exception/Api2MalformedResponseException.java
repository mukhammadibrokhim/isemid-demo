package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;

public class Api2MalformedResponseException extends Api2Exception {

    public Api2MalformedResponseException(String operation, Throwable cause) {
        super(
                HttpStatus.BAD_GATEWAY,
                "API2_MALFORMED_RESPONSE",
                "API2 upstream returned a malformed response.",
                operation,
                null,
                cause
        );
    }
}
