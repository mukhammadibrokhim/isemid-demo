package uz.uzinfocom.app.modules.card.application.exception;

import uz.uzinfocom.app.shared.exception.ScopeViolationException;

public class CardScopeViolationException extends ScopeViolationException {

    public CardScopeViolationException() {
        super("error.card.scope-violation");
    }
}
