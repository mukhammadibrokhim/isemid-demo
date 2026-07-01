package uz.uzinfocom.app.modules.form058.application.query.dto;

import java.time.Instant;
import java.util.UUID;

public record Form058TableResponse(
        Long id,
        UUID uuid,
        Instant createdAt,
        Form058TableStatus status,
        String mkb10Code,
        String mkb10Name,
        String source,
        Long senderOrganizationId,
        String senderOrganizationName,
        PatientShortResponse patient
) {
    public record PatientShortResponse(
            Long id,
            String firstName,
            String lastName,
            String middleName,

            String permanentRegionName,
            String permanentDistrictName,
            String permanentNeighborhoodName,

            String permanentStreetAddress,
            String permanentHouseNumber,
            String permanentApartmentNumber
    ) {
    }
}