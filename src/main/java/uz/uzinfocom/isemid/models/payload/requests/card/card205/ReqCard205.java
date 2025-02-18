package uz.uzinfocom.isemid.models.payload.requests.card.card205;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD205 type card")
public class ReqCard205 extends ReqCard {
    private String field;
}
