package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class Api2BadGatewayException extends Api2Exception {

    public Api2BadGatewayException(String operation, Api2UpstreamError upstreamError) {
        super(
                HttpStatus.BAD_GATEWAY,
                "API2_BAD_GATEWAY",
                "api2.error.bad_gateway",
                operation,
                upstreamError
        );
    }
}
