package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class Api2MalformedResponseException extends Api2Exception {

    public Api2MalformedResponseException(String operation, Throwable cause) {
        this(operation, null, cause);
    }

    public Api2MalformedResponseException(String operation, Api2UpstreamError upstreamError) {
        this(operation, upstreamError, null);
    }

    public Api2MalformedResponseException(
            String operation,
            Api2UpstreamError upstreamError,
            Throwable cause
    ) {
        super(
                HttpStatus.BAD_GATEWAY,
                "API2_MALFORMED_RESPONSE",
                "api2.error.malformed_response",
                operation,
                upstreamError,
                cause
        );
    }
}
