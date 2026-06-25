package uz.uzinfocom.app.modules.patient.application.query.dto.detail;

import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;

import java.time.LocalDate;
import java.util.UUID;

public record PatientAffiliationDetailResponse(
        Long id,
        UUID uuid,
        AffiliationType type,
        LocalDate lastVisitedDate,
        String organizationName,
        String regionCode,
        String districtCode,
        Long organizationId,
        UUID organizationUuid,
        String address
) {
}