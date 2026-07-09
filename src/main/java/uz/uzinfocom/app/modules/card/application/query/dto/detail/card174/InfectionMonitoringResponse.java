package uz.uzinfocom.app.modules.card.application.query.dto.detail.card174;

import java.time.LocalDate;

public record InfectionMonitoringResponse(
        Long id,
        Integer sequentialNumber,
        String lastName,
        String firstName,
        String middleName,
        String genderCode,
        LocalDate birthDate,
        String address,
        String profession,
        LocalDate applicationDate,
        LocalDate confirmationDate,
        String possibleInfectionLocation,
        String possibleInfectionFactor,
        LocalDate possibleInfectionDate
) {
}
