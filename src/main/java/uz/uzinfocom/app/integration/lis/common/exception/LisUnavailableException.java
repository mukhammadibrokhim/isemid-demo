package uz.uzinfocom.app.integration.lis.common.exception;

import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * LIS could not be reached at all, or answered 5xx — a transport failure or
 * an upstream outage. Retrying later is the right response.
 */
public class LisUnavailableException extends LisException {

    public LisUnavailableException(String operation, Integer upstreamStatus, String upstreamMessage) {
        super(ErrorCode.UPSTREAM_ERROR, "lis.error.unavailable", operation, upstreamStatus, upstreamMessage);
    }
}
