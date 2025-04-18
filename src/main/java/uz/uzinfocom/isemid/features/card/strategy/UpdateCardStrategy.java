package uz.uzinfocom.isemid.features.card.strategy;


import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.user.models.User;

public interface UpdateCardStrategy {
    ApiResponse<?> updateCard(long id, CardRequest cardRequest, User currentUser);
}