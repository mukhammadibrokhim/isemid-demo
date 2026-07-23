package uz.uzinfocom.app.modules.act.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class UnsupportedActTypeException extends AppException {

    public UnsupportedActTypeException(Object actType) {
        super(ErrorCode.VALIDATION_FAILED, "error.act.unsupported-type", actType);
    }
}
