package uz.uzinfocom.isemid.features.card.strategy.impls;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCardService {

   final Map<CardType, UpdateCardStrategy> updateCardStrategies;

    public ApiResponse<?> updateCard(long id, CardRequest cardRequest, User currentUser) {
        CardType cardType = cardRequest.getCardType();

        UpdateCardStrategy strategy = updateCardStrategies.get(cardType);
        if (strategy == null) {
            throw new ApiException(BAD_REQUEST, "Неподдерживаемый тип карты: " + cardType);
        }

        return strategy.updateCard(id, cardRequest, currentUser);
    }
}