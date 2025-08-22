package uz.uzinfocom.isemid.features.card.factory;

import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card161.Card161;
import uz.uzinfocom.isemid.features.card.models.card174.Card174;
import uz.uzinfocom.isemid.features.card.models.card175.Card175;
import uz.uzinfocom.isemid.features.card.models.card205.Card205;
import uz.uzinfocom.isemid.features.card.models.card_tube.CardTube;

@Component
public class CardFactory {
    public Card createCard(CardType type) {
        return switch (type) {
            case CARD161 -> new Card161();
            case CARD174 -> new Card174();
            case CARD175 -> new Card175();
            case CARD205 -> new Card205();
            case CARD_TUBE -> new CardTube();
        };
    }

}
