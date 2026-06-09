package uz.uzinfocom.app.integration.api2.legalentity.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class LegalEntityNotFoundException extends Api2Exception {

    public LegalEntityNotFoundException(String operation, Api2UpstreamError upstreamError) {
        super(
                HttpStatus.NOT_FOUND,
                "LEGAL_ENTITY_NOT_FOUND",
                "api2.legal_entity.error.not_found",
                operation,
                upstreamError
        );
    }
}
