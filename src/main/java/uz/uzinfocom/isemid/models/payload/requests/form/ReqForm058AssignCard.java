package uz.uzinfocom.isemid.models.payload.requests.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.entities.card.CardType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqForm058AssignCard {
    @NotNull(message = "Form058 id is required.")
    private long form058Id;
    @NotNull(message = "Card type is required.")
    private List<CardType> cardType;

    @Valid
    @NotEmpty
    private List<CardUser> assignUsers;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CardUser {
        private Long userId;
        private CardType cardType;
    }
}
