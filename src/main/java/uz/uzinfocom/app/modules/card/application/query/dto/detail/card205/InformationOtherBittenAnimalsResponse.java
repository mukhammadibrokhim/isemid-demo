package uz.uzinfocom.app.modules.card.application.query.dto.detail.card205;

import java.time.LocalDateTime;

public record InformationOtherBittenAnimalsResponse(
        Long id,
        String bittenAnimalCategoryCode,
        LocalDateTime bittenDateTime,
        String whereAnimalBitten
) {
}
