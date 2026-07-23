package uz.uzinfocom.app.integration.lis.common.exception;

import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * LIS did not answer in time. Unlike the other failures this one is
 * genuinely ambiguous — LIS may still have accepted the act — which is why
 * the act is left re-sendable with {@code force} available to override
 * LIS's duplicate check.
 */
public class LisTimeoutException extends LisException {

    public LisTimeoutException(String operation, Integer upstreamStatus) {
        super(ErrorCode.UPSTREAM_TIMEOUT, "lis.error.timeout", operation, upstreamStatus, null);
    }
}
