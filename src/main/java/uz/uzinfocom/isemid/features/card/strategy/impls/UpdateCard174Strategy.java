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
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;

import java.util.Optional;

import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCard;

@Service
@RequiredArgsConstructor
public class UpdateCard174Strategy implements UpdateCardStrategy {
    private final CardRepository cardRepository;
    private final MessageUtil messageUtil;
    private final CardMapper mapper;

    @Override
    @Transactional
    public ApiResponse<?> updateCard(long id, CardRequest reqCard) {
        Card card = validateCard(id, reqCard, cardRepository, messageUtil);
        Card174 existingCard = (Card174) card;

        Card174Request reqCard174 = (Card174Request) reqCard;

        mapper.update(existingCard, reqCard174);

        Optional.ofNullable(existingCard.getInfectionMonitoring())
                .ifPresent(list -> list.forEach(item -> item.setCard174(existingCard)));

        Optional.ofNullable(existingCard.getOutbreakControlMeasures())
                .ifPresent(list -> list.forEach(item -> item.setCard174(existingCard)));

        return ApiResponse.success(messageUtil.get("message.updated"), mapper.toCardResponse(cardRepository.save(existingCard)));
    }
}