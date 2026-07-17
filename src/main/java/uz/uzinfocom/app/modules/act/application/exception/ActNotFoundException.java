package uz.uzinfocom.app.modules.act.application.exception;

import uz.uzinfocom.app.shared.exception.NotFoundException;

public class ActNotFoundException extends NotFoundException {

    public ActNotFoundException(Object id) {
        super("error.act.not-found", id);
    }
}
