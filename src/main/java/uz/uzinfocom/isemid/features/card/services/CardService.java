package uz.uzinfocom.isemid.features.card.services;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardAssignActRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRejectRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface CardService {
    Page<Card> filter(
            int page,
            int size,
            CardStatus cardStatus,
            String mkb10Code, Long formId, User user
    );

    Card getById(long id);

    Card update(long id, CardRequest reqCard);

    Card acceptCard(Long cardId, User currentUser);

    Card startWorkingOnCard(Long cardId, User currentUser);

    Card rejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard);

    Card saveAndComplete(Long cardId, User user);

    Card supervisorApproveCard(long cardId, User currentUser);

    Card supervisorRejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard);

    Card assignAct(CardAssignActRequest request, User user);

    Page<Card> getAssignedCards(int page, int size, LocalDate fromDate, LocalDate toDate, CardType cardType, CardStatus cardStatus, String mkb10Code,
                                String patientDocValue, User currentUser);
}
