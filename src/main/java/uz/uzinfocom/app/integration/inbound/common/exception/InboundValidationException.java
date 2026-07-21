package uz.uzinfocom.app.integration.inbound.common.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * Thrown by the additional, integration-only validators (e.g.
 * {@code InboundForm058Validator}, {@code InboundForm0581Validator}) —
 * stricter rules layered on top of, not replacing, each form's existing
 * {@code Form058CreateValidator}/{@code Form0581CreateValidator}.
 */
public class InboundValidationException extends AppException {

    public InboundValidationException(String messageCode, Object... args) {
        super(ErrorCode.VALIDATION_FAILED, messageCode, args);
    }
}
