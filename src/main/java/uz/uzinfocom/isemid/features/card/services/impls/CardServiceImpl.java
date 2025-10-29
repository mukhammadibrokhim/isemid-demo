package uz.uzinfocom.isemid.features.card.services.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.services.ActBuildService;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardAssignActRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRejectRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.services.CardService;
import uz.uzinfocom.isemid.features.card.specifications.CardSpecification;
import uz.uzinfocom.isemid.features.card.strategy.impls.UpdateCardService;
import uz.uzinfocom.isemid.features.card.utils.CardValidationUtil;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.services.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final UpdateCardService updateCardService;
    private final MessageUtil messageUtil;
    private final ActBuildService actBuilder;
    private final UserService userService;
    private final CardValidationUtil cardValidationUtil;

    @Override
    public Page<Card> filter(
            int page, int size, String formTypeCode, String fromOrName,
            String toOrgName, CardStatus cardStatus, Long formId, String mkb10Code, User user
    ) {
        Specification<Card> spec = CardSpecification.build(formTypeCode, fromOrName, toOrgName, cardStatus, null, false, formId, mkb10Code,user);
        return cardRepository.findAll(spec, pageableSortByIdDesc(page, size));
    }

    @Override
    public Card getById(long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));

    }

    @Override
    @Transactional
    public Card update(long id, CardRequest cardRequest) {
        return updateCardService.update(id, cardRequest);
    }

    @Override
    public Card acceptCard(Long cardId, User currentUser) {
        Card card = cardValidationUtil.validateAndGetCard(cardId, currentUser);
        if (card.getStatus() != CardStatus.NEW && card.getStatus() != CardStatus.IN_PROGRESS) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.invalid_card_status", card.getStatus()));
        }
        card.setStatus(CardStatus.ACCEPTED_BY_USER);
        return cardRepository.save(card);
    }

    @Override
    public Card rejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard) {
        Card card = cardValidationUtil.validateAndGetCard(cardId, currentUser);
        if (card.getStatus() == CardStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.card.completed_cannot_be_rejected"));
        }
        Form058 form058 = card.getForm058();
        form058.setStatus(FormStatus.CARD_REJECTED);
        card.setStatus(CardStatus.REJECTED_BY_USER);
        card.setAttachedUserComment(rejectCard.getComment());
        return cardRepository.save(card);
    }

    @Override
    public Card saveAndComplete(Long cardId, User currentUser) {
        Card card = cardValidationUtil.validateAndGetCard(cardId, currentUser);
        if (card.getStatus() == CardStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.card.already_completed"));
        }
        card.setCompletedDate(LocalDate.now());
        card.setStatus(CardStatus.COMPLETED);
        return cardRepository.save(card);
    }

    @Override
    public Card supervisorApproveCard(long cardId, User currentUser) {
        Card card = this.getById(cardId);
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
        return cardRepository.save(card);
    }

    @Override
    public Card supervisorRejectCard(Long cardId, User currentUser, CardRejectRequest rejectCard) {
        Card card = this.getById(cardId);

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
        form058.setStatus(FormStatus.CARD_REJECTED);
        card.setStatus(CardStatus.REJECTED);
        card.setSupervisorComment(rejectCard.getComment());
        return cardRepository.save(card);
    }

    @Override
    public Card assignAct(CardAssignActRequest request, User user) {
        Card card = this.getById(request.getCardId());

        Map<Long, User> userMap = userService.getAllByIds(request.getAssignUserIds()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Act> acts = actBuilder.actBuilder(request.getActTypes(), card, user, userMap);
        card.getActs().addAll(acts);
        return cardRepository.save(card);
    }

    @Override
    public Page<Card> getAssignedCards(int page, int size, User currentUser) {
        Specification<Card> spec = CardSpecification.build(currentUser);
        return cardRepository.findAll(spec, pageableSortByIdDesc(page, size));
    }
}
