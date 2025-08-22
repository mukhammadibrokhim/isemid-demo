package uz.uzinfocom.isemid.features.card.dtos.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.isemid.features.card.dtos.requests.card161.Card161Request;
import uz.uzinfocom.isemid.features.card.dtos.requests.card174.Card174Request;
import uz.uzinfocom.isemid.features.card.dtos.requests.card175.Card175Request;
import uz.uzinfocom.isemid.features.card.dtos.requests.card205.Card205Request;
import uz.uzinfocom.isemid.features.card.dtos.requests.card_tube.CardTubeRequest;
import uz.uzinfocom.isemid.features.card.enums.CardType;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "cardType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Card161Request.class, name = "CARD161"),
        @JsonSubTypes.Type(value = Card174Request.class, name = "CARD174"),
        @JsonSubTypes.Type(value = Card175Request.class, name = "CARD175"),
        @JsonSubTypes.Type(value = Card205Request.class, name = "CARD205"),
        @JsonSubTypes.Type(value = CardTubeRequest.class, name = "CARD_TUBE")
})
public abstract class CardRequest {
    @NotNull(message = "card type is required.")
    private CardType cardType;
    private Long userId;
}
