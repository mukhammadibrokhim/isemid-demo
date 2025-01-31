package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.enums.CardStatus;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCardAssignUsers;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;

public interface CardService {
    ApiResponsePage getAllCardsByFilter(
            int page,
            int size,
            String formTypeCode,
            String fromOrgName,
            String toOrgName,
            CardStatus cardStatus,
            String fullURL
    );

    ApiResponse<?> assignUsers(ReqCardAssignUsers assignUsers, User currentUser);

    ApiResponse<?> getById(long id);
}
