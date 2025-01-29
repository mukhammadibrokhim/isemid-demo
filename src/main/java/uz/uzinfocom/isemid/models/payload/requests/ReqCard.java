package uz.uzinfocom.isemid.models.payload.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.entities.enums.CardType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqCard {

    @NotNull(message = "card type is required.")
    private CardType cardType;

    @NotNull(message = "User id is required.")
    private long userId;
}
