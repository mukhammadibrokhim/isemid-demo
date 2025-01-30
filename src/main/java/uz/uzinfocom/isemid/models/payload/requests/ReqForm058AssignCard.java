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
public class ReqForm058AssignCard {
    @NotNull(message = "Form058 id is required.")
    private long form058Id;
    @NotNull(message = "Card type is required.")
    private CardType cardType;
}
