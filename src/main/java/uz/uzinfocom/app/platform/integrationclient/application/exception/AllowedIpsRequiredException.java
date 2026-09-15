package uz.uzinfocom.app.platform.integrationclient.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class AllowedIpsRequiredException extends AppException {
    public AllowedIpsRequiredException() {
        super(ErrorCode.VALIDATION_FAILED, "integration-client.allowed-ips.required-for-ip-allowlist");
    }
}
