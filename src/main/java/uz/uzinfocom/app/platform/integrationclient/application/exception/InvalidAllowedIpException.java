package uz.uzinfocom.app.platform.integrationclient.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

public class InvalidAllowedIpException extends AppException {
    public InvalidAllowedIpException(String invalidEntry) {
        super(ErrorCode.VALIDATION_FAILED, "integration-client.allowed-ips.invalid", invalidEntry);
    }
}
