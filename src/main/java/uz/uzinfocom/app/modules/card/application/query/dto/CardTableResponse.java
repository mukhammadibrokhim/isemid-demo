package uz.uzinfocom.app.modules.card.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

@Schema(description = "Строка табличного (списочного) представления карты — только базовые поля, "
        + "без подтипо-специфичных данных, чтобы список формировался без лишних JOIN-ов.")
public record CardTableResponse(
        @Schema(description = "Идентификатор карты.")
        Long id,

        @Schema(description = "Тип карты.")
        CardType cardType,

        @Schema(description = "Текущий статус карты.")
        CardStatus status,

        @Schema(description = "Идентификатор супервайзера, назначившего карту.")
        Long assignedById
) {
}
