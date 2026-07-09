package uz.uzinfocom.app.modules.card.web.dto.response;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

public record CardCreateResponse(
        Long id,
        CardType cardType,
        CardStatus status
) {
}
