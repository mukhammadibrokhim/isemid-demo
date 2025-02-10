package uz.uzinfocom.isemid.services.impls.card.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.card.CardRepository;
import uz.uzinfocom.isemid.services.card.strategy.UpdateCardStrategy;

@Service
@RequiredArgsConstructor
public class UpdateCard175Strategy implements UpdateCardStrategy {
    final CardRepository cardRepository;

    @Override
    public ApiResponse<?> updateCard(long id, ReqCard reqCard, User currentUser) {
        return null;
    }
}
