package uz.uzinfocom.app.modules.form058.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class InvalidForm058SourceHeaderException extends AppException {

    public InvalidForm058SourceHeaderException(String source, String allowedSources) {
        super(ErrorCode.BAD_REQUEST, "error.form058.source.header.invalid", source, allowedSources);
    }
}