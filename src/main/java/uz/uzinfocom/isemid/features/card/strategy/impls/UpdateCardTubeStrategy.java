package uz.uzinfocom.isemid.features.card.strategy.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.card_tube.CardTubeRequest;
import uz.uzinfocom.isemid.features.card.mappers.CardMapper;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card_tube.CardTube;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;

import java.util.Optional;

import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCard;

@Service
@RequiredArgsConstructor
public class UpdateCardTubeStrategy implements UpdateCardStrategy {
    private final CardRepository cardRepository;
    private final MessageUtil messageUtil;
    private final CardMapper mapper;

    @Override
    public ApiResponse<?> updateCard(long id, CardRequest cardRequest) {
        Card card = validateCard(id, cardRequest, cardRepository, messageUtil);
        CardTube existingCard = (CardTube) card;
        CardTubeRequest request = (CardTubeRequest) cardRequest;
        mapper.update(existingCard, request);

        Optional.ofNullable(existingCard.getPreMBTChestXRay())
                .ifPresent(list -> list.forEach(xRay -> xRay.setCardTube(existingCard)));

        Optional.ofNullable(existingCard.getPreviousTBHistory())
                .ifPresent(list -> list.forEach(history -> history.setCardTube(existingCard)));

        Optional.ofNullable(existingCard.getPossibleInfectionSources())
                .ifPresent(list -> list.forEach(possible -> possible.setCardTube(existingCard)));

        Optional.ofNullable(existingCard.getContactMonitoringList())
                .ifPresent(list -> list.forEach(contactMonitoring -> contactMonitoring.setCardTube(existingCard)));
        return ApiResponse.success(messageUtil.get("message.updated"), mapper.toCardResponse(cardRepository.save(existingCard)));
    }
}
