package uz.uzinfocom.app.integration.api2.citizen.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class CitizenDataNotFoundException extends Api2Exception {

    public CitizenDataNotFoundException(String operation, Api2UpstreamError upstreamError) {
        super(
                HttpStatus.NOT_FOUND,
                "CITIZEN_NOT_FOUND",
                "api2.citizen.error.not_found",
                operation,
                upstreamError
        );
    }
}
