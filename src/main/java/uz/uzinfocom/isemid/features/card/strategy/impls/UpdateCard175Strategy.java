package uz.uzinfocom.isemid.features.card.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.card175.Card175Request;
import uz.uzinfocom.isemid.features.card.mappers.CardMapper;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card175.Card175;
import uz.uzinfocom.isemid.features.card.reposiotries.Card175Repository;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;

import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCard;
import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCard175;

@Service
@RequiredArgsConstructor
public class UpdateCard175Strategy implements UpdateCardStrategy {
    private final CardRepository cardRepository;
    private final Card175Repository card175Repository;
    private final MessageUtil messageUtil;
    private final CardMapper mapper;

    @Override
    @Transactional
    public ApiResponse<?> updateCard(long id, CardRequest request) {
//        Card card = validateCard(id, request.getCardType(), request, cardRepository, messageUtil);
        Card175 existingCard = validateCard175(id, request.getCardType(), request, card175Repository, messageUtil);
        Card175Request reqCard175 = (Card175Request) request;

        mapper.update(existingCard, reqCard175);
        return ApiResponse.success(messageUtil.get("message.updated"), mapper.toCardResponse(card175Repository.save(existingCard)));
    }
}