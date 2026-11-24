package uz.uzinfocom.app.platform.export.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class ExportLimitExceededException extends AppException {

    public ExportLimitExceededException(int maxActiveJobs) {
        super(ErrorCode.TOO_MANY_REQUESTS, "error.export.too-many-active", maxActiveJobs);
    }
}
