package uz.uzinfocom.isemid.features.card.strategy;

import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.models.Card;

public interface UpdateCardStrategy {
    Card update(long id, CardRequest cardRequest);
}