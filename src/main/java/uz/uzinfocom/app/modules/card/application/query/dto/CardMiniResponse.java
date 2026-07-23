package uz.uzinfocom.app.modules.card.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.Instant;

@Schema(description = "Краткие сведения о карте (для встраивания в другие ответы, например, в детальный "
        + "просмотр акта).")
public record CardMiniResponse(
        @Schema(description = "Идентификатор карты.")
        Long id,

        @Schema(description = "Тип карты.")
        CardType cardType,

        @Schema(description = "Наименование типа карты на текущем языке интерфейса.")
        String cardTypeName,

        @Schema(description = "Текущий статус карты.")
        CardStatus status,

        @Schema(description = "Наименование статуса карты на текущем языке интерфейса.")
        String statusName,

        @Schema(description = "Дата и время создания карты.")
        Instant createdAt
) {
}
