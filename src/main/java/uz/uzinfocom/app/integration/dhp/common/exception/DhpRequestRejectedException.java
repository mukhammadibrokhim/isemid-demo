package uz.uzinfocom.app.integration.dhp.common.exception;

import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * DHP rejected the request itself (400/422, e.g. {@code "incorrect NI"}).
 * Caller-side, so it is ignored by the {@code dhp} circuit breaker.
 */
public class DhpRequestRejectedException extends DhpIntegrationException {

    public DhpRequestRejectedException(String operation) {
        super(ErrorCode.BAD_REQUEST, "dhp.error.rejected", operation);
    }
}
