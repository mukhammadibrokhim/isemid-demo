package uz.uzinfocom.isemid.features.card.dtos.responses;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.card.dtos.responses.card161.Card161Response;
import uz.uzinfocom.isemid.features.card.dtos.responses.card174.Card174Response;
import uz.uzinfocom.isemid.features.card.dtos.responses.card175.Card175Response;
import uz.uzinfocom.isemid.features.card.dtos.responses.card205.Card205Response;
import uz.uzinfocom.isemid.features.card.dtos.responses.card_tube.CardTubeResponse;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058MiniResponse;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserMiniResponse;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "cardType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Card161Response.class, name = "CARD161"),
        @JsonSubTypes.Type(value = Card174Response.class, name = "CARD174"),
        @JsonSubTypes.Type(value = Card175Response.class, name = "CARD175"),
        @JsonSubTypes.Type(value = Card205Response.class, name = "CARD205"),
        @JsonSubTypes.Type(value = CardTubeResponse.class, name = "CARD_TUBE")
})
public abstract class CardResponse {
    private Long id;
    private UUID uuid;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private CardType cardType;
    private CardStatus status;
    private Form058MiniResponse form058;
    private List<UserMiniResponse> users;
}