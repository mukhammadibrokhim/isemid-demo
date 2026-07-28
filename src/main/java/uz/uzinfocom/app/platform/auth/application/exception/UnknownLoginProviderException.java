package uz.uzinfocom.app.platform.auth.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class UnknownLoginProviderException extends AppException {

    public UnknownLoginProviderException(String providerKey) {
        super(ErrorCode.NOT_FOUND, "auth.login.unknown-provider", providerKey);
    }
}
