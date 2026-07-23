package uz.uzinfocom.app.integration.lis.common.exception;

import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * LIS rejected the payload we sent (4xx other than auth) — the act's data is
 * wrong or incomplete for LIS's rules, so retrying unchanged will fail again.
 */
public class LisBadRequestException extends LisException {

    public LisBadRequestException(String operation, Integer upstreamStatus, String upstreamMessage) {
        super(ErrorCode.UPSTREAM_ERROR, "lis.error.bad_request", operation, upstreamStatus, upstreamMessage);
    }
}
