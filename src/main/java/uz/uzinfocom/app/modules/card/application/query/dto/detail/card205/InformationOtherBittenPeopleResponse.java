package uz.uzinfocom.app.modules.card.application.query.dto.detail.card205;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Сведения о другом лице, пострадавшем от укуса того же животного.")
public record InformationOtherBittenPeopleResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Фамилия пострадавшего.")
        String lastName,

        @Schema(description = "Имя пострадавшего.")
        String firstName,

        @Schema(description = "Отчество пострадавшего.")
        String middleName,

        @Schema(description = "Пол пострадавшего.")
        String gender,

        @Schema(description = "Дата рождения пострадавшего.")
        String birthDate,

        @Schema(description = "Адрес проживания пострадавшего.")
        String livingAddress,

        @Schema(description = "Код региона по классификатору административно-территориального деления.")
        String region,

        @Schema(description = "Код района по классификатору административно-территориального деления.")
        String district,

        @Schema(description = "Массив/махалля проживания.")
        String neighborhood,

        @Schema(description = "Улица проживания.")
        String street,

        @Schema(description = "Номер дома.")
        String houseNumber,

        @Schema(description = "Номер квартиры.")
        String apartmentNumber,

        @Schema(description = "Дата и время укуса.")
        LocalDateTime bittenDate
) {
}
