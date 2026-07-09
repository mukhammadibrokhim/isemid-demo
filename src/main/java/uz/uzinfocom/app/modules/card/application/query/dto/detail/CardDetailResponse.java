package uz.uzinfocom.app.modules.card.application.query.dto.detail;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

/**
 * Common contract for every per-type card detail response, matching
 * {@link uz.uzinfocom.app.modules.card.web.dto.request.CardRequest}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Card161DetailResponse.class, name = "CARD161"),
        @JsonSubTypes.Type(value = Card174DetailResponse.class, name = "CARD174"),
        @JsonSubTypes.Type(value = Card175DetailResponse.class, name = "CARD175"),
        @JsonSubTypes.Type(value = Card205DetailResponse.class, name = "CARD205"),
        @JsonSubTypes.Type(value = CardTubeDetailResponse.class, name = "CARD_TUBE")
})
public sealed interface CardDetailResponse permits Card161DetailResponse, Card174DetailResponse, Card175DetailResponse, Card205DetailResponse, CardTubeDetailResponse {

    Long id();

    CardType type();

    CardStatus status();
}
