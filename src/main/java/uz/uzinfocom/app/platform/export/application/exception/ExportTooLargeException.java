package uz.uzinfocom.app.platform.export.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class ExportTooLargeException extends AppException {

    public ExportTooLargeException(long matchedRows, long maxRows) {
        super(ErrorCode.VALIDATION_FAILED, "error.export.too-large", matchedRows, maxRows);
    }
}
