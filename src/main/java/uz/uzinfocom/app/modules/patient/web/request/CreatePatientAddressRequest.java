package uz.uzinfocom.app.modules.patient.web.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;

@Schema(description = "Адрес пациента.")
public record CreatePatientAddressRequest(

        @Schema(description = "Тип адреса (постоянный/временный).")
        AddressType type,

        // stateCode - DMED's own naming for the same field (see DmedCreateForm058Request).
        @Schema(description = "Код региона по классификатору административно-территориального деления.")
        @JsonAlias("stateCode")
        @Size(max = 64, message = "{patient.address.region_code.size}")
        String regionCode,

        // cityCode - DMED's own naming for the same field.
        @Schema(description = "Код района по классификатору административно-территориального деления.")
        @JsonAlias("cityCode")
        @Size(max = 64, message = "{patient.address.district_code.size}")
        String districtCode,

        @Schema(description = "Код массива/махалли (по справочнику).")
        @Size(max = 64, message = "{patient.address.neighborhood_code.size}")
        String neighborhoodCode,

        @Schema(description = "Улица проживания.")
        @Size(max = 1000, message = "{patient.address.street_address.size}")
        String streetAddress,

        @Schema(description = "Номер дома.")
        @Size(max = 64, message = "{patient.address.house_number.size}")
        String houseNumber,

        @Schema(description = "Номер квартиры.")
        @Size(max = 64, message = "{patient.address.apartment_number.size}")
        String apartmentNumber

) {
}
