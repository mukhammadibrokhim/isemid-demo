package uz.uzinfocom.isemid.features.card.strategy.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.card_tube.CardTubeRequest;
import uz.uzinfocom.isemid.features.card.mappers.CardMapper;
import uz.uzinfocom.isemid.features.card.models.card_tube.CardTube;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.reposiotries.CardTubeRepository;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;

import static uz.uzinfocom.isemid.features.card.utils.CardBindingUtil.bindChildEntities;
import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCardTube;

@Service
@RequiredArgsConstructor
public class UpdateCardTubeStrategy implements UpdateCardStrategy {
    private final CardRepository cardRepository;
    private final CardTubeRepository cardTubeRepository;
    private final MessageUtil messageUtil;
    private final CardMapper mapper;

    @Override
    public ApiResponse<?> updateCard(long id, CardRequest request) {
//        Card card = validateCard(id, request.getCardType(), request, cardRepository, messageUtil);
        CardTube existingCard = validateCardTube(id, request.getCardType(), request, cardTubeRepository, messageUtil);
        CardTubeRequest cardTubeRequest = (CardTubeRequest) request;

        mapper.update(existingCard, cardTubeRequest);
        bindChildEntities(existingCard);
        return ApiResponse.success(messageUtil.get("message.updated"), mapper.toCardResponse(cardTubeRepository.save(existingCard)));
    }
}
