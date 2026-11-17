package uz.uzinfocom.app.modules.form129.application.exception;

import uz.uzinfocom.app.shared.exception.ScopeViolationException;

public class Form129ScopeViolationException extends ScopeViolationException {

    public Form129ScopeViolationException() {
        super("error.form129.scope-violation");
    }
}
