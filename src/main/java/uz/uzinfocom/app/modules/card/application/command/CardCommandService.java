package uz.uzinfocom.app.modules.card.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.card.application.exception.CardNotFoundException;
import uz.uzinfocom.app.modules.card.application.exception.CardScopeViolationException;
import uz.uzinfocom.app.modules.card.application.exception.CardValidationException;
import uz.uzinfocom.app.modules.card.application.exception.InvalidCardStatusException;
import uz.uzinfocom.app.modules.card.application.exception.UnsupportedCardTypeException;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandler;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandlerRegistry;
import uz.uzinfocom.app.modules.card.application.shared.CurrentCardUser;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.modules.card.web.dto.request.AssignCardsRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.CardRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.ReassignCardUsersRequest;
import uz.uzinfocom.app.modules.form058.application.exception.Form058NotFoundException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Transaction boundary and dispatch only — every type-specific decision is
 * delegated to the {@link CardTypeHandler} resolved from the registry.
 * Status-transition rules below are extracted from the legacy
 * {@code CardServiceImpl} (see the plan document for the source trace),
 * expressed through {@link CardStatus}'s predicate methods instead of
 * inline {@code if} chains.
 */
@Service
@RequiredArgsConstructor
public class CardCommandService {

    private final CardRepository cardRepository;
    private final Form058JpaRepository form058Repository;
    private final UserRepository userRepository;
    private final CardTypeHandlerRegistry handlerRegistry;
    private final CurrentCardUser currentCardUser;

    @Transactional
    public Card create(Long formId, CardRequest request) {
        Form058 form = form058Repository.findByIdAndDeletedFalse(formId)
                .orElseThrow(() -> new Form058NotFoundException(formId));

        CardTypeHandler<?, ?, ?> handler = handlerRegistry.get(request.type());
        Card card = handler.handleCreate(form, request);
        Card saved = cardRepository.save(card);

        form.linkCards();
        form058Repository.save(form);

        return saved;
    }

    /**
     * Bulk-assigns one blank card per distinct requested type to a form,
     * all sharing the same set of attached employees, with
     * {@code assignedById} set to whoever is calling this (the supervisor
     * who will later approve/reject the finished work) — mirrors the
     * legacy "assign card" step that precedes actual data entry, which
     * happens afterward via {@link #update}. Callers only need to know
     * this succeeded (and that the form is now CARD_LINKED) — the created
     * cards themselves are not returned; each assigned user finds theirs
     * afterwards through {@code GET /cards/mine}.
     */
    @Transactional
    public void assignCards(Long formId, AssignCardsRequest request) {
        Form058 form = form058Repository.findByIdAndDeletedFalse(formId)
                .orElseThrow(() -> new Form058NotFoundException(formId));

        Long assignedById = currentCardUser.userIdOrNull();
        if (assignedById == null) {
            throw new CardScopeViolationException();
        }

        Map<Long, User> userMap = resolveUsers(request.assignUserIds());
        List<CardType> cardTypes = request.cardTypes().stream().filter(Objects::nonNull).distinct().toList();

        List<Card> cards = cardTypes.stream()
                .map(cardType -> {
                    Card card = handlerRegistry.get(cardType).handleCreateBlank();
                    card.setForm058(form);
                    card.setUsers(new HashSet<>(userMap.values()));
                    card.setAssignedById(assignedById);
                    return card;
                })
                .toList();

        cardRepository.saveAll(cards);

        form.linkCards();
        form058Repository.save(form);
    }

    /**
     * Only allowed while the attached user actually has the ball (accepted,
     * or reworking after a supervisor rejection) — see
     * {@link CardStatus#canBeUpdated()}. Before acceptance there's nothing
     * to edit yet; after the user's own rejection, the card needs
     * {@link #reassignUsers reassignment} first.
     */
    @Transactional
    public void update(Long cardId, CardRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        requireTransition(card.getStatus().canBeUpdated(), card.getStatus());

        if (card.getCardType() != request.type()) {
            throw new UnsupportedCardTypeException(request.type());
        }

        CardTypeHandler<?, ?, ?> handler = handlerRegistry.get(request.type());
        handler.handleUpdate(card, request);
        cardRepository.save(card);
    }

    /**
     * Hands a card the attached user rejected to different employee(s) —
     * replaces the users entirely and resets the card to NEW so they go
     * through the normal accept/reject cycle themselves. Whoever performs
     * the reassignment becomes the new {@code assignedById} (the
     * supervisor who will review the eventual submission), same as the
     * original {@link #assignCards}.
     */
    @Transactional
    public void reassignUsers(Long cardId, ReassignCardUsersRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        requireTransition(card.getStatus().canBeReassigned(), card.getStatus());

        Long assignedById = currentCardUser.userIdOrNull();
        if (assignedById == null) {
            throw new CardScopeViolationException();
        }

        Map<Long, User> userMap = resolveUsers(request.assignUserIds());

        card.setUsers(new HashSet<>(userMap.values()));
        card.setAssignedById(assignedById);
        card.setAttachedUserComment(null);
        card.setStatus(CardStatus.NEW);
        cardRepository.save(card);
    }

