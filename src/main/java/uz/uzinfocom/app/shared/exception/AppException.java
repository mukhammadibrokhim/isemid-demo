package uz.uzinfocom.app.shared.exception;

import lombok.Getter;

@Getter
public abstract class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String messageCode;
    private final Object[] args;

    protected AppException(ErrorCode errorCode, String messageCode, Object... args) {
        super(messageCode);
        this.errorCode = errorCode;
        this.messageCode = messageCode == null ? errorCode.getDefaultMessageCode() : messageCode;
        this.args = args == null ? new Object[0] : args.clone();
    }
}
