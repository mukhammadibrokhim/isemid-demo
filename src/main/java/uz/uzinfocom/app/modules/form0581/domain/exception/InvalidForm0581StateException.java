package uz.uzinfocom.app.modules.form0581.domain.exception;

import uz.uzinfocom.app.shared.exception.ConflictException;

public class InvalidForm0581StateException extends ConflictException {

    public InvalidForm0581StateException(String messageCode, Object... args) {
        super(messageCode, args);
    }
}
