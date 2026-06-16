package uz.uzinfocom.app.modules.form058.application.query.dto;

import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Form058DetailResult(
        Long id,
        UUID uuid,
        FormStatus status,
        String source,
        Long senderOrganizationId,
        Long receiverOrganizationId,
        Long hospitalPlaceId,
        String mkb10Code,
        String mkb10Name,
        String finalMkb10Code,
        String finalMkb10Name,
        LocalDate diseaseDate,
        LocalDate firstVisitDate,
        LocalDate visitDate,
        Instant initialReportDateTime,
        String diseasePlace,
        String notifierFullName,
        String journalFormCode,
        String comment,
        String patientNnuzb,
        String patientPinfl,
        String patientFullName,
        LocalDate patientBirthDate,
        String patientGender,
        String patientPhone,
        String locationRegionCode,
        String locationDistrictCode,
        String locationNeighborhoodCode,
        String locationAddress,
        boolean hasLinkedCards,
        Long assignedCardId,
        String cancelReason,
        Long canceledBy,
        Instant canceledAt,
        Long approvedBy,
        Long approvedOrganizationId,
        Instant approvedAt,
        String notApprovedReason,
        Instant createdAt,
        Instant updatedAt,
        Long createdBy,
        Long updatedBy
) {
}
