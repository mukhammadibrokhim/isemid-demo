package uz.uzinfocom.app.modules.card.web.dto.request.card205;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record InformationOtherBittenPeopleRequest(
        @Size(max = 255) String lastName,
        @Size(max = 255) String firstName,
        @Size(max = 255) String middleName,
        @Size(max = 32) String gender,
        @Size(max = 32) String birthDate,
        @Size(max = 500) String livingAddress,
        @Size(max = 64) String region,
        @Size(max = 64) String district,
        @Size(max = 255) String neighborhood,
        @Size(max = 255) String street,
        @Size(max = 32) String houseNumber,
        @Size(max = 32) String apartmentNumber,
        LocalDateTime bittenDate
) {
}
