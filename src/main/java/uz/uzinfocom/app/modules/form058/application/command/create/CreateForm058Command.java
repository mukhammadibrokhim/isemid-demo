package uz.uzinfocom.app.modules.form058.application.command.create;

import java.time.Instant;
import java.time.LocalDate;

public record CreateForm058Command(
        String mkb10Code,
        String mkb10Name,
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
