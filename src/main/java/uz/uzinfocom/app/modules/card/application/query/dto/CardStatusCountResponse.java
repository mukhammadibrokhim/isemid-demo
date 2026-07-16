package uz.uzinfocom.app.modules.card.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;

@Schema(description = "Количество карт по статусу.")
public record CardStatusCountResponse(
        @Schema(description = "Статус карты.")
        CardStatus status,

        @Schema(description = "Количество карт с этим статусом.")
        long count
) {
}
