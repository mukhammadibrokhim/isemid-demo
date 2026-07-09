package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.web.dto.request.ChildRequest;

import java.time.LocalDateTime;

public record VaccinationRequest(
        Long id,
        @Size(max = 64) String vaccinationVerifiedCode,
        @Size(max = 255) String vaccinationName,
        @Size(max = 100) String serialNumber,
        LocalDateTime vaccinationDate,
        Integer doseVolume,
        Boolean scheduled
) implements ChildRequest {
}
