package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class Api2BadRequestException extends Api2Exception {

    public Api2BadRequestException(String operation, Api2UpstreamError upstreamError) {
        super(
                HttpStatus.BAD_REQUEST,
                "API2_UPSTREAM_BAD_REQUEST",
                "API2 upstream rejected the request.",
                operation,
                upstreamError
        );
    }
}
