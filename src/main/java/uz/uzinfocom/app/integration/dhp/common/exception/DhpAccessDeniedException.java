package uz.uzinfocom.app.integration.dhp.common.exception;

import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * DHP refused our own M2M credentials or scope (401 that survived a token
 * refresh, or 403). Reported as an upstream error rather than FORBIDDEN -
 * it is our client that lacks access, not the caller. Ignored by the
 * {@code dhp} circuit breaker: DHP itself is healthy.
 */
public class DhpAccessDeniedException extends DhpIntegrationException {

    public DhpAccessDeniedException(String operation) {
        super(ErrorCode.UPSTREAM_ERROR, "dhp.error.access_denied", operation);
    }
}
