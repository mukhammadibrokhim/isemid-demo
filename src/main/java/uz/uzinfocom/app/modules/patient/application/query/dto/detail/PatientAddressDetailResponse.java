package uz.uzinfocom.app.modules.patient.application.query.dto.detail;

import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;

import java.util.UUID;

public record PatientAddressDetailResponse(
        Long id,
        UUID uuid,
        AddressType type,
        String regionCode,
        String districtCode,
        String neighborhoodCode,
        String streetAddress,
        String houseNumber,
        String apartmentNumber
) {
}