package uz.uzinfocom.app.modules.form058.application.command.update;

import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;

import java.time.Instant;
import java.time.LocalDate;

public record UpdateForm058Command(
        Long id,
        String mkb10Code,
        String mkb10Name,
        LocalDate diseaseDate,
        LocalDate firstVisitDate,
        LocalDate visitDate,
        Instant initialReportDateTime,
        Long receiverOrganizationId,
        Long hospitalPlaceId,
        String diseasePlace,
        String notifierFullName,
        String journalFormCode,
        String comment,
        CreatePatientCommand patient,
        Double locationLatitude,
        Double locationLongitude,
        String location
) {
}
