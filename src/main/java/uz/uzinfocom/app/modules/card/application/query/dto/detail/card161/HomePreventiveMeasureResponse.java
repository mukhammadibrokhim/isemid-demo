package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HomePreventiveMeasureResponse(
        Long id,
        String notifiedPerson,
        LocalDateTime nextDiseaseTime,
        String drugName,
        String dose,
        String series,
        LocalDateTime resultTime,
        LocalDate receivedDate,
        String lisResult,
        String observationResult
) {
}
