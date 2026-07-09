package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import jakarta.validation.constraints.Size;

public record InfectionSourceDetailRequest(
        @Size(max = 64) String infectionSourceNotFoundCode,
        @Size(max = 500) String personFullName,
        @Size(max = 64) String infectionSourceDiseasePeriodCode,
        @Size(max = 64) String animalTypeCode
) {
}
