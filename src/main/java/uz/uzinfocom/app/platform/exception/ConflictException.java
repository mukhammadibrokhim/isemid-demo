package uz.uzinfocom.app.platform.exception;

public class ConflictException extends BaseException {
    public ConflictException(String messageCode, Object... args) {
        super(ErrorCode.CONFLICT, messageCode, args);
    }
}
