package uz.uzinfocom.app.platform.iam.application.shared.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class OrganizationResolutionException extends AppException {

    public OrganizationResolutionException(String messageCode, Object... args) {
        super(ErrorCode.VALIDATION_FAILED, messageCode, args);
    }
}
