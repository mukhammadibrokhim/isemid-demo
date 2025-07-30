package uz.uzinfocom.isemid.features.card.services;


import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardAssignActRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRejectRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.user.models.User;

public interface CardService {
    PagedResponse<?> getAllCardsByFilter(
            int page,
            int size,
            String formTypeCode,
            String fromOrgName,
            String toOrgName,
            CardStatus cardStatus,
            Long formId,
            User user,
            String fullURL
    );

    ApiResponse<?> getById(long id);

    ApiResponse<?> updateCard(long id, CardRequest reqCard);

    ApiResponse<?> acceptCard(Long cardId, User currentUser);

    ApiResponse<?> rejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard);

    PagedResponse<?> getCardUsers(int page, int size, Long userId, String fullURL);

    ApiResponse<?> saveAndComplete(Long cardId, User user);

    ApiResponse<?> supervisorApproveCard(long cardId, User currentUser);

    ApiResponse<?> supervisorRejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard);

    ApiResponse<?> getCardActs(long cardId);

    ApiResponse<?> assignAct(CardAssignActRequest reqCardAssignAct, User user);
}
