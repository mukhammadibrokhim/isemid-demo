package uz.uzinfocom.app.modules.form129.application.exception;

import uz.uzinfocom.app.shared.exception.NotFoundException;

public class Form129NotFoundException extends NotFoundException {

    public Form129NotFoundException(Object id) {
        super("error.form129.not-found", id);
    }
}
