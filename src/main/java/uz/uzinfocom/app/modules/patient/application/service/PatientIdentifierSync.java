package uz.uzinfocom.app.modules.patient.application.service;

import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientIdentifierCommand;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;

/**
 * Shared upsert used by every module that lets a patient's identifiers be
 * edited alongside its own form (Form058, Form0581). Matches by {@code id}
 * when the caller supplied one (editing a specific existing document); a
 * missing id falls back to the legacy find-or-create-by-type behavior, which
 * is safe here because identifier types are one-per-patient by convention.
 */
public final class PatientIdentifierSync {

    private PatientIdentifierSync() {
    }

    public static void upsert(Patient patient, UpdatePatientIdentifierCommand command) {
        if (command == null || !StringUtils.hasText(command.type()) || !StringUtils.hasText(command.value())) {
            return;
        }

        PatientIdentifier identifier = command.id() != null
                ? findById(patient, command.id())
                : findOrCreateByType(patient, command.type());
        if (identifier == null) {
            return;
        }

        identifier.setTypeCode(command.type());
        identifier.setValue(command.value().trim());
        identifier.setPeriodStart(command.periodStart());
        identifier.setPeriodEnd(command.periodEnd());
    }

    private static PatientIdentifier findById(Patient patient, Long id) {
        return patient.getIdentifiers().stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }

    private static PatientIdentifier findOrCreateByType(Patient patient, String type) {
        return patient.getIdentifiers().stream()
                .filter(item -> type.equals(item.getTypeCode()))
                .findFirst()
                .orElseGet(() -> {
                    PatientIdentifier newIdentifier = new PatientIdentifier();
                    newIdentifier.setTypeCode(type);
                    patient.addIdentifier(newIdentifier);
                    return newIdentifier;
                });
    }
}
