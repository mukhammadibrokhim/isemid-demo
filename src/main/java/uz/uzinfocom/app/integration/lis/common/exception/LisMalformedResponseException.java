package uz.uzinfocom.app.integration.lis.common.exception;

import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * LIS answered successfully but the body wasn't what the contract promises —
 * e.g. a create-act call that returned no act id. Treated as a failure
 * because we have nothing to correlate the eventual callback against.
 */
public class LisMalformedResponseException extends LisException {

    public LisMalformedResponseException(String operation, Integer upstreamStatus, String detail) {
        super(ErrorCode.UPSTREAM_ERROR, "lis.error.malformed_response", operation, upstreamStatus, detail);
    }
}
