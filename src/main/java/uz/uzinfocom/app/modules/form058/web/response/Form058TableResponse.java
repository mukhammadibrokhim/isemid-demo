package uz.uzinfocom.app.modules.form058.web.response;

import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Form058TableResponse(
        Long id,
        UUID uuid,
        FormStatus status,
        String mkb10Code,
        String mkb10Name,
        Long senderOrganizationId,
        String senderOrganizationName,
        Long receiverOrganizationId,
        String receiverOrganizationName,
        String patientFullName,
        String nnuzb,
        String pinfl,
        LocalDate patientBirthDate,
        String patientGenderCode,
        String patientPhoneNumber,
        String livingRegionCode,
        String livingRegionName,
        String livingDistrictCode,
        String livingDistrictName,
        String livingNeighborhoodCode,
        String livingNeighborhoodName,
        String livingAddress,
        Instant initialReportDateTime,
        Instant createdAt,
        Instant updatedAt,
        Long assignedCardId
) {
}