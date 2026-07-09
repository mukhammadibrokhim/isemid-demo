package uz.uzinfocom.app.modules.card.application.query.dto;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

public record CardTableResponse(
        Long id,
        CardType cardType,
        CardStatus status,
        Long assignedById
) {
}
