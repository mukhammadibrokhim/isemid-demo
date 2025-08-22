package uz.uzinfocom.isemid.features.card.strategy.impls;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCardService {
    private final Map<CardType, UpdateCardStrategy> updateCardStrategies;
    private final MessageUtil messageUtil;

    public Card update(long id, CardRequest cardRequest) {
        CardType cardType = cardRequest.getCardType();

        UpdateCardStrategy strategy = updateCardStrategies.get(cardType);
        if (strategy == null) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.card.unsupported_type", cardType));
        }

        return strategy.update(id, cardRequest);
    }
}