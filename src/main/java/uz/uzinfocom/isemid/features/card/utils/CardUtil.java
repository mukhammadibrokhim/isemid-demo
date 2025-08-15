package uz.uzinfocom.isemid.features.card.utils;

import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card161.Card161;
import uz.uzinfocom.isemid.features.card.models.card174.Card174;
import uz.uzinfocom.isemid.features.card.models.card175.Card175;
import uz.uzinfocom.isemid.features.card.models.card205.Card205;
import uz.uzinfocom.isemid.features.card.models.card_tube.CardTube;
import uz.uzinfocom.isemid.features.card.reposiotries.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@UtilityClass
public class CardUtil {

    public static Card validateCard(long id, @NotNull CardType type, CardRequest cardRequest, CardRepository cardRepository, MessageUtil messageUtil) {
        Card card = cardRepository.findByIdAndType(id, type.name()).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));

        if (cardRequest.getCardType() == null || !cardRequest.getCardType().equals(card.getCardType())) {
            throw new ApiException(BAD_REQUEST, String.format(messageUtil.get("error.card.invalid_type"), card.getCardType(), cardRequest.getCardType()));
        }
        if (card.getStatus() != CardStatus.COMPLETED)
            card.setStatus(CardStatus.IN_PROGRESS);

        return card;
    }

    public static Card161 validateCard161(long id, @NotNull CardType type, CardRequest cardRequest,
                                          Card161Repository repository, MessageUtil messageUtil) {
        Card161 card = repository.findByIdAndTypeForUpdate(id, type)
                .orElseThrow(() -> new ApiException(NOT_FOUND,
                        messageUtil.get("error.not_found_by_id", id)));

        checkCardTypeAndStatus(card, cardRequest, messageUtil);
        return card;
    }

    public static Card174 validateCard174(long id, @NotNull CardType type, CardRequest cardRequest,
                                          Card174Repository repository, MessageUtil messageUtil) {
        Card174 card = repository.findByIdAndTypeForUpdate(id, type)
                .orElseThrow(() -> new ApiException(NOT_FOUND,
                        messageUtil.get("error.not_found_by_id", id)));

        checkCardTypeAndStatus(card, cardRequest, messageUtil);
        return card;
    }

    public static Card175 validateCard175(long id, @NotNull CardType type, CardRequest cardRequest,
                                          Card175Repository repository, MessageUtil messageUtil) {
        Card175 card = repository.findByIdAndTypeForUpdate(id, type)
                .orElseThrow(() -> new ApiException(NOT_FOUND,
                        messageUtil.get("error.not_found_by_id", id)));

        checkCardTypeAndStatus(card, cardRequest, messageUtil);
        return card;
    }

    public static Card205 validateCard205(long id, @NotNull CardType type, CardRequest cardRequest,
                                          Card205Repository repository, MessageUtil messageUtil) {
        Card205 card = repository.findByIdAndTypeForUpdate(id, type)
                .orElseThrow(() -> new ApiException(NOT_FOUND,
                        messageUtil.get("error.not_found_by_id", id)));

        checkCardTypeAndStatus(card, cardRequest, messageUtil);
        return card;
    }

    public static CardTube validateCardTube(long id, @NotNull CardType type, CardRequest cardRequest,
                                            CardTubeRepository repository, MessageUtil messageUtil) {
        CardTube card = repository.findByIdAndTypeForUpdate(id, type)
                .orElseThrow(() -> new ApiException(NOT_FOUND,
                        messageUtil.get("error.not_found_by_id", id)));

        checkCardTypeAndStatus(card, cardRequest, messageUtil);
        return card;
    }

    // Bitta umumiy yordamchi method
    private static void checkCardTypeAndStatus(Card card, CardRequest cardRequest, MessageUtil messageUtil) {
        if (cardRequest.getCardType() == null || !cardRequest.getCardType().equals(card.getCardType())) {
            throw new ApiException(BAD_REQUEST, String.format(
                    messageUtil.get("error.card.invalid_type"),
                    card.getCardType(), cardRequest.getCardType()
            ));
        }

        if (card.getStatus() != CardStatus.COMPLETED) {
            card.setStatus(CardStatus.IN_PROGRESS);
        }
    }

}
