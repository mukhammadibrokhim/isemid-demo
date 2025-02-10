package uz.uzinfocom.isemid.services.impls.card.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.Card161;
import uz.uzinfocom.isemid.models.entities.enums.CardType;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.card.CardRepository;
import uz.uzinfocom.isemid.services.card.strategy.UpdateCardStrategy;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UpdateCard161Strategy implements UpdateCardStrategy {
    private final CardRepository cardRepository;

    @Override
    public ApiResponse<?> updateCard(long id, ReqCard reqCard, User currentUser) {
        Card161 existingCard = (Card161) cardRepository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Card161 not found."));

        if (reqCard.getCardType() == null || !reqCard.getCardType().equals(CardType.CARD161)) {
            throw new ApiException(BAD_REQUEST, "Card type is not correct.");
        }




        return new ApiResponse<>(true, "Card161 updated successfully.", cardRepository.save(existingCard));
    }
}