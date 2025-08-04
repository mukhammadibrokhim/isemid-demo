package uz.uzinfocom.isemid.features.card.services.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.PagedResponseUtil;
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
import uz.uzinfocom.isemid.features.card.specifications.CardSpecification;
import uz.uzinfocom.isemid.features.card.strategy.impls.UpdateCardService;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final UpdateCardService updateCardService;
    private final MessageUtil messageUtil;
    private final CardMapper cardMapper;
    private final ActFactory actFactory;

    @Override
    public PagedResponse<?> getAllCardsByFilter(int page, int size, String formTypeCode, String fromOrName, String toOrgName, CardStatus cardStatus, Long formId, User user, String fullURL) {
        Specification<Card> spec = CardSpecification.build(formTypeCode, fromOrName, toOrgName, cardStatus, null, false, formId);
        Page<Card> cardPage = cardRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return PagedResponseUtil.build(page, cardPage.map(cardMapper::toCardResponse), fullURL, messageUtil);
    }

    @Override
    public ApiResponse<?> getById(long id) {
        return ApiResponse.success(messageUtil.get("message.success"),
                cardMapper.toCardResponse(cardRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)))));
    }

    @Override
    @Transactional
    public ApiResponse<?> updateCard(long id, CardRequest cardRequest) {
        return updateCardService.updateCard(id, cardRequest);
    }

    @Override
    public ApiResponse<?> acceptCard(Long cardId, User currentUser) {
        Card card = validateAndGetCard(cardId, currentUser);

        if (card.getStatus() != CardStatus.NEW && card.getStatus() != CardStatus.IN_PROGRESS) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.invalid_card_status", card.getStatus()));
        }
        card.setStatus(CardStatus.ACCEPTED_BY_USER);
        return ApiResponse.success(messageUtil.get("message.success"), cardMapper.toCardResponse(cardRepository.save(card)));
    }

    @Override
    public ApiResponse<?> rejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard) {
        Card card = validateAndGetCard(cardId, currentUser);
        if (card.getStatus() == CardStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.card.completed_cannot_be_rejected"));
        }
        card.setStatus(CardStatus.REJECTED_BY_USER);
        card.setAttachedUserComment(rejectCard.getComment());
        return ApiResponse.success(messageUtil.get("message.success"), cardMapper.toCardResponse(cardRepository.save(card)));
    }

    @Override
    public PagedResponse<?> getCardUsers(int page, int size, Long userId, String fullURL) {
        Specification<Card> spec = CardSpecification.build(null, null, null, null, userId, true, null);
        Page<Card> cardPage = cardRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return PagedResponseUtil.build(page, cardPage.map(cardMapper::toCardResponse), fullURL, messageUtil);
    }

    @Override
    public ApiResponse<?> saveAndComplete(Long cardId, User currentUser) {
        Card card = validateAndGetCard(cardId, currentUser);
        if (card.getStatus() == CardStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.card.already_completed"));
        }
        Form058 form058 = card.getForm058();
        form058.setStatus(FormStatus.APPROVED_PENDING);
        card.setCompletedDate(LocalDate.now());
        card.setStatus(CardStatus.COMPLETED);
        return ApiResponse.success(messageUtil.get("message.success"), cardMapper.toCardResponse(cardRepository.save(card)));
    }

    @Override
    public ApiResponse<?> supervisorApproveCard(long cardId, User currentUser) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", cardId)));
        if (!card.getAssignedById().equals(currentUser.getId())) {
            throw new ApiException(FORBIDDEN, messageUtil.get("error.card.unauthorized_approve"));
        }
        if (card.getStatus().equals(CardStatus.APPROVED)) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.card.already_approved"));
        }
        if (!card.getStatus().equals(CardStatus.COMPLETED)) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.card.not_completed"));
        }
        card.setStatus(CardStatus.APPROVED);
        return ApiResponse.success(messageUtil.get("message.success"), cardMapper.toCardResponse(cardRepository.save(card)));
    }

    @Override
    public ApiResponse<?> supervisorRejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", cardId)));

        if (!card.getAssignedById().equals(currentUser.getId())) {
            throw new ApiException(FORBIDDEN, messageUtil.get("error.card.unauthorized_reject"));
        }

        if (card.getStatus() != CardStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.card.completed_cannot_be_rejected"));
        }

        if (rejectCard.getComment() == null || rejectCard.getComment().trim().isEmpty()) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.card.rejection_reason_required"));
        }
        Form058 form058 = card.getForm058();
        form058.setStatus(FormStatus.CARD_LINKED);
        card.setStatus(CardStatus.REJECTED);
        card.setSupervisorComment(rejectCard.getComment());
        return ApiResponse.success(messageUtil.get("message.success"), cardMapper.toCardResponse(cardRepository.save(card)));
    }

    @Override
    public ApiResponse<?> getCardActs(long cardId) {
        Card card = this.getCardById(cardId);
        return ApiResponse.success(messageUtil.get("message.success"), card.getActs());
    }

    @Override
    public ApiResponse<?> assignAct(CardAssignActRequest reqCardAssignAct, User user) {
        Card card = getCardById(reqCardAssignAct.getCardId());
        List<Act> acts = actBuilder(reqCardAssignAct.getActTypes(), card);
        card.getActs().addAll(acts);
        return ApiResponse.success(messageUtil.get("message.success"), cardMapper.toCardResponse(cardRepository.save(card)));
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
        return cardRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    private Card validateAndGetCard(long cardId, User currentUser) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", cardId)));

        if (card.getUsers().stream().noneMatch(user -> user.getId().equals(currentUser.getId()))) {
            throw new ApiException(FORBIDDEN, messageUtil.get("error.card.access_denied"));
        }

        return card;
    }
}
