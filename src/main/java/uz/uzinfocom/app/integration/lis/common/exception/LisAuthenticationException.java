package uz.uzinfocom.app.integration.lis.common.exception;

import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * LIS refused our credentials (401/403) — a misconfigured API key or an
 * unauthorized organization/user token. Deliberately carries no upstream
 * message: auth failures are the most likely to leak secrets in their text.
 */
public class LisAuthenticationException extends LisException {

    public LisAuthenticationException(String operation, Integer upstreamStatus) {
        super(ErrorCode.UPSTREAM_ERROR, "lis.error.upstream_unauthorized", operation, upstreamStatus, null);
    }
}
