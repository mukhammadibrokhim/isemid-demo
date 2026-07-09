package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import java.time.LocalDateTime;

public record VaccinationResponse(
        Long id,
        String vaccinationVerifiedCode,
        String vaccinationName,
        String serialNumber,
        LocalDateTime vaccinationDate,
        Integer doseVolume,
        Boolean scheduled
) {
}
