package uz.uzinfocom.isemid.features.card.dtos.responses.card175;

import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.card.dtos.responses.CardResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Card175Response extends CardResponse {
    private String card175Filed;
}
