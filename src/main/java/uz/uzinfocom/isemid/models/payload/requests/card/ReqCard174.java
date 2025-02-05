package uz.uzinfocom.isemid.models.payload.requests.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD175 type card")
public class ReqCard174 extends ReqCard{
    private String field;
}
