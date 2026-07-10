package uz.uzinfocom.app.modules.form0581.application.exception;

import uz.uzinfocom.app.shared.exception.NotFoundException;

public class Form0581NotFoundException extends NotFoundException {

    public Form0581NotFoundException(Object id) {
        super("error.form0581.not-found", id);
    }
}
