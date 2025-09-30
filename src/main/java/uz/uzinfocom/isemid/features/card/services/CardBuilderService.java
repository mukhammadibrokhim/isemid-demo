package uz.uzinfocom.isemid.features.card.services;

import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Map;

public interface CardBuilderService {
    List<Card> buildCards(
            List<CardType> cardTypes,
            Map<Long, User> userMap,
            Form058 form058,
            User assignedBy
    );
}
