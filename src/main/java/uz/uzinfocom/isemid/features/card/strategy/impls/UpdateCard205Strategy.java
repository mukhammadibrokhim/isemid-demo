package uz.uzinfocom.isemid.features.card.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.card205.Card205Request;
import uz.uzinfocom.isemid.features.card.mappers.CardMapper;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card205.Card205;
import uz.uzinfocom.isemid.features.card.reposiotries.Card205Repository;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;

import static uz.uzinfocom.isemid.features.card.utils.CardBindingUtil.bindChildEntities;
import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCard;
import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCard205;

@Service
@RequiredArgsConstructor
public class UpdateCard205Strategy implements UpdateCardStrategy {
    private final CardRepository cardRepository;
    private final Card205Repository card205Repository;
    private final MessageUtil messageUtil;
    private final CardMapper mapper;

    @Override
    @Transactional
    public ApiResponse<?> updateCard(long id, CardRequest request) {
//        Card card = validateCard(id, request.getCardType(), request, cardRepository, messageUtil);
        Card205 existingCard = validateCard205(id, request.getCardType(), request, card205Repository, messageUtil);
        Card205Request card205Request = (Card205Request) request;

        mapper.update(existingCard, card205Request);
        bindChildEntities(existingCard);
        return ApiResponse.success(messageUtil.get("message.updated"), mapper.toCardResponse(card205Repository.save(existingCard)));
    }
}

