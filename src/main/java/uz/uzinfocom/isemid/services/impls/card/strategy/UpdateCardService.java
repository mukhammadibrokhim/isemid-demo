package uz.uzinfocom.isemid.services.impls.card.strategy;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.CardType;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.card.strategy.UpdateCardStrategy;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCardService {

   final Map<CardType, UpdateCardStrategy> updateCardStrategies;

    public ApiResponse<?> updateCard(long id, ReqCard reqCard, User currentUser) {
        CardType cardType = reqCard.getCardType();

        UpdateCardStrategy strategy = updateCardStrategies.get(cardType);
        if (strategy == null) {
            throw new ApiException(BAD_REQUEST, "Unsupported card type: " + cardType);
        }

        return strategy.updateCard(id, reqCard, currentUser);
    }
}