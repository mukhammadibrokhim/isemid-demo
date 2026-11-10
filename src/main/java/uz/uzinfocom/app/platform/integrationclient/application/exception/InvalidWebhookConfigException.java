package uz.uzinfocom.app.platform.integrationclient.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * Thrown by {@code IntegrationClientCommandService#updateWebhook} for any of
 * its {@code active=true} validation branches (non-HTTPS/missing callback
 * URL, missing HTTP method, missing auth sub-field for the chosen
 * {@code authType}) - unlike {@link InvalidAllowedIpException}, which always
 * carries the same fixed message with one offending value, this carries
 * whichever message code matches the specific branch that failed.
 */
public class InvalidWebhookConfigException extends AppException {
    public InvalidWebhookConfigException(String messageCode, Object... args) {
        super(ErrorCode.VALIDATION_FAILED, messageCode, args);
    }
}
