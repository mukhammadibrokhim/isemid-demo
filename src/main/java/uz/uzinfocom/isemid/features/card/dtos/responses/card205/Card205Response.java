package uz.uzinfocom.isemid.features.card.dtos.responses.card205;

import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.card.dtos.responses.CardResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Card205Response extends CardResponse {
    private String f;
}
