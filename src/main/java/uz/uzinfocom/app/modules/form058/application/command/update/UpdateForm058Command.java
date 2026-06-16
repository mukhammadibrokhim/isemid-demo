package uz.uzinfocom.app.modules.form058.application.command.update;

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
        String patientNnuzb,
        String patientPinfl,
        String patientFullName,
        LocalDate patientBirthDate,
        String patientGender,
        String patientPhone,
        String locationRegionCode,
        String locationDistrictCode,
        String locationNeighborhoodCode,
        String locationAddress
) {
}
