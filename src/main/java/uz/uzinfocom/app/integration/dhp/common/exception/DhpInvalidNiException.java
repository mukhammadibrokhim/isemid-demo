package uz.uzinfocom.app.integration.dhp.common.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/** The caller's {@code ni} is missing or not a 14-digit NNUZB - rejected before any call to DHP. */
public class DhpInvalidNiException extends AppException {

    public DhpInvalidNiException() {
        super(ErrorCode.VALIDATION_FAILED, "validation.nnuzb.format");
    }
}
