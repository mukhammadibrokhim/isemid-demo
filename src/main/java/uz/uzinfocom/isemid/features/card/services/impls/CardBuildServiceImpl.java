package uz.uzinfocom.isemid.features.card.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.factory.CardFactory;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.services.CardBuildService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CardBuildServiceImpl implements CardBuildService {
    private final CardFactory cardFactory;

    /**
     * Builds or updates cards based on requested card types and users.
     */
    @Override
    public List<Card> buildCards(List<CardType> cardTypes,
                                 Map<Long, User> userMap,
                                 Form058 form058,
                                 User assignedBy) {

        List<Card> resultCards = new ArrayList<>();

        for (CardType cardType : cardTypes) {
            Optional<Card> existingCardOpt = form058.getCards().stream()
                    .filter(card -> card.getCardType() == cardType)
                    .findFirst();

            if (existingCardOpt.isPresent()) {
                // Update existing card users and status
                Card existingCard = existingCardOpt.get();
                existingCard.setUsers(new HashSet<>(userMap.values()));
                existingCard.setStatus(CardStatus.NEW);
                resultCards.add(existingCard);
            } else {
                // Create new card via factory
                Card newCard = cardFactory.createCard(cardType);
                newCard.setCreatedBy(assignedBy.getId());
                newCard.setUpdatedBy(assignedBy.getId());
                newCard.setUsers(new HashSet<>(userMap.values()));
                newCard.setStatus(CardStatus.NEW);
                newCard.setCardType(cardType);
                newCard.setAssignedById(assignedBy.getId());
                newCard.setForm058(form058);
                resultCards.add(newCard);
            }
        }

        return resultCards;
    }
}
