package uz.uzinfocom.app.modules.form0581.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сведения об ином пострадавшем в том же происшествии (помимо основного пациента).")
public record Form0581OtherInjuredPersonDetailResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Фамилия пострадавшего.")
        String lastName,

        @Schema(description = "Имя пострадавшего.")
        String firstName,

        @Schema(description = "Отчество пострадавшего.")
        String middleName,

        @Schema(description = "Код региона проживания пострадавшего.")
        String regionCode,

        @Schema(description = "Код района проживания пострадавшего.")
        String districtCode,

        @Schema(description = "Код махалли проживания пострадавшего.")
        String neighborhoodCode,

        @Schema(description = "Улица проживания пострадавшего.")
        String street,

        @Schema(description = "Номер дома пострадавшего.")
        String houseNumber,

        @Schema(description = "Номер квартиры пострадавшего.")
        String apartmentNumber
) {
}
