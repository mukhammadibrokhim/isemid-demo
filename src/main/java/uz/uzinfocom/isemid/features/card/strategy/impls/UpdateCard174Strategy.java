package uz.uzinfocom.isemid.features.card.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.card174.Card174Request;
import uz.uzinfocom.isemid.features.card.mappers.CardMapper;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card174.Card174;
import uz.uzinfocom.isemid.features.card.reposiotries.Card174Repository;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;

import static uz.uzinfocom.isemid.features.card.utils.CardBindingUtil.bindChildEntities;
import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCard;
import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCard174;

@Service
@RequiredArgsConstructor
public class UpdateCard174Strategy implements UpdateCardStrategy {
    private final CardRepository cardRepository;
    private final Card174Repository card174Repository;
    private final MessageUtil messageUtil;
    private final CardMapper mapper;

    @Override
    @Transactional
    public ApiResponse<?> updateCard(long id, CardRequest request) {
//        Card card = validateCard(id, request.getCardType(), request, cardRepository, messageUtil);
        Card174 existingCard = validateCard174(id, request.getCardType(), request, card174Repository, messageUtil);

        Card174Request reqCard174 = (Card174Request) request;

        mapper.update(existingCard, reqCard174);
        bindChildEntities(existingCard);

        return ApiResponse.success(messageUtil.get("message.updated"), mapper.toCardResponse(card174Repository.save(existingCard)));
    }
}