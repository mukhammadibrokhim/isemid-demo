package uz.uzinfocom.app.integration.aircontrol.common.exception;

import org.springframework.web.client.ResourceAccessException;
import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.net.SocketTimeoutException;

/**
 * An AIRCONTROL call failed for a reason that says something about AIRCONTROL
 * (or our configuration for it); every such failure counts against the
 * {@code aircontrol} circuit breaker. A 404 for an unknown station is not an
 * exception - the client answers it with an empty result.
 */
public class AirControlIntegrationException extends AppException {

    protected AirControlIntegrationException(ErrorCode errorCode, String messageCode) {
        super(errorCode, messageCode);
    }

    /** Login/password not configured or refused (401/403) - our setup problem, not the caller's. */
    public static AirControlIntegrationException notConfigured() {
        return new AirControlIntegrationException(ErrorCode.INTERNAL_ERROR, "aircontrol.error.not_configured");
    }

    /** Switched off at runtime ({@code integration.aircontrol.enabled}); nothing is sent. */
    public static AirControlIntegrationException disabled() {
        return new AirControlIntegrationException(ErrorCode.UPSTREAM_CIRCUIT_OPEN, "aircontrol.error.disabled");
    }

    /** Transport failure (connection refused, DNS, read timeout, ...). */
    public static AirControlIntegrationException unavailable(Throwable cause) {
        boolean timeout = cause instanceof ResourceAccessException access
                && access.getCause() instanceof SocketTimeoutException;
        AirControlIntegrationException exception = timeout
                ? new AirControlIntegrationException(ErrorCode.UPSTREAM_TIMEOUT, "aircontrol.error.timeout")
                : new AirControlIntegrationException(ErrorCode.UPSTREAM_ERROR, "aircontrol.error.unavailable");
        exception.initCause(cause);
        return exception;
    }

    /** 5xx / 429 / anything else we can't act on. */
    public static AirControlIntegrationException upstream() {
        return new AirControlIntegrationException(ErrorCode.UPSTREAM_ERROR, "aircontrol.error.unavailable");
    }

    /** 2xx but the body is not the documented JSON shape. */
    public static AirControlIntegrationException malformed() {
        return new AirControlIntegrationException(ErrorCode.UPSTREAM_ERROR, "aircontrol.error.malformed_response");
    }
}
