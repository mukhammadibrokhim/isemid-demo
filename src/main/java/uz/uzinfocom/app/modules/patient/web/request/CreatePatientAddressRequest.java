package uz.uzinfocom.app.modules.patient.web.request;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;

public record CreatePatientAddressRequest(

        AddressType type,

        @Size(max = 64)
        String regionCode,

        @Size(max = 64)
        String districtCode,

        @Size(max = 64)
        String neighborhoodCode,

        @Size(max = 1000)
        String streetAddress,

        @Size(max = 64)
        String houseNumber,

        @Size(max = 64)
        String apartmentNumber

) {
}
