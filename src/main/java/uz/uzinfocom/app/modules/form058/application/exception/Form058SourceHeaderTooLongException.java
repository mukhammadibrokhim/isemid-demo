package uz.uzinfocom.app.modules.form058.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class Form058SourceHeaderTooLongException extends AppException {

    public Form058SourceHeaderTooLongException(int maxLength) {
        super(ErrorCode.BAD_REQUEST, "error.form058.source.header.too_long", maxLength);
    }
}