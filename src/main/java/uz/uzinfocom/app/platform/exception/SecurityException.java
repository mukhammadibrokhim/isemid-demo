package uz.uzinfocom.app.platform.exception;

public class SecurityException extends BaseException {
    public SecurityException(String messageCode, Object... args) {
        super(ErrorCode.SECURITY_VIOLATION, messageCode, args);
    }
}
