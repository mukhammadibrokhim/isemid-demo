package uz.uzinfocom.isemid.features.card.dtos.responses.card174;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.card.dtos.responses.CardResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Card174Response extends CardResponse {
    private String dsa;
}
