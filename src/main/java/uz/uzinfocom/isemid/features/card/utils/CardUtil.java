package uz.uzinfocom.isemid.features.card.utils;

import lombok.experimental.UtilityClass;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@UtilityClass
public class CardUtil {

    public static Card validateCard(long id, CardRequest cardRequest, CardRepository cardRepository) {
        Card card = cardRepository.findByIdForUpdate(id).orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_ID, id)));

        if (cardRequest.getCardType() == null || !cardRequest.getCardType().equals(card.getCardType())) {
            throw new ApiException(BAD_REQUEST, String.format(MessageUtil.INVALID_TYPE, card.getCardType(), cardRequest.getCardType()));
        }
        if (card.getStatus() != CardStatus.COMPLETED)
            card.setStatus(CardStatus.IN_PROGRESS);

        return card;
    }
}
