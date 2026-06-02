package uz.uzinfocom.app.platform.exception;

public class ValidationException extends BaseException {
    public ValidationException(String messageCode, Object... args) {
        super(ErrorCode.VALIDATION_FAILED, messageCode, args);
    }
}
