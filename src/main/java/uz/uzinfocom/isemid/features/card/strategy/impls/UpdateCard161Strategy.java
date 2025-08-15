package uz.uzinfocom.isemid.features.card.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.card161.Card161Request;
import uz.uzinfocom.isemid.features.card.mappers.CardMapper;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card161.Card161;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;

import static uz.uzinfocom.isemid.features.card.utils.CardBindingUtil.bindChildEntities;
import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCard;

@Service
@RequiredArgsConstructor
public class UpdateCard161Strategy implements UpdateCardStrategy {
    private final CardRepository cardRepository;
    private final MessageUtil messageUtil;
    private final CardMapper cardMapper;

    @Override
    @Transactional
    public ApiResponse<?> updateCard(long id, CardRequest request) {
        Card card = validateCard(id,request.getCardType(), request, cardRepository, messageUtil);

        Card161 existingCard = (Card161) card;
        Card161Request request161 = (Card161Request) request;
        cardMapper.update(existingCard, request161);

        bindChildEntities(existingCard);
        return ApiResponse.success(messageUtil.get("message.updated"),
                cardMapper.toCardResponse(cardRepository.save(existingCard)));
    }
}