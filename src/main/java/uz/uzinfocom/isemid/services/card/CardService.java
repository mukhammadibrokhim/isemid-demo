package uz.uzinfocom.isemid.services.card;

import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.CardStatus;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqCardAssignAct;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqRejectCard;
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
            Long formId, User user, String fullURL
    );

    ApiResponse<?> getById(long id);

    ApiResponse<?> updateCard(long id, ReqCard reqCard, User currentUser);

    ApiResponse<?> acceptCard(Long cardId, User currentUser);

    ApiResponse<?> rejectCard(Long cardId, User currentUser, ReqRejectCard rejectCard);

    ApiResponsePage getCardUsers(int page, int size, Long userId, String fullURL);

    ApiResponse<?> saveAndComplete(Long cardId, User user);

    ApiResponse<?> supervisorApproveCard(long cardId, User currentUser);

    ApiResponse<?> supervisorRejectCard(Long cardId, User currentUser, ReqRejectCard rejectCard);

    ApiResponse<?> assignAct(ReqCardAssignAct reqCardAssignAct, User user);

    ApiResponse<?> changeStatus(long cardId, CardStatus cardStatus);
}
