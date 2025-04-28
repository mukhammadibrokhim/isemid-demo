package uz.uzinfocom.isemid.features.card.dtos.responses.card_tube;

import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.card.dtos.responses.CardResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CardTubeResponse extends CardResponse {
    private String title;
}
