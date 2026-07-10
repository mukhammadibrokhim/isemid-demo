package uz.uzinfocom.app.modules.form0581.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сведения о владельце животного.")
public record Form0581AnimalOwnerDetailResponse(
        @Schema(description = "Фамилия владельца животного.")
        String ownerLastName,

        @Schema(description = "Имя владельца животного.")
        String ownerFirstName,

        @Schema(description = "Отчество владельца животного.")
        String ownerMiddleName,

        @Schema(description = "Код региона проживания владельца.")
        String ownerRegionCode,

        @Schema(description = "Код района проживания владельца.")
        String ownerDistrictCode,

        @Schema(description = "Код махалли проживания владельца.")
        String ownerNeighborhoodCode,

        @Schema(description = "Улица проживания владельца.")
        String ownerStreet,

        @Schema(description = "Номер дома владельца.")
        String ownerHouseNumber,

        @Schema(description = "Номер квартиры владельца.")
        String ownerApartmentNumber
) {
}
