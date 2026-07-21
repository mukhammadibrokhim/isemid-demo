package uz.uzinfocom.app.integration.inbound.oauth.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * Thrown for any client_credentials failure — unknown client_id, wrong
 * secret, or an inactive/revoked client. Deliberately a single exception
 * type with one generic message for all three cases: distinguishing "unknown
 * client" from "wrong secret" in the response would let a caller enumerate
 * valid client_ids.
 */
public class InvalidIntegrationCredentialsException extends AppException {

    public InvalidIntegrationCredentialsException() {
        super(ErrorCode.UNAUTHORIZED, "integration.token.invalid-credentials");
    }
}
