package uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube;

import java.time.LocalDate;

public record TBHistoryResponse(
        Long id,
        String infectionLocation,
        LocalDate infectionDate,
        String mkb10Code,
        String mkb10Name,
        String registrationGroup
) {
}
