package uz.uzinfocom.isemid.features.card.strategy.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.card_tube.CardTubeRequest;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.card_tube.CardTube;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;
import uz.uzinfocom.isemid.features.user.models.User;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UpdateCardTubeStrategy implements UpdateCardStrategy {

    private final CardRepository cardRepository;

    @Override
    public ApiResponse<?> updateCard(long id, CardRequest cardRequest, User currentUser) {
        CardTube existingCard = (CardTube) cardRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND));

        if (cardRequest.getCardType() == null || !cardRequest.getCardType().equals(CardType.CARD_TUBE)) {
            throw new ApiException(BAD_REQUEST, "Тип карты неправильный.");
        }

        existingCard.setStatus(CardStatus.IN_PROGRESS);
        CardTubeRequest cardTubeRequest = (CardTubeRequest) cardRequest;


        CardTube updatedCard = cardRepository.save(existingCard);
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, updatedCard);
    }
}
