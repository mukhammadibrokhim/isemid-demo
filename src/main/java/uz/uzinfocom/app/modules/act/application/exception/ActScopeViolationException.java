package uz.uzinfocom.app.modules.act.application.exception;

import uz.uzinfocom.app.shared.exception.ScopeViolationException;

public class ActScopeViolationException extends ScopeViolationException {

    public ActScopeViolationException() {
        super("error.act.scope-violation");
    }
}
