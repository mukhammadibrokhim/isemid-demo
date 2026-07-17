package uz.uzinfocom.app.modules.act.application.exception;

import uz.uzinfocom.app.shared.exception.ConflictException;

public class InvalidActStatusException extends ConflictException {

    public InvalidActStatusException(String messageCode, Object... args) {
        super(messageCode, args);
    }
}
