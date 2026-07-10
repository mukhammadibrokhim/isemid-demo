package uz.uzinfocom.app.modules.form0581.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class InvalidForm0581SourceHeaderException extends AppException {

    public InvalidForm0581SourceHeaderException(String source, String allowedSources) {
        super(ErrorCode.BAD_REQUEST, "error.form0581.source.header.invalid", source, allowedSources);
    }
}
