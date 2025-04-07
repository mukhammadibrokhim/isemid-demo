package uz.uzinfocom.isemid.services.impls.card;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.act.ActStatus;
import uz.uzinfocom.isemid.models.entities.card.Card;
import uz.uzinfocom.isemid.models.entities.card.CardStatus;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqCardAssignAct;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqRejectCard;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.UserRepository;
import uz.uzinfocom.isemid.repositories.card.CardRepository;
import uz.uzinfocom.isemid.services.card.CardService;
import uz.uzinfocom.isemid.services.impls.card.strategy.UpdateCardService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.utils.CommonUtils.*;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    final CardRepository cardRepository;
    final UserRepository userRepository;
    final UpdateCardService updateCardService;

    @Override
    public ApiResponsePage getAllCardsByFilter(int page, int size, String formTypeCode, String fromOrName, String toOrgName, CardStatus cardStatus, Long formId, User user, String fullURL) {
        Specification<Card> spec = getFilteredSpecification(formTypeCode, fromOrName, toOrgName, cardStatus, null, false, formId);
        Page<Card> cardPage = cardRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, cardPage, fullURL);
    }

    @Override
    public ApiResponse<?> getById(long id) {
        return new ApiResponse<>(true, "successfully retrieved.",
                cardRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found.")));
    }

    @Transactional
    @Override
    public ApiResponse<?> updateCard(long id, ReqCard reqCard, User currentUser) {
        return updateCardService.updateCard(id, reqCard, currentUser);
    }

    @Override
    public ApiResponse<?> acceptCard(Long cardId, User currentUser) {
        Card card = validateAndGetCard(cardId, currentUser);

        if (card.getStatus() != CardStatus.NEW && card.getStatus() != CardStatus.IN_PROGRESS) {
            throw new ApiException(BAD_REQUEST, "Card cannot be accepted in its current status: " + card.getStatus());
        }

        card.setStatus(CardStatus.ACCEPTED_BY_USER);
        return new ApiResponse<>(true, "successfully accepted.", cardRepository.save(card));
    }

    @Override
    public ApiResponse<?> rejectCard(Long cardId, User currentUser, ReqRejectCard rejectCard) {
        Card card = validateAndGetCard(cardId, currentUser);
        if (card.getStatus() == CardStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, "Completed card cannot be rejected.");
        }
        card.setStatus(CardStatus.REJECTED_BY_USER);
        card.setAttachedUserComment(rejectCard.getComment());
        return new ApiResponse<>(true, "Card successfully rejected by user.", cardRepository.save(card));
    }

    @Override
    public ApiResponsePage getCardUsers(int page, int size, Long userId, String fullURL) {
        Specification<Card> spec = getFilteredSpecification(null, null, null, null, userId, true, null);
        Page<Card> cardPage = cardRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, cardPage, fullURL);
    }

    @Override
    public ApiResponse<?> saveAndComplete(Long cardId, User currentUser) {
        Card card = validateAndGetCard(cardId, currentUser);
        if (card.getStatus() == CardStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, "Card is already completed.");
        }
        card.setCompletedDate(LocalDate.now());
        card.setStatus(CardStatus.COMPLETED);
        return new ApiResponse<>(true, "successfully saved.", cardRepository.save(card));
    }

    @Override
    public ApiResponse<?> supervisorApproveCard(long cardId, User currentUser) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found."));
        if (!card.getAssignedById().equals(currentUser.getId())) {
            throw new ApiException(FORBIDDEN, "You are not authorized to approve this card.");
        }
        if (!card.getStatus().equals(CardStatus.COMPLETED)) {
            throw new ApiException(BAD_REQUEST, "Card is not completed.");
        }
        card.setStatus(CardStatus.APPROVED);
        return new ApiResponse<>(true, "successfully approve.", cardRepository.save(card));
    }

    @Override
    public ApiResponse<?> supervisorRejectCard(Long cardId, User currentUser, ReqRejectCard rejectCard) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found."));

        if (!card.getAssignedById().equals(currentUser.getId())) {
            throw new ApiException(FORBIDDEN, "You are not authorized to reject this card.");
        }

        if (card.getStatus() != CardStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, "Only completed cards can be rejected.");
        }

        if (rejectCard.getComment() == null || rejectCard.getComment().trim().isEmpty()) {
            throw new ApiException(BAD_REQUEST, "Rejection reason is required.");
        }

        card.setStatus(CardStatus.REJECTED);
        card.setSupervisorComment(rejectCard.getComment());
        return new ApiResponse<>(true, "Supervisor successfully rejected.", cardRepository.save(card));
    }

    @Override
    public ApiResponse<?> assignAct(ReqCardAssignAct reqCardAssignAct, User user) {
        Card card = cardRepository.findById(reqCardAssignAct.getCardId())
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found by id: " + reqCardAssignAct.getCardId()));

        List<Act> acts = reqCardAssignAct.getActTypes().stream()
                .map(actType -> {
                    Act act = createActInstance(actType, card);
                    act.setActType(actType);
                    act.setCard(card);
                    act.setActStatus(ActStatus.NEW);
                    return act;
                })
                .toList();
        card.getActs().addAll(acts);
        cardRepository.save(card);
        return new ApiResponse<>(true, "Acts successfully assigned.", card);
    }

    private Card validateAndGetCard(long cardId, User currentUser) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found with id: " + cardId));

        if (card.getUsers().stream().noneMatch(user -> user.getId().equals(currentUser.getId()))) {
            throw new ApiException(FORBIDDEN, "You do not have access to this card.");
        }

        return card;
    }

    public static Specification<Card> getFilteredSpecification(
            String formTypeCode,
            String fromOrgName,
            String toOrgName,
            CardStatus cardStatus,
            Long userId,
            boolean attachedUser,
            Long formId
    ) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                Stream.of(
                        hasText(formTypeCode) ? criteriaBuilder.equal(root.get("cardType"), formTypeCode) : null,
                        formId != null ? criteriaBuilder.equal(root.join("form058", JoinType.LEFT).get("id"), formId) : null,
                        hasText(fromOrgName) ? criteriaBuilder.equal(root.get("form058").get("senderOrganization").get("name"), fromOrgName) : null,
                        hasText(toOrgName) ? criteriaBuilder.equal(root.get("form058").get("receiverOrganization").get("name"), toOrgName) : null,
                        cardStatus != null ? criteriaBuilder.equal(root.get("cardStatus"), cardStatus) : null,
                        (attachedUser && userId != null) ? criteriaBuilder.equal(root.get("users").get("id"), userId) : null
                ).filter(Objects::nonNull).toArray(Predicate[]::new)
        );
    }
}
