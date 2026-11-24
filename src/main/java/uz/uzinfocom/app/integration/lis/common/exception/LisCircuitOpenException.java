package uz.uzinfocom.app.integration.lis.common.exception;

import lombok.Getter;
import uz.uzinfocom.app.shared.exception.ErrorCode;
import uz.uzinfocom.app.shared.exception.RetryAfterAware;

/**
 * The "lis" circuit breaker is open - the call was rejected locally without
 * ever reaching LIS. Kept apart from {@link LisUnavailableException} so the
 * client gets its own error code and a {@code Retry-After} to back off on.
 */
@Getter
public class LisCircuitOpenException extends LisException implements RetryAfterAware {

    private final long retryAfterSeconds;

    public LisCircuitOpenException(String operation, long retryAfterSeconds) {
        super(ErrorCode.UPSTREAM_CIRCUIT_OPEN, "lis.error.circuit_open", operation, null, "Circuit breaker open");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
