package uz.uzinfocom.app.shared.exception;

public class SecurityException extends AppException {
    public SecurityException(String messageCode, Object... args) {
        super(ErrorCode.SECURITY_VIOLATION, messageCode, args);
    }
}
