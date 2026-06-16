package uz.uzinfocom.app.modules.form058.application.exception;

import uz.uzinfocom.app.shared.exception.NotFoundException;

public class Form058NotFoundException extends NotFoundException {

    public Form058NotFoundException(Object id) {
        super("error.form058.not-found", id);
    }
}
