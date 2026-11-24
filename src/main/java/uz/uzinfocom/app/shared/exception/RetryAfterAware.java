package uz.uzinfocom.app.shared.exception;

/**
 * Marks a failure the caller can simply wait out - e.g. an outbound circuit
 * breaker that is open. Exception handlers turn it into a {@code Retry-After}
 * header plus a {@code retryAfterSeconds} body field, so a client can back
 * off instead of hammering an endpoint that is guaranteed to fail.
 */
public interface RetryAfterAware {

    /** Upper bound, in seconds, after which a retry may succeed. */
    long getRetryAfterSeconds();
}
