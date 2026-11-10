package uz.uzinfocom.app.integration.api2.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.shared.exception.RetryAfterAware;

/**
 * The "api2" circuit breaker is open - the call was rejected locally without
 * ever reaching API2. Kept apart from {@link Api2UnavailableException} so the
 * client gets its own error code and a {@code Retry-After} to back off on.
 */
@Getter
public class Api2CircuitOpenException extends Api2Exception implements RetryAfterAware {

    private final long retryAfterSeconds;

    public Api2CircuitOpenException(String operation, long retryAfterSeconds, Throwable cause) {
        super(
                HttpStatus.SERVICE_UNAVAILABLE,
                "API2_CIRCUIT_OPEN",
                "api2.error.circuit_open",
                operation,
                null,
                cause
        );
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