    @Transactional
    public void delete(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        Long formId = card.getForm058().getId();

        cardRepository.delete(card);
        cardRepository.flush();

        if (!cardRepository.existsByForm058_Id(formId)) {
            Form058 form = form058Repository.findByIdAndDeletedFalse(formId)
                    .orElseThrow(() -> new Form058NotFoundException(formId));
            form.markCardsUnlinked();
            form058Repository.save(form);
        }
    }

    /**
     * The attached user marks the card as reviewed/correct.
     */
    @Transactional
    public void acceptByUser(Long cardId) {
        Card card = requireAttachedUserCard(cardId);
        requireTransition(card.getStatus().canBeAcceptedByUser(), card.getStatus());
        card.setStatus(CardStatus.ACCEPTED_BY_USER);
        cardRepository.save(card);
    }

    /**
     * The attached user flags the card as incorrect.
     */
    @Transactional
    public void rejectByUser(Long cardId, String comment) {
        Card card = requireAttachedUserCard(cardId);
        requireTransition(card.getStatus().canBeRejectedByUser(), card.getStatus());
        card.setStatus(CardStatus.REJECTED_BY_USER);
        card.setAttachedUserComment(comment);
        cardRepository.save(card);
    }

    /**
     * The attached user marks the card as finished, ready for supervisor
     * review.
     */
    @Transactional
    public void complete(Long cardId) {
        Card card = requireAttachedUserCard(cardId);
        requireTransition(card.getStatus().canBeUpdated(), card.getStatus());
        card.setCompletedDate(LocalDate.now());
        card.setStatus(CardStatus.COMPLETED);
        cardRepository.save(card);
    }

    /**
     * Only the supervisor the card was assigned to may approve it, and only
     * once it is completed.
     */
    @Transactional
    public void approveBySupervisor(Long cardId) {
        Card card = requireAssignedSupervisorCard(cardId);
        requireTransition(card.getStatus().canBeApprovedBySupervisor(), card.getStatus());
        card.setStatus(CardStatus.APPROVED);
        cardRepository.save(card);
    }

    /**
     * Only the supervisor the card was assigned to may reject it, only once
     * it is completed, and only with a non-blank reason.
     */
    @Transactional
    public void rejectBySupervisor(Long cardId, String comment) {
        Card card = requireAssignedSupervisorCard(cardId);
        requireTransition(card.getStatus().canBeRejectedBySupervisor(), card.getStatus());
        if (!StringUtils.hasText(comment)) {
            throw new CardValidationException("error.card.rejection-reason-required");
        }
        card.setStatus(CardStatus.REJECTED);
        card.setSupervisorComment(comment);
        cardRepository.save(card);
    }

    /**
     * Attaches a new act to the card. Unlike the status transitions above,
     * legacy applies no status check here — an act may be assigned
     * regardless of the card's current state.
     */
    @Transactional
    public void assignAct(Long cardId, String actType) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        Act act = new Act();
        act.setCard(card);
        act.setActType(actType);
        act.setActStatus(ActStatus.NEW);
        card.getActs().add(act);

        cardRepository.save(card);
    }

    private Card requireAttachedUserCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        Long userId = currentCardUser.userIdOrNull();
        boolean attached = userId != null && card.getUsers().stream()
                .anyMatch(user -> userId.equals(user.getId()));

        if (!attached) {
            throw new CardScopeViolationException();
        }
        return card;
    }

    private Card requireAssignedSupervisorCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        Long userId = currentCardUser.userIdOrNull();
        if (userId == null || !userId.equals(card.getAssignedById())) {
            throw new CardScopeViolationException();
        }
        return card;
    }

    private void requireTransition(boolean allowed, CardStatus current) {
        if (!allowed) {
            throw new InvalidCardStatusException("error.card.invalid-status-transition", current);
        }
    }

    /**
     * Resolves distinct, non-null user ids into their entities, rejecting
     * the whole operation if any id doesn't exist — shared by
     * {@link #assignCards} and {@link #reassignUsers}.
     */
    private Map<Long, User> resolveUsers(List<Long> rawUserIds) {
        List<Long> userIds = rawUserIds.stream().filter(Objects::nonNull).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        if (userMap.size() != userIds.size()) {
            throw new CardValidationException("error.card.assign-user-not-found");
        }
        return userMap;
    }
}
