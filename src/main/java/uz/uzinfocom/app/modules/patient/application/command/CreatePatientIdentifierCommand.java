package uz.uzinfocom.app.modules.patient.application.command;

import java.time.LocalDate;

public record CreatePatientIdentifierCommand(
        String type,
        String value,
        LocalDate periodStart,
        LocalDate periodEnd
) {
}