package uz.uzinfocom.app.modules.form0581.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "Сведения об ином пострадавшем в том же происшествии (помимо основного пациента).")
public record OtherInjuredPersonRequest(
        @Schema(description = "Идентификатор записи. Не указывается (null) при добавлении новой записи; "
                + "указывается для обновления уже существующей.")
        Long id,

        @Schema(description = "Фамилия пострадавшего.")
        @Size(max = 255, message = "{validation.form0581.other-injured.last-name.size}")
        String lastName,

        @Schema(description = "Имя пострадавшего.")
        @Size(max = 255, message = "{validation.form0581.other-injured.first-name.size}")
        String firstName,

        @Schema(description = "Отчество пострадавшего.")
        @Size(max = 255, message = "{validation.form0581.other-injured.middle-name.size}")
        String middleName,

        @Schema(description = "Код региона проживания пострадавшего (по справочнику).")
        @Size(max = 64, message = "{validation.form0581.other-injured.region-code.size}")
        String regionCode,

        @Schema(description = "Код района проживания пострадавшего (по справочнику).")
        @Size(max = 64, message = "{validation.form0581.other-injured.district-code.size}")
        String districtCode,

        @Schema(description = "Код махалли проживания пострадавшего (по справочнику).")
        @Size(max = 64, message = "{validation.form0581.other-injured.neighborhood-code.size}")
        String neighborhoodCode,

        @Schema(description = "Улица проживания пострадавшего.")
        @Size(max = 255, message = "{validation.form0581.other-injured.street.size}")
        String street,

        @Schema(description = "Номер дома пострадавшего.")
        @Size(max = 32, message = "{validation.form0581.other-injured.house-number.size}")
        String houseNumber,

        @Schema(description = "Номер квартиры пострадавшего.")
        @Size(max = 32, message = "{validation.form0581.other-injured.apartment-number.size}")
        String apartmentNumber
) implements ChildRequest {
}
