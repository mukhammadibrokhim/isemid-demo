package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.web.dto.request.ChildRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HomePreventiveMeasureRequest(
        Long id,
        @Size(max = 500) String notifiedPerson,
        LocalDateTime nextDiseaseTime,
        @Size(max = 255) String drugName,
        @Size(max = 100) String dose,
        @Size(max = 100) String series,
        LocalDateTime resultTime,
        LocalDate receivedDate,
        @Size(max = 500) String lisResult,
        @Size(max = 500) String observationResult
) implements ChildRequest {
}
