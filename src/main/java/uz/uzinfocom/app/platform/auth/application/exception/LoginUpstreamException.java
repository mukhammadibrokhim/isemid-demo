package uz.uzinfocom.app.platform.auth.application.exception;

import org.springframework.web.client.ResourceAccessException;
import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.net.SocketTimeoutException;

/**
 * The provider's token endpoint failed for a reason that isn't the caller's
 * fault: network failure, timeout, or a 5xx/unexpected non-2xx response.
 */
public class LoginUpstreamException extends AppException {

    public LoginUpstreamException(String providerKey, Throwable cause) {
        super(resolveErrorCode(cause), "auth.login.upstream-error", providerKey);
        initCause(cause);
    }

    public LoginUpstreamException(String providerKey, int upstreamStatus) {
        super(ErrorCode.UPSTREAM_ERROR, "auth.login.upstream-error", providerKey);
    }

    private static ErrorCode resolveErrorCode(Throwable cause) {
        if (cause instanceof ResourceAccessException resourceAccessException
                && resourceAccessException.getCause() instanceof SocketTimeoutException) {
            return ErrorCode.UPSTREAM_TIMEOUT;
        }

        return ErrorCode.UPSTREAM_ERROR;
    }
}
