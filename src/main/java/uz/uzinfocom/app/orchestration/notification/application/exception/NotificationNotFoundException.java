package uz.uzinfocom.app.orchestration.notification.application.exception;

import uz.uzinfocom.app.shared.exception.NotFoundException;

public class NotificationNotFoundException extends NotFoundException {

    public NotificationNotFoundException(Object id) {
        super("error.notification.not-found", id);
    }
}
