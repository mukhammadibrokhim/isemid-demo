package uz.uzinfocom.app.modules.form0581.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Сведения о владельце животного, нанёсшего укус/царапину/ослюнение.")
public record AnimalOwnerRequest(
        @Schema(description = "Фамилия владельца животного.")
        @Size(max = 255, message = "{validation.form0581.owner-last-name.size}")
        String ownerLastName,

        @Schema(description = "Имя владельца животного.")
        @Size(max = 255, message = "{validation.form0581.owner-first-name.size}")
        String ownerFirstName,

        @Schema(description = "Отчество владельца животного.")
        @Size(max = 255, message = "{validation.form0581.owner-middle-name.size}")
        String ownerMiddleName,

        @Schema(description = "Код региона проживания владельца (по справочнику).")
        @Size(max = 64, message = "{validation.form0581.owner-region-code.size}")
        String regionCode,

        @Schema(description = "Код района проживания владельца (по справочнику).")
        @Size(max = 64, message = "{validation.form0581.owner-district-code.size}")
        String districtCode,

        @Schema(description = "Код махалли проживания владельца (по справочнику).")
        @Size(max = 64, message = "{validation.form0581.owner-neighborhood-code.size}")
        String neighborhoodCode,

        @Schema(description = "Улица проживания владельца.")
        @Size(max = 255, message = "{validation.form0581.owner-street.size}")
        String street,

        @Schema(description = "Номер дома владельца.")
        @Size(max = 32, message = "{validation.form0581.owner-house-number.size}")
        String houseNumber,

        @Schema(description = "Номер квартиры владельца.")
        @Size(max = 32, message = "{validation.form0581.owner-apartment-number.size}")
        String apartmentNumber
) {
}
