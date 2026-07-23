package uz.uzinfocom.app.integration.lis.common.exception;

import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * Raised for the three act types LIS has no laboratory workflow for
 * (ACT155, ACT156, ACT224 — inspection acts, not sample-submission acts).
 * A caller error rather than an upstream one.
 */
public class LisUnsupportedActTypeException extends LisException {

    public LisUnsupportedActTypeException(Object actType) {
        super(ErrorCode.VALIDATION_FAILED, "lis.error.unsupported_act_type", "createAct", null, String.valueOf(actType));
    }
}
