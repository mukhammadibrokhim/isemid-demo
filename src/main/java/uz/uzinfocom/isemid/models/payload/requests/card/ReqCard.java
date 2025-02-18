package uz.uzinfocom.isemid.models.payload.requests.card;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.isemid.models.entities.card.CardType;
import uz.uzinfocom.isemid.models.payload.requests.card.card161.ReqCard161;
import uz.uzinfocom.isemid.models.payload.requests.card.card174.ReqCard174;
import uz.uzinfocom.isemid.models.payload.requests.card.card175.ReqCard175;
import uz.uzinfocom.isemid.models.payload.requests.card.card205.ReqCard205;
import uz.uzinfocom.isemid.models.payload.requests.card.card_tube.ReqCardTube;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "cardType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReqCard161.class, name = "CARD161"),
        @JsonSubTypes.Type(value = ReqCard174.class, name = "CARD174"),
        @JsonSubTypes.Type(value = ReqCard175.class, name = "CARD175"),
        @JsonSubTypes.Type(value = ReqCard205.class, name = "CARD205"),
        @JsonSubTypes.Type(value = ReqCardTube.class, name = "CARD_TUBE")
})
public abstract class ReqCard {

    @NotNull(message = "card type is required.")
    private CardType cardType;

//    @NotNull(message = "User id is required.")
    private long userId;
}
