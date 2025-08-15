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

    public static Card validateCard(long id, CardRequest cardRequest, CardRepository cardRepository, MessageUtil messageUtil) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));

        if (cardRequest.getCardType() == null || !cardRequest.getCardType().equals(card.getCardType())) {
            throw new ApiException(BAD_REQUEST, String.format(messageUtil.get("error.card.invalid_type"), card.getCardType(), cardRequest.getCardType()));
        }
        if (card.getStatus() != CardStatus.COMPLETED)
            card.setStatus(CardStatus.IN_PROGRESS);

        return card;
    }
}
