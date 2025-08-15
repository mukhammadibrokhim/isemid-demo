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
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;

import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCard;

@Service
@RequiredArgsConstructor
public class UpdateCard175Strategy implements UpdateCardStrategy {
    private final CardRepository cardRepository;
    private final MessageUtil messageUtil;
    private final CardMapper mapper;

    @Override
    @Transactional
    public ApiResponse<?> updateCard(long id, CardRequest request) {
        Card card = validateCard(id, request.getCardType(), request, cardRepository, messageUtil);
        Card175 existingCard = (Card175) card;
        Card175Request reqCard175 = (Card175Request) request;

        mapper.update(existingCard, reqCard175);
        return ApiResponse.success(messageUtil.get("message.updated"), mapper.toCardResponse(cardRepository.save(existingCard)));
    }
}