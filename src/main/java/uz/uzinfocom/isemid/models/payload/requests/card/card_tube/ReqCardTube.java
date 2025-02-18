package uz.uzinfocom.isemid.models.payload.requests.card.card_tube;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD_TUBE type card")
public class ReqCardTube extends ReqCard {
    private String field;
}
