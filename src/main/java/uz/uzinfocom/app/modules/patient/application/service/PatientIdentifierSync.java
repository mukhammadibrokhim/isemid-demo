package uz.uzinfocom.app.modules.patient.application.service;

import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientIdentifierCommand;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;

/**
 * Shared find-or-create-by-type upsert used by every module that lets a
 * patient's identifiers be edited alongside its own form (Form058, Form0581).
 */
public final class PatientIdentifierSync {

    private PatientIdentifierSync() {
    }

    public static void upsert(Patient patient, CreatePatientIdentifierCommand command) {
        if (command == null || !StringUtils.hasText(command.type()) || !StringUtils.hasText(command.value())) {
            return;
        }

        PatientIdentifier identifier = patient.getIdentifiers().stream()
                .filter(item -> command.type().equals(item.getTypeCode()))
                .findFirst()
                .orElseGet(() -> {
                    PatientIdentifier newIdentifier = new PatientIdentifier();
                    newIdentifier.setTypeCode(command.type());
                    patient.addIdentifier(newIdentifier);
                    return newIdentifier;
                });
        identifier.setValue(command.value().trim());
        identifier.setPeriodStart(command.periodStart());
        identifier.setPeriodEnd(command.periodEnd());
    }
}
