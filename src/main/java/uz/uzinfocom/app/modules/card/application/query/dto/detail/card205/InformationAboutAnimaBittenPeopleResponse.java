package uz.uzinfocom.app.modules.card.application.query.dto.detail.card205;

public record InformationAboutAnimaBittenPeopleResponse(
        Long id,
        String animalCategoryCode,
        String animalType,
        String fullNameOfAnimalBittenOwner,
        String addressOfAnimalBittenOwner,
        String region,
        String district,
        String neighborhood,
        String street,
        String houseNumber,
        String apartmentNumber
) {
}
