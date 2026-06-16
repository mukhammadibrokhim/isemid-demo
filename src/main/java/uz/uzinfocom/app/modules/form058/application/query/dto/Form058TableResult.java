package uz.uzinfocom.app.modules.form058.application.query.dto;

import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

import java.time.Instant;
import java.util.UUID;

public record Form058TableResult(
        Long id,
        UUID uuid,
        FormStatus status,
        String mkb10Code,
        String mkb10Name,
        String patientNnuzb,
        String patientFullName,
        Long senderOrganizationId,
        Long receiverOrganizationId,
        Instant initialReportDateTime,
        Long assignedCardId
) {
}
