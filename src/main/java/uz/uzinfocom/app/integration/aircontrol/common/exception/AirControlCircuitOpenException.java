package uz.uzinfocom.app.integration.aircontrol.common.exception;

import lombok.Getter;
import uz.uzinfocom.app.shared.exception.ErrorCode;
import uz.uzinfocom.app.shared.exception.RetryAfterAware;

/** The "air-control" circuit breaker is open - rejected locally; carries the breaker's wait as {@code Retry-After}. */
@Getter
public class AirControlCircuitOpenException extends AirControlIntegrationException implements RetryAfterAware {

    private final long retryAfterSeconds;

    public AirControlCircuitOpenException(long retryAfterSeconds, Throwable cause) {
        super(ErrorCode.UPSTREAM_CIRCUIT_OPEN, "air-control.error.circuit_open");
        this.retryAfterSeconds = retryAfterSeconds;
        initCause(cause);
    }
}
