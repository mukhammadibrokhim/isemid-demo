package uz.uzinfocom.app.modules.patient.application.command;

import java.time.LocalDate;

public record UpdatePatientIdentifierCommand(
        Long id,
        String type,
        String value,
        LocalDate periodStart,
        LocalDate periodEnd
) {
}
