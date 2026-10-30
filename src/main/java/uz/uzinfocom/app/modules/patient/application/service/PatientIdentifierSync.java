package uz.uzinfocom.app.modules.patient.application.service;

import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientIdentifierCommand;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;
import uz.uzinfocom.app.platform.security.mask.SensitiveIdentifierMasker;

/**
 * Shared upsert used by every module that lets a patient's identifiers be
 * edited alongside its own form (Form058, Form0581). {@code id} is optional:
 * when supplied it must match one of the patient's existing identifiers -
 * that one is updated in place, and the command is silently ignored if
 * nothing matches; when omitted, a brand-new identifier is always added.
 */
public final class PatientIdentifierSync {

    private PatientIdentifierSync() {
    }

    public static void upsert(Patient patient, UpdatePatientIdentifierCommand command) {
        if (command == null || !StringUtils.hasText(command.type()) || !StringUtils.hasText(command.value())) {
            return;
        }

        PatientIdentifier identifier;
        if (command.id() != null) {
            identifier = findById(patient, command.id());
            if (identifier == null) {
                return;
            }
        } else {
            identifier = new PatientIdentifier();
            patient.addIdentifier(identifier);
        }

        String value = command.value().trim();
        // Responses show sensitive identifiers masked, so a client that sends the form back
        // unchanged echoes the masked text - never let that overwrite the stored value.
        boolean echoedMask = command.id() != null
                && SensitiveIdentifierMasker.isSensitiveType(identifier.getTypeCode())
                && SensitiveIdentifierMasker.isMaskedFormOf(value, identifier.getValue());

        identifier.setTypeCode(command.type());
        if (!echoedMask) {
            identifier.setValue(value);
        }
        identifier.setPeriodStart(command.periodStart());
        identifier.setPeriodEnd(command.periodEnd());
    }

    private static PatientIdentifier findById(Patient patient, Long id) {
        return patient.getIdentifiers().stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }
}
