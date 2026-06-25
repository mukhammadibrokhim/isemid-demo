package uz.uzinfocom.app.modules.patient.application.query.dto.detail;

import java.time.LocalDate;
import java.util.UUID;

public record PatientIdentifierDetailResponse(
        Long id,
        UUID uuid,
        String typeCode,
        String value,
        LocalDate periodStart,
        LocalDate periodEnd
) {
}