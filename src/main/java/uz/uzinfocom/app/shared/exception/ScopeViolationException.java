package uz.uzinfocom.app.shared.exception;

public class ScopeViolationException extends AppException {
    public ScopeViolationException(String messageCode, Object... args) {
        super(ErrorCode.SCOPE_VIOLATION, messageCode, args);
    }
}
