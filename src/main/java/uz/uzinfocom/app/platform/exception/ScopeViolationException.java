package uz.uzinfocom.app.platform.exception;

public class ScopeViolationException extends BaseException {
    public ScopeViolationException(String messageCode, Object... args) {
        super(ErrorCode.SCOPE_VIOLATION, messageCode, args);
    }
}
