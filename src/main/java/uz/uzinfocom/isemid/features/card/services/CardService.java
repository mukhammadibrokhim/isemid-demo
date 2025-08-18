package uz.uzinfocom.isemid.features.card.services;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardAssignActRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRejectRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;

public interface CardService {
    //    PagedResponse<?> filter(
//            int page,
//            int size,
//            String formTypeCode,
//            String fromOrgName,
//            String toOrgName,
//            CardStatus cardStatus,
//            Long formId,
//            User user,
//            String fullURL
//    );
//
//    ApiResponse<?> getById(long id);
//
//    ApiResponse<?> updateCard(long id, CardRequest reqCard);
//
//    ApiResponse<?> acceptCard(Long cardId, User currentUser);
//
//    ApiResponse<?> rejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard);
//
//    PagedResponse<?> getCardUsers(int page, int size, Long userId, String fullURL);
//
//    ApiResponse<?> saveAndComplete(Long cardId, User user);
//
//    ApiResponse<?> supervisorApproveCard(long cardId, User currentUser);
//
//    ApiResponse<?> supervisorRejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard);
//
//    ApiResponse<?> getCardActs(long cardId);
//
//    ApiResponse<?> assignAct(CardAssignActRequest reqCardAssignAct, User user);
    Page<Card> filter(
            int page,
            int size,
            String formTypeCode,
            String fromOrgName,
            String toOrgName,
            CardStatus cardStatus,
            Long formId,
            User user
    );

    Card getById(long id);

    Card update(long id, CardRequest reqCard);

    Card acceptCard(Long cardId, User currentUser);

    Card rejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard);

    Page<Card> getCardsByUserId(int page, int size, Long userId);

    Card saveAndComplete(Long cardId, User user);

    Card supervisorApproveCard(long cardId, User currentUser);

    Card supervisorRejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard);

    List<Act> getCardActs(long cardId);

    Card assignAct(CardAssignActRequest request, User user);


}
