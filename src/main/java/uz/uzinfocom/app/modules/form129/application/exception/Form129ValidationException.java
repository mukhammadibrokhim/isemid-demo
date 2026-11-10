package uz.uzinfocom.app.modules.form129.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class Form129ValidationException extends AppException {

    public Form129ValidationException(String messageCode, Object... args) {
        super(ErrorCode.VALIDATION_FAILED, messageCode, args);
    }
}
