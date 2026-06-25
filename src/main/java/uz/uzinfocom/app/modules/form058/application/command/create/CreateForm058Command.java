package uz.uzinfocom.app.modules.form058.application.command.create;

import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;

import java.time.Instant;
import java.time.LocalDate;

public record CreateForm058Command(
        String mkb10Code,
        String mkb10Name,
        CreatePatientCommand patient,
        String source,
        LocalDate diseaseDate,
        LocalDate firstVisitDate,
        LocalDate visitDate,
        Instant initialReportDateTime,
        Long senderOrganizationId,
        Long receiverOrganizationId,
        Long hospitalPlaceId,
        String diseasePlace,
        String notifierFullName,
        String journalFormCode,
        String comment,
        Double locationLatitude,
        Double locationLongitude,
        String location
) {
}
