package uz.uzinfocom.isemid.services.impls.card.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.CardStatus;
import uz.uzinfocom.isemid.models.entities.card.CardType;
import uz.uzinfocom.isemid.models.entities.card.card_tube.CardTube;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.requests.card.card_tube.ReqCardTube;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.card.CardRepository;
import uz.uzinfocom.isemid.services.card.strategy.UpdateCardStrategy;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UpdateCardTubeStrategy implements UpdateCardStrategy {

    final CardRepository cardRepository;

    @Override
    public ApiResponse<?> updateCard(long id, ReqCard reqCard, User currentUser) {
        CardTube existingCard = (CardTube) cardRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found."));

        if (reqCard.getCardType() == null || !reqCard.getCardType().equals(CardType.CARD_TUBE)) {
            throw new ApiException(BAD_REQUEST, "Card type is not correct.");
        }

        existingCard.setStatus(CardStatus.IN_PROGRESS);
        ReqCardTube cardTube = (ReqCardTube) reqCard;


        CardTube updatedCard = cardRepository.save(existingCard);
        return new ApiResponse<>(true, "Card Tube updated successfully.", updatedCard);
    }
}
