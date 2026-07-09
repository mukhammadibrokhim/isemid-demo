package uz.uzinfocom.app.modules.card.application.exception;

import uz.uzinfocom.app.shared.exception.NotFoundException;

public class CardNotFoundException extends NotFoundException {

    public CardNotFoundException(Object id) {
        super("error.card.not-found", id);
    }
}
