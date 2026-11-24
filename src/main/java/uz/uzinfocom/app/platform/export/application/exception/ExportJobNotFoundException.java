package uz.uzinfocom.app.platform.export.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class ExportJobNotFoundException extends AppException {

    public ExportJobNotFoundException(Long id) {
        super(ErrorCode.NOT_FOUND, "error.export.job.not-found", id);
    }
}
