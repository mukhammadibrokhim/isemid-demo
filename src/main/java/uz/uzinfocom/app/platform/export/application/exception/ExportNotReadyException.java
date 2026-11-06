package uz.uzinfocom.app.platform.export.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class ExportNotReadyException extends AppException {

    public ExportNotReadyException(Long id) {
        super(ErrorCode.CONFLICT, "error.export.not-ready", id);
    }
}
