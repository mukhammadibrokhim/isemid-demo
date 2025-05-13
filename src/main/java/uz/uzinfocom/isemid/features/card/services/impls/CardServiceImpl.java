package uz.uzinfocom.isemid.features.card.services.impls;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.factory.ActFactory;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardAssignActRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRejectRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.mappers.CardMapper;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.services.CardService;
import uz.uzinfocom.isemid.features.card.strategy.impls.UpdateCardService;
import uz.uzinfocom.isemid.features.card.utils.CardMessageUtil;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final UpdateCardService updateCardService;
    private final CardMapper cardMapper;
    private final ActFactory actFactory;

    @Override
    public ApiResponsePage getAllCardsByFilter(int page, int size, String formTypeCode, String fromOrName, String toOrgName, CardStatus cardStatus, Long formId, User user, String fullURL) {
        Specification<Card> spec = getFilteredSpecification(formTypeCode, fromOrName, toOrgName, cardStatus, null, false, formId);
        Page<Card> cardPage = cardRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, cardPage.map(cardMapper::toCardResponse), fullURL);
    }

    @Override
    public ApiResponse<?> getById(long id) {
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS,
                cardMapper.toCardResponse(cardRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND))));
    }

    @Transactional
    @Override
    public ApiResponse<?> updateCard(long id, CardRequest cardRequest, User currentUser) {
        return updateCardService.updateCard(id, cardRequest, currentUser);
    }

    @Override
    public ApiResponse<?> acceptCard(Long cardId, User currentUser) {
        Card card = validateAndGetCard(cardId, currentUser);

        if (card.getStatus() != CardStatus.NEW && card.getStatus() != CardStatus.IN_PROGRESS) {
            throw new ApiException(BAD_REQUEST, String.format(CardMessageUtil.INVALID_CARD_STATUS, card.getStatus()));
        }
        card.setStatus(CardStatus.ACCEPTED_BY_USER);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, cardMapper.toCardResponse(cardRepository.save(card)));
    }

    @Override
    public ApiResponse<?> rejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard) {
        Card card = validateAndGetCard(cardId, currentUser);
        if (card.getStatus() == CardStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, CardMessageUtil.COMPLETED_CANNOT_BE_REJECTED);
        }
        card.setStatus(CardStatus.REJECTED_BY_USER);
        card.setAttachedUserComment(rejectCard.getComment());
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, cardMapper.toCardResponse(cardRepository.save(card)));
    }

    @Override
    public ApiResponsePage getCardUsers(int page, int size, Long userId, String fullURL) {
        Specification<Card> spec = getFilteredSpecification(null, null, null, null, userId, true, null);
        Page<Card> cardPage = cardRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, cardPage.map(cardMapper::toCardResponse), fullURL);
    }

    @Override
    public ApiResponse<?> saveAndComplete(Long cardId, User currentUser) {
        Card card = validateAndGetCard(cardId, currentUser);
        if (card.getStatus() == CardStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, CardMessageUtil.CARD_ALREADY_COMPLETED);
        }
        Form058 form058 = card.getForm058();
        form058.setStatus(FormStatus.APPROVED_PENDING);
        card.setCompletedDate(LocalDate.now());
        card.setStatus(CardStatus.COMPLETED);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, cardMapper.toCardResponse(cardRepository.save(card)));
    }

    @Override
    public ApiResponse<?> supervisorApproveCard(long cardId, User currentUser) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found."));
        if (!card.getAssignedById().equals(currentUser.getId())) {
            throw new ApiException(FORBIDDEN, CardMessageUtil.UNAUTHORIZED_APPROVE);
        }
        if (card.getStatus().equals(CardStatus.APPROVED)) {
            throw new ApiException(BAD_REQUEST, CardMessageUtil.CARD_ALREADY_APPROVED);
        }
        if (!card.getStatus().equals(CardStatus.COMPLETED)) {
            throw new ApiException(BAD_REQUEST, CardMessageUtil.CARD_NOT_COMPLETED);
        }
        card.setStatus(CardStatus.APPROVED);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, cardMapper.toCardResponse(cardRepository.save(card)));
    }

    @Override
    public ApiResponse<?> supervisorRejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_RESOURCE, "Card", cardId)));

        if (!card.getAssignedById().equals(currentUser.getId())) {
            throw new ApiException(FORBIDDEN, CardMessageUtil.UNAUTHORIZED_REJECT);
        }

        if (card.getStatus() != CardStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, CardMessageUtil.COMPLETED_CANNOT_BE_REJECTED);
        }

        if (rejectCard.getComment() == null || rejectCard.getComment().trim().isEmpty()) {
            throw new ApiException(BAD_REQUEST, CardMessageUtil.REJECTION_REASON_REQUIRED);
        }
        Form058 form058 = card.getForm058();
        form058.setStatus(FormStatus.CARD_LINKED);
        card.setStatus(CardStatus.REJECTED);
        card.setSupervisorComment(rejectCard.getComment());
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, cardMapper.toCardResponse(cardRepository.save(card)));
    }

    @Override
    public ApiResponse<?> getCardActs(long cardId) {
        Card card = this.getCardById(cardId);
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS, card.getActs());
    }

    @Override
    public ApiResponse<?> assignAct(CardAssignActRequest reqCardAssignAct, User user) {
        Card card = getCardById(reqCardAssignAct.getCardId());
        List<Act> acts = actBuilder(reqCardAssignAct.getActTypes(), card);
        card.getActs().addAll(acts);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, cardMapper.toCardResponse(cardRepository.save(card)));
    }

    private List<Act> actBuilder(List<ActType> actTypes, Card card) {
        return actTypes.stream()
                .map(actType -> {
                    Act act = actFactory.createAct(actType);
                    act.setCard(card);
                    act.setActType(actType);
                    act.setActStatus(ActStatus.NEW);
                    return act;
                }).toList();
    }

    private Card getCardById(long id) {
        return cardRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_ID, id)));
    }

    private Card validateAndGetCard(long cardId, User currentUser) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_RESOURCE, "Card", cardId)));

        if (card.getUsers().stream().noneMatch(user -> user.getId().equals(currentUser.getId()))) {
            throw new ApiException(FORBIDDEN, CardMessageUtil.ACCESS_DENIED);
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
