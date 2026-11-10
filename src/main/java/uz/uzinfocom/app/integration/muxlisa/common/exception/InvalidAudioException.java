package uz.uzinfocom.app.integration.muxlisa.common.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/** The caller's recording is empty, too large or in an unsupported format - rejected before any call to Muxlisa. */
public class InvalidAudioException extends AppException {

    public InvalidAudioException(String messageCode, Object... args) {
        super(ErrorCode.VALIDATION_FAILED, messageCode, args);
    }
}
