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

import java.util.Optional;

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
        Card card = validateCard(id, request, cardRepository, messageUtil);

        Card161 existingCard = (Card161) card;
        Card161Request request161 = (Card161Request) request;
        cardMapper.update(existingCard, request161);

        Optional.ofNullable(existingCard.getVaccinations())
                .ifPresent(list -> list.forEach(v -> v.setCard161(existingCard)));

        Optional.ofNullable(existingCard.getRiskFactors())
                .ifPresent(list -> list.forEach(rf -> rf.setCard161(existingCard)));

        Optional.ofNullable(existingCard.getPossibleInfectionSources())
                .ifPresent(list -> list.forEach(source -> source.setCard161(existingCard)));

        Optional.ofNullable(existingCard.getEnvironmentalSources())
                .ifPresent(list -> list.forEach(env -> env.setCard161(existingCard)));

        Optional.ofNullable(existingCard.getEnvironmentalLabTests())
                .ifPresent(list -> list.forEach(test -> test.setCard161(existingCard)));

        Optional.ofNullable(existingCard.getContactPersonDetails())
                .ifPresent(list -> list.forEach(contact -> contact.setCard161(existingCard)));

        Optional.ofNullable(existingCard.getScreenedGroups())
                .ifPresent(list -> list.forEach(group -> group.setCard161(existingCard)));

        Optional.ofNullable(existingCard.getHomePreventiveMeasures())
                .ifPresent(list -> list.forEach(measure -> measure.setCard161(existingCard)));

        Optional.ofNullable(existingCard.getOutbreakDisinfectionMeasures())
                .ifPresent(list -> list.forEach(measure -> measure.setCard161(existingCard)));
        return ApiResponse.success(messageUtil.get("message.updated"),
                cardMapper.toCardResponse(cardRepository.save(existingCard)));
    }
}