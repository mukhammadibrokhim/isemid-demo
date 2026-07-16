package uz.uzinfocom.app.modules.card.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

@Schema(description = "Количество карт по типу.")
public record CardTypeCountResponse(
        @Schema(description = "Тип карты.")
        CardType type,

        @Schema(description = "Количество карт этого типа.")
        long count
) {
}
