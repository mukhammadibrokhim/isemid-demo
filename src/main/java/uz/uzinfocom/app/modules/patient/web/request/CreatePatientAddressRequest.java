package uz.uzinfocom.app.modules.patient.web.request;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;

public record CreatePatientAddressRequest(

        AddressType type,

        @Size(max = 64, message = "{patient.address.region_code.size}")
        String regionCode,

        @Size(max = 64, message = "{patient.address.district_code.size}")
        String districtCode,

        @Size(max = 64, message = "{patient.address.neighborhood_code.size}")
        String neighborhoodCode,

        @Size(max = 1000, message = "{patient.address.street_address.size}")
        String streetAddress,

        @Size(max = 64, message = "{patient.address.house_number.size}")
        String houseNumber,

        @Size(max = 64, message = "{patient.address.apartment_number.size}")
        String apartmentNumber

) {
}
