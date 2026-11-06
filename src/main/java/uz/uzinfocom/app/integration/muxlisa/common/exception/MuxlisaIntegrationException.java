package uz.uzinfocom.app.integration.muxlisa.common.exception;

import org.springframework.web.client.ResourceAccessException;
import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.net.SocketTimeoutException;

/**
 * A Muxlisa call failed for a reason that says something about Muxlisa (or our
 * configuration for it) rather than about the caller's audio - these count
 * against the {@code muxlisa} circuit breaker. {@link MuxlisaRequestRejectedException}
 * is listed under {@code resilience4j.circuitbreaker.configs.muxlisa.ignore-exceptions}.
 */
public class MuxlisaIntegrationException extends AppException {

    protected MuxlisaIntegrationException(ErrorCode errorCode, String messageCode) {
        super(errorCode, messageCode);
    }

    /** The API key is not configured or Muxlisa refused it - our setup problem, not the caller's. */
    public static MuxlisaIntegrationException notConfigured() {
        return new MuxlisaIntegrationException(ErrorCode.INTERNAL_ERROR, "muxlisa.error.not_configured");
    }

    /** Switched off at runtime ({@code integration.muxlisa.enabled}); nothing is sent to Muxlisa. */
    public static MuxlisaIntegrationException disabled() {
        return new MuxlisaIntegrationException(ErrorCode.UPSTREAM_CIRCUIT_OPEN, "muxlisa.error.disabled");
    }

    /** Muxlisa answered 402: the account behind our API key has no balance left. */
    public static MuxlisaIntegrationException insufficientBalance() {
        return new MuxlisaIntegrationException(ErrorCode.UPSTREAM_ERROR, "muxlisa.error.insufficient_balance");
    }

    /** Too many STT calls in flight at once - rejected locally, before the circuit breaker. */
    public static MuxlisaIntegrationException busy() {
        return new MuxlisaIntegrationException(ErrorCode.TOO_MANY_REQUESTS, "muxlisa.error.busy");
    }

    /** Transport failure (connection refused, DNS, read timeout, ...). */
    public static MuxlisaIntegrationException unavailable(Throwable cause) {
        boolean timeout = cause instanceof ResourceAccessException access
                && access.getCause() instanceof SocketTimeoutException;
        MuxlisaIntegrationException exception = timeout
                ? new MuxlisaIntegrationException(ErrorCode.UPSTREAM_TIMEOUT, "muxlisa.error.timeout")
                : new MuxlisaIntegrationException(ErrorCode.UPSTREAM_ERROR, "muxlisa.error.unavailable");
        exception.initCause(cause);
        return exception;
    }

    /** Muxlisa answered 5xx / 429 / anything else we can't act on. */
    public static MuxlisaIntegrationException upstream() {
        return new MuxlisaIntegrationException(ErrorCode.UPSTREAM_ERROR, "muxlisa.error.unavailable");
    }

    /** Muxlisa answered 2xx but the body held no recognizable transcript. */
    public static MuxlisaIntegrationException malformed() {
        return new MuxlisaIntegrationException(ErrorCode.UPSTREAM_ERROR, "muxlisa.error.malformed_response");
    }
}
