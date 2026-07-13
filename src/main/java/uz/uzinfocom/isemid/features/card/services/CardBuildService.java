package uz.uzinfocom.isemid.features.card.services;

import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form0581.models.Form0581;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Map;

public interface CardBuildService  {
    List<Card> buildCards(List<CardType> cardType, Map<Long, User> userMap, Form058 form058, User user);

    List<Card> buildCards(List<CardType> cardType, Map<Long, User> userMap, Form0581 form0581, User user);
}
