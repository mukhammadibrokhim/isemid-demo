package uz.uzinfocom.app.integration.muxlisa.common.exception;

import lombok.Getter;
import uz.uzinfocom.app.shared.exception.ErrorCode;
import uz.uzinfocom.app.shared.exception.RetryAfterAware;

/** The "muxlisa" circuit breaker is open - rejected locally; carries the breaker's wait as {@code Retry-After}. */
@Getter
public class MuxlisaCircuitOpenException extends MuxlisaIntegrationException implements RetryAfterAware {

    private final long retryAfterSeconds;

    public MuxlisaCircuitOpenException(long retryAfterSeconds, Throwable cause) {
        super(ErrorCode.UPSTREAM_CIRCUIT_OPEN, "muxlisa.error.circuit_open");
        this.retryAfterSeconds = retryAfterSeconds;
        initCause(cause);
    }
}
