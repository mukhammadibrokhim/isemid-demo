package uz.uzinfocom.app.modules.card.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class UnsupportedCardTypeException extends AppException {

    public UnsupportedCardTypeException(Object cardType) {
        super(ErrorCode.VALIDATION_FAILED, "error.card.unsupported-type", cardType);
    }
}
