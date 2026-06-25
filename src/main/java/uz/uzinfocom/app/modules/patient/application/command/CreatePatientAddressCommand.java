package uz.uzinfocom.app.modules.patient.application.command;

import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;

public record CreatePatientAddressCommand(

        AddressType type,
        String regionCode,
        String districtCode,
        String neighborhoodCode,
        String streetAddress,
        String houseNumber,
        String apartmentNumber

) {
}