package uz.uzinfocom.app.modules.patient.application.exception;

import uz.uzinfocom.app.shared.exception.NotFoundException;

public class PatientIdentifierNotFoundException extends NotFoundException {

    public PatientIdentifierNotFoundException(Object id) {
        super("error.patient.identifier.not-found", id);
    }
}
