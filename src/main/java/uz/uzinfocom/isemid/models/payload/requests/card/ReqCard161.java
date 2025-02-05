package uz.uzinfocom.isemid.models.payload.requests.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD161 type card")
public class ReqCard161 extends ReqCard {
    private String field;
}
