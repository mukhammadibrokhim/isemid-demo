package uz.uzinfocom.app.shared.exception;

public class NotFoundException extends AppException {
    public NotFoundException() {
        super(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getDefaultMessageCode());
    }

    public NotFoundException(String messageCode, Object... args) {
        super(ErrorCode.NOT_FOUND, messageCode, args);
    }
}
