package uz.uzinfocom.app.modules.card.web.dto.request.card205;

import jakarta.validation.constraints.Size;

public record InformationAboutAnimaBittenPeopleRequest(
        @Size(max = 64) String animalCategoryCode,
        @Size(max = 255) String animalType,
        @Size(max = 255) String fullNameOfAnimalBittenOwner,
        @Size(max = 500) String addressOfAnimalBittenOwner,
        @Size(max = 64) String region,
        @Size(max = 64) String district,
        @Size(max = 255) String neighborhood,
        @Size(max = 255) String street,
        @Size(max = 32) String houseNumber,
        @Size(max = 32) String apartmentNumber
) {
}
