package uz.uzinfocom.app.platform.audit.application.exception;

import uz.uzinfocom.app.shared.exception.NotFoundException;

public class AuditEventNotFoundException extends NotFoundException {

    public AuditEventNotFoundException(Object id) {
        super("error.audit-event.not-found", id);
    }
}
