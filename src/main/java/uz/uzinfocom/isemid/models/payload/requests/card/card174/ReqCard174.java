package uz.uzinfocom.isemid.models.payload.requests.card.card174;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;

@Getter
@Setter@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD175 type card")
public class ReqCard174 extends ReqCard {
    private String field;
}
