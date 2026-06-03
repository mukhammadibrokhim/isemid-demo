package uz.uzinfocom.app.shared.exception;

public class ConflictException extends AppException {
    public ConflictException(String messageCode, Object... args) {
        super(ErrorCode.CONFLICT, messageCode, args);
    }
}
