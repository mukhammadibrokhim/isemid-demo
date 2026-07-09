package uz.uzinfocom.app.modules.card.application.query.dto.detail.card205;

import java.time.LocalDateTime;

public record InformationOtherBittenPeopleResponse(
        Long id,
        String lastName,
        String firstName,
        String middleName,
        String gender,
        String birthDate,
        String livingAddress,
        String region,
        String district,
        String neighborhood,
        String street,
        String houseNumber,
        String apartmentNumber,
        LocalDateTime bittenDate
) {
}
