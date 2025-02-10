package uz.uzinfocom.isemid.services.card.strategy;

import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;

public interface UpdateCardStrategy {
    ApiResponse<?> updateCard(long id, ReqCard reqCard, User currentUser);
}