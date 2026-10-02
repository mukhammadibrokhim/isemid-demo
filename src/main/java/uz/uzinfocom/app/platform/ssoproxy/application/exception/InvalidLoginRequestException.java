package uz.uzinfocom.app.platform.ssoproxy.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * A field the resolved provider's grant requires (e.g. {@code username}/
 * {@code password} for password grant, {@code code}/{@code codeVerifier}/
 * {@code redirectUri} for authorization_code) was missing from {@code
 * LoginRequest}. Not a {@code jakarta.validation} failure because which
 * fields are required depends on which provider was resolved from the
 * {@code {provider}} path variable, not on the DTO shape alone.
 */
public class InvalidLoginRequestException extends AppException {

    public InvalidLoginRequestException(String messageCode) {
        super(ErrorCode.VALIDATION_FAILED, messageCode);
    }
}
