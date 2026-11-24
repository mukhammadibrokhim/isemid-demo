package uz.uzinfocom.app.integration.dhp.common.exception;

import lombok.Getter;
import uz.uzinfocom.app.shared.exception.ErrorCode;
import uz.uzinfocom.app.shared.exception.RetryAfterAware;

/**
 * The "dhp" circuit breaker is open - the call was rejected locally without
 * ever reaching DHP. Carries the breaker's wait duration so the client gets
 * a {@code Retry-After} to back off on.
 */
@Getter
public class DhpCircuitOpenException extends DhpIntegrationException implements RetryAfterAware {

    private final long retryAfterSeconds;

    public DhpCircuitOpenException(String operation, long retryAfterSeconds, Throwable cause) {
        super(ErrorCode.UPSTREAM_CIRCUIT_OPEN, "dhp.error.circuit_open", operation);
        this.retryAfterSeconds = retryAfterSeconds;
        initCause(cause);
    }
}
