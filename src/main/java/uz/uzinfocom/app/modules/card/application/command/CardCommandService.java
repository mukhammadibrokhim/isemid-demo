package uz.uzinfocom.app.modules.card.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.card.application.exception.CardNotFoundException;
import uz.uzinfocom.app.modules.card.application.exception.CardScopeViolationException;
import uz.uzinfocom.app.modules.card.application.exception.CardValidationException;
import uz.uzinfocom.app.modules.card.application.exception.InvalidCardStatusException;
import uz.uzinfocom.app.modules.card.application.exception.UnsupportedCardTypeException;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandler;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandlerRegistry;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardDetailResponse;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;
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
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581NotFoundException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
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

    /**
     * Only the zoonotic/animal-bite investigation card types make sense for
     * a form0581 (rabies/animal-bite) case — CARD161 (general infectious
     * disease) and CARD_TUBE (TB dispensary) stay form058-only.
     */
    private static final Set<CardType> FORM0581_ALLOWED_TYPES = EnumSet.of(CardType.CARD174, CardType.CARD175, CardType.CARD205);

    private final CardRepository cardRepository;
    private final Form058JpaRepository form058Repository;
    private final Form0581JpaRepository form0581Repository;
    private final UserRepository userRepository;
    private final CardTypeHandlerRegistry handlerRegistry;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Bulk-assigns one blank card per distinct requested type to a form,
     * all sharing the same set of attached employees, with
     * {@code assignedById} set to whoever is calling this (the supervisor
     * who will later approve/reject the finished work) — mirrors the
     * legacy "assign card" step that precedes actual data entry, which
     * happens afterward via {@link #update}. This is the only way cards
     * get created — there is no separate "create one fully-populated card"
     * operation. Callers only need to know this succeeded (and that the
     * form is now CARD_LINKED) — the created cards themselves are not
     * returned; each assigned user finds theirs afterwards through
     * {@code GET /cards/mine}.
     */
    @Transactional
    public void assignCards(Long formId, AssignCardsRequest request) {
        Form058 form = form058Repository.findByIdAndDeletedFalse(formId)
                .orElseThrow(() -> new Form058NotFoundException(formId));

        List<Card> cards = createBlankCards(request, card -> card.setForm058(form));
        cardRepository.saveAll(cards);

        String oldStatus = form.getStatus().name();
        form.linkCards();
        form058Repository.save(form);

        eventPublisher.publishEvent(new StatusChangedEvent(
                AuditEntityType.FORM058, form.getId(), oldStatus, form.getStatus().name(),
                currentUserProvider.userIdOrNull(), null
        ));
    }

    /**
     * Same shape as {@link #assignCards}, but for a form0581 (rabies/animal-bite)
     * case — restricted to {@link #FORM0581_ALLOWED_TYPES}, and advances only
     * {@code Form0581.hasLinkedCards} rather than a {@code FormStatus}-style
     * status (form0581 has no CARD_LINKED-equivalent status).
     */
    @Transactional
    public void assignCardsToForm0581(Long form0581Id, AssignCardsRequest request) {
        Form0581 form = form0581Repository.findByIdAndDeletedFalse(form0581Id)
                .orElseThrow(() -> new Form0581NotFoundException(form0581Id));

        for (CardType cardType : request.cardTypes()) {
            if (cardType != null && !FORM0581_ALLOWED_TYPES.contains(cardType)) {
                throw new CardValidationException("error.card.unsupported-type-for-form0581", cardType);
            }
        }

        List<Card> cards = createBlankCards(request, card -> card.setForm0581(form));
        cardRepository.saveAll(cards);

        form.linkCards();
        form0581Repository.save(form);
    }

    /**
     * Shared "one blank card per distinct requested type, all sharing the
     * same attached employees" creation logic used by both {@link #assignCards}
     * and {@link #assignCardsToForm0581} — {@code attachToCase} is the only
     * difference between the two (which of {@code form058}/{@code form0581}
     * gets set on each created card).
     */
    private List<Card> createBlankCards(AssignCardsRequest request, Consumer<Card> attachToCase) {
        Long assignedById = currentUserProvider.userIdOrNull();
        if (assignedById == null) {
            throw new CardScopeViolationException();
        }

        Map<Long, User> userMap = resolveUsers(request.assignUserIds());
        List<CardType> cardTypes = request.cardTypes().stream().filter(Objects::nonNull).distinct().toList();

        return cardTypes.stream()
                .map(cardType -> {
                    Card card = handlerRegistry.get(cardType).handleCreateBlank();
                    attachToCase.accept(card);
                    card.setUsers(new HashSet<>(userMap.values()));
                    card.setAssignedById(assignedById);
                    return card;
                })
                .toList();
    }

    /**
     * Only allowed while the attached user actually has the ball (accepted,
     * already in progress, or reworking after a supervisor rejection) —
     * see {@link CardStatus#canBeUpdated()}. Before acceptance there's
     * nothing to edit yet; after the user's own rejection, the card needs
     * {@link #reassignUsers reassignment} first. Every successful save
     * moves the status to {@link CardStatus#IN_PROGRESS} — this is the
     * plain "Save" action; {@link #complete} is the separate "Save and
     * Complete" step that sends it to the supervisor. Returns the full
     * detail response so the caller sees the result of the edit without a
     * separate {@code GET} round-trip.
     */
    @Transactional
    public CardDetailResponse update(Long cardId, CardRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        requireTransition(card.getStatus().canBeUpdated(), card.getStatus());

        if (card.getCardType() != request.type()) {
            throw new UnsupportedCardTypeException(request.type());
        }

        CardTypeHandler<?, ?, ?> handler = handlerRegistry.get(request.type());
        handler.handleUpdate(card, request);
        card.setStatus(CardStatus.IN_PROGRESS);
        Card saved = cardRepository.save(card);
        return handler.handleToResponse(saved);
    }

    /**
     * Only the supervisor a card is already assigned to may hand it to
     * different employee(s) — replaces the users entirely and resets the
     * card to NEW so they go through the normal accept/reject cycle
     * themselves. {@code assignedById} stays the same supervisor; only the
     * attached employees change.
     */
    @Transactional
    public void reassignUsers(Long cardId, ReassignCardUsersRequest request) {
        Card card = requireAssignedSupervisorCard(cardId);

        requireTransition(card.getStatus().canBeReassigned(), card.getStatus());

        Map<Long, User> userMap = resolveUsers(request.assignUserIds());

        card.setUsers(new HashSet<>(userMap.values()));
        card.setAttachedUserComment(null);
        card.setStatus(CardStatus.NEW);
        cardRepository.save(card);
    }

    /**
     * Only safe before any real data exists on the card — see
     * {@link CardStatus#canBeDeleted()}. Once the attached user has saved
     * at least once (IN_PROGRESS) or the card has moved further along,
     * deleting it would destroy real work or a real supervisor decision.
     * Soft delete only (mirrors {@code DeleteForm058Service}/
     * {@code ActCommandService.delete}): the row stays, marked via
     * {@code deleteInfo}, so {@code existsByForm058_IdAndDeleteInfoDeletedFalse}
     * below excludes it rather than relying on the row being physically gone.
     */
    @Transactional
    public void delete(Long cardId, String reason) {
        Card card = cardRepository.findActiveByIdForUpdate(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        requireTransition(card.getStatus().canBeDeleted(), card.getStatus());

        Long formId = card.getForm058() != null ? card.getForm058().getId() : null;
        Long form0581Id = card.getForm0581() != null ? card.getForm0581().getId() : null;

        card.softDelete(currentUserProvider.userIdOrNull(), reason);
        cardRepository.flush();

        if (formId != null && !cardRepository.existsByForm058_IdAndDeleteInfoDeletedFalse(formId)) {
            Form058 form = form058Repository.findByIdAndDeletedFalse(formId)
                    .orElseThrow(() -> new Form058NotFoundException(formId));
            form.markCardsUnlinked();
            form058Repository.save(form);
        } else if (form0581Id != null && !cardRepository.existsByForm0581_IdAndDeleteInfoDeletedFalse(form0581Id)) {
            Form0581 form = form0581Repository.findByIdAndDeletedFalse(form0581Id)
                    .orElseThrow(() -> new Form0581NotFoundException(form0581Id));
            form.markCardsUnlinked();
            form0581Repository.save(form);
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

    private Card requireAttachedUserCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        Long userId = currentUserProvider.userIdOrNull();
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

        Long userId = currentUserProvider.userIdOrNull();
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
