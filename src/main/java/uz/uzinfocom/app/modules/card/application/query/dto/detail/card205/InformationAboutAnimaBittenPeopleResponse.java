package uz.uzinfocom.app.modules.card.application.query.dto.detail.card205;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сведения о владельце животного, укушенного тем же источником заражения.")
public record InformationAboutAnimaBittenPeopleResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Код категории животного (по справочнику).")
        String animalCategoryCode,

        @Schema(description = "Вид животного.")
        String animalType,

        @Schema(description = "ФИО владельца укушенного животного.")
        String fullNameOfAnimalBittenOwner,

        @Schema(description = "Адрес владельца укушенного животного.")
        String addressOfAnimalBittenOwner,

        @Schema(description = "Код региона по классификатору административно-территориального деления.")
        String region,

        @Schema(description = "Код района по классификатору административно-территориального деления.")
        String district,

        @Schema(description = "Массив/махалля проживания владельца.")
        String neighborhood,

        @Schema(description = "Улица проживания владельца.")
        String street,

        @Schema(description = "Номер дома.")
        String houseNumber,

        @Schema(description = "Номер квартиры.")
        String apartmentNumber
) {
}
