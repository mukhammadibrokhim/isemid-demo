package uz.uzinfocom.app.modules.card.application.exception;

import uz.uzinfocom.app.shared.exception.ConflictException;

public class InvalidCardStatusException extends ConflictException {

    public InvalidCardStatusException(String messageCode, Object... args) {
        super(messageCode, args);
    }
}
