package uz.uzinfocom.app.modules.card.web.dto.request.card174;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record InfectionMonitoringRequest(
        Integer sequentialNumber,
        @Size(max = 255) String lastName,
        @Size(max = 255) String firstName,
        @Size(max = 255) String middleName,
        @Size(max = 64) String genderCode,
        LocalDate birthDate,
        @Size(max = 500) String address,
        @Size(max = 255) String profession,
        LocalDate applicationDate,
        LocalDate confirmationDate,
        @Size(max = 500) String possibleInfectionLocation,
        @Size(max = 500) String possibleInfectionFactor,
        LocalDate possibleInfectionDate
) {
}
