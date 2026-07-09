package uz.uzinfocom.app.modules.card.web.dto.request.card_tube;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TBHistoryRequest(
        @Size(max = 255) String infectionLocation,
        LocalDate infectionDate,
        @Size(max = 64) String mkb10Code,
        @Size(max = 500) String mkb10Name,
        @Size(max = 255) String registrationGroup
) {
}
