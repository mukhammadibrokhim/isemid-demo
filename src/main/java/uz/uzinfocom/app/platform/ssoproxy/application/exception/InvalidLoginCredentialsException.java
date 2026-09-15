package uz.uzinfocom.app.platform.ssoproxy.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * Thrown for any login failure the provider reports as the caller's fault
 * (e.g. an expired or already-used authorization code). Deliberately one
 * generic message regardless of which upstream detail caused it - same
 * reasoning as {@code InvalidIntegrationCredentialsException}.
 */
public class InvalidLoginCredentialsException extends AppException {

    public InvalidLoginCredentialsException(String providerKey) {
        super(ErrorCode.UNAUTHORIZED, "auth.login.invalid-credentials", providerKey);
    }
}
