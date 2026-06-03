package uz.uzinfocom.app.shared.exception;

public class ValidationException extends AppException {
    public ValidationException(String messageCode, Object... args) {
        super(ErrorCode.VALIDATION_FAILED, messageCode, args);
    }
}
