package uz.uzinfocom.app.modules.form058.domain.exception;

import uz.uzinfocom.app.shared.exception.ConflictException;

public class InvalidForm058StateException extends ConflictException {

    public InvalidForm058StateException(String messageCode, Object... args) {
        super(messageCode, args);
    }
}
