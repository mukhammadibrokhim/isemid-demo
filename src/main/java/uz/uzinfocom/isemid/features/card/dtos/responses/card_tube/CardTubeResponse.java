package uz.uzinfocom.isemid.features.card.dtos.responses.card_tube;

import lombok.*;
import uz.uzinfocom.isemid.features.card.dtos.responses.CardResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardTubeResponse extends CardResponse {
    private String title;
}
