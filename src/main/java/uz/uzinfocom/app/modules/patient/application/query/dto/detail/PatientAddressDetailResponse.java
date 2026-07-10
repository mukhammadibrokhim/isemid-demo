package uz.uzinfocom.app.modules.patient.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;

import java.util.UUID;

@Schema(description = "Адрес пациента.")
public record PatientAddressDetailResponse(
        @Schema(description = "Идентификатор записи адреса.")
        Long id,

        @Schema(description = "UUID записи адреса.")
        UUID uuid,

        @Schema(description = "Тип адреса (постоянный/временный).")
        AddressType type,

        @Schema(description = "Код региона по классификатору административно-территориального деления.")
        String regionCode,

        @Schema(description = "Код района по классификатору административно-территориального деления.")
        String districtCode,

        @Schema(description = "Код массива/махалли (по справочнику).")
        String neighborhoodCode,

        @Schema(description = "Улица проживания.")
        String streetAddress,

        @Schema(description = "Номер дома.")
        String houseNumber,

        @Schema(description = "Номер квартиры.")
        String apartmentNumber
) {
}
