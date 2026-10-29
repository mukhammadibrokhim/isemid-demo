package uz.uzinfocom.app.modules.form129.domain.exception;

import uz.uzinfocom.app.shared.exception.ConflictException;

public class InvalidForm129StateException extends ConflictException {

    public InvalidForm129StateException(String messageCode, Object... args) {
        super(messageCode, args);
    }
}
