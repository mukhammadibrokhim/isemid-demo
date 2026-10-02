package uz.uzinfocom.app.integration.dhp.common.exception;

import org.springframework.web.client.ResourceAccessException;
import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.net.SocketTimeoutException;

/**
 * A DHP call failed for a reason that says something about DHP (or our
 * configuration for it) rather than about the caller's request - these count
 * against the {@code dhp} circuit breaker. The caller-side subclasses
 * ({@link DhpRequestRejectedException}, {@link DhpAccessDeniedException}) are
 * listed under {@code resilience4j.circuitbreaker.configs.dhp.ignore-exceptions}
 * instead.
 */
public class DhpIntegrationException extends AppException {

    private final String operation;

    protected DhpIntegrationException(ErrorCode errorCode, String messageCode, String operation) {
        super(errorCode, messageCode);
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    /** The M2M client id/secret are not configured - our own setup problem, not DHP's. */
    public static DhpIntegrationException notConfigured(String operation) {
        return new DhpIntegrationException(ErrorCode.INTERNAL_ERROR, "dhp.error.not_configured", operation);
    }

    /** Transport failure (connection refused, DNS, read timeout, ...) or the breaker rejecting the call. */
    public static DhpIntegrationException unavailable(String operation, Throwable cause) {
        boolean timeout = cause instanceof ResourceAccessException access
                && access.getCause() instanceof SocketTimeoutException;
        DhpIntegrationException exception = timeout
                ? new DhpIntegrationException(ErrorCode.UPSTREAM_TIMEOUT, "dhp.error.timeout", operation)
                : new DhpIntegrationException(ErrorCode.UPSTREAM_ERROR, "dhp.error.unavailable", operation);
        exception.initCause(cause);
        return exception;
    }

    /** DHP answered 5xx / 429 / anything else we can't act on. */
    public static DhpIntegrationException upstream(String operation) {
        return new DhpIntegrationException(ErrorCode.UPSTREAM_ERROR, "dhp.error.unavailable", operation);
    }

    /** DHP answered 2xx but the body was not the JSON we expected. */
    public static DhpIntegrationException malformed(String operation, Throwable cause) {
        DhpIntegrationException exception =
                new DhpIntegrationException(ErrorCode.UPSTREAM_ERROR, "dhp.error.malformed_response", operation);
        if (cause != null) {
            exception.initCause(cause);
        }
        return exception;
    }
}
