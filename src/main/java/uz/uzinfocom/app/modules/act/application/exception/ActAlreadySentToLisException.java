package uz.uzinfocom.app.modules.act.application.exception;

import uz.uzinfocom.app.shared.exception.ConflictException;

public class ActAlreadySentToLisException extends ConflictException {

    public ActAlreadySentToLisException(String messageCode, Object... args) {
        super(messageCode, args);
    }
}
