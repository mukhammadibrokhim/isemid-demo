package uz.uzinfocom.isemid.models.entities.card.card_tube;

import jakarta.persistence.Entity;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.card.Card;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CardTube extends Card {
    private String specificName;
}
