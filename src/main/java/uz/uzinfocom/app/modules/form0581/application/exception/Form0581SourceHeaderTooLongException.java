package uz.uzinfocom.app.modules.form0581.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class Form0581SourceHeaderTooLongException extends AppException {

    public Form0581SourceHeaderTooLongException(int maxLength) {
        super(ErrorCode.BAD_REQUEST, "error.form0581.source.header.too_long", maxLength);
    }
}
