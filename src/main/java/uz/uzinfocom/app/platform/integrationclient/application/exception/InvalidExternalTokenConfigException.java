package uz.uzinfocom.app.platform.integrationclient.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class InvalidExternalTokenConfigException extends AppException {
    public InvalidExternalTokenConfigException(String messageCode, Object... args) {
        super(ErrorCode.VALIDATION_FAILED, messageCode, args);
    }
}
