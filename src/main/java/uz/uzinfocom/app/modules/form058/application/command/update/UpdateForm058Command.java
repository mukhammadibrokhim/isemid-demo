package uz.uzinfocom.app.modules.form058.application.command.update;

import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientCommand;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UpdateForm058Command(
        Long id,
        String icd10Code,
        String icd10Name,
        LocalDate diseaseDate,
        LocalDate firstVisitDate,
        LocalDate visitDate,
        LocalDateTime initialReportDateTime,
        Long receiverOrganizationId,
        Long hospitalPlaceId,
        String diseasePlaceCode,
        String notifierFullName,
        String journalFormCode,
        String comment,
        UpdatePatientCommand patient,
        Double locationLatitude,
        Double locationLongitude,
        String location
) {
}
