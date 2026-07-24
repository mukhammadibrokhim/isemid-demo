package uz.uzinfocom.app.modules.act.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.act.application.exception.ActAlreadySentToLisException;
import uz.uzinfocom.app.modules.act.application.exception.ActNotFoundException;
import uz.uzinfocom.app.modules.act.application.exception.ActScopeViolationException;
import uz.uzinfocom.app.modules.act.application.exception.ActValidationException;
import uz.uzinfocom.app.modules.act.application.exception.InvalidActStatusException;
import uz.uzinfocom.app.modules.act.application.exception.UnsupportedActTypeException;
import uz.uzinfocom.app.modules.act.application.handler.ActTypeHandler;
import uz.uzinfocom.app.modules.act.application.handler.ActTypeHandlerRegistry;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.ActDetailResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActRepository;
import uz.uzinfocom.app.modules.act.web.dto.request.ActRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.AssignActsRequest;
import uz.uzinfocom.app.modules.card.application.exception.CardNotFoundException;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Transaction boundary and dispatch only — every type-specific decision
 * (which fields exist, how child sample/detail collections are synced) is
 * delegated to the {@link ActTypeHandler} resolved from the registry, the
 * same split {@code CardCommandService} uses for {@code Card}.
 * <p>
 * The act's status ({@link ActStatus}) moves forward through {@link #update}
 * (NEW/READY/SEND_FAILED -> IN_PROGRESS), {@link #markReady}
 * (IN_PROGRESS/SEND_FAILED -> READY), {@link #markSendingToLis}
 * (READY/SEND_FAILED -> SENT), and {@link #receiveLisResponse}
 * (SENT -> COMPLETED, called back by LIS once it has processed the act).
 * {@link #recordLisSendFailure} is the one step back
 * (SENT -> SEND_FAILED), taken when the send itself failed.
 * <p>
 * {@link #markSendingToLis}/{@link #recordLisSendSuccess}/
 * {@link #recordLisSendFailure} are deliberately three separate transactions
 * rather than one — the actual HTTP call to LIS happens between the first
 * and the other two, in {@code ActLisSendService}, and must not run inside
 * an open database transaction (see that class's javadoc).
 */
@Service
@RequiredArgsConstructor
public class ActCommandService {

    private final ActRepository actRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final ActTypeHandlerRegistry handlerRegistry;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Bulk-assigns one blank act per distinct requested {@code actType} to a
     * card, all sharing the same set of attached employees, with
     * {@code assignedById} recording whoever assigned it. This is the only
     * way acts get created — the actual field data is filled in afterwards
     * via {@link #update}.
     */
    @Transactional
    public void assignActs(Long cardId, AssignActsRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        Long assignedById = currentUserProvider.userIdOrNull();
        if (assignedById == null) {
            throw new ActScopeViolationException();
        }

        Map<Long, User> userMap = resolveUsers(request.assignUserIds());
        List<ActType> actTypes = request.actTypes().stream().filter(Objects::nonNull).distinct().toList();

        List<Act> acts = actTypes.stream()
                .map(actType -> {
                    Act act = handlerRegistry.get(actType).handleCreateBlank();
                    act.setActType(actType);
                    act.setActStatus(ActStatus.NEW);
                    act.setCard(card);
                    act.setUsers(new HashSet<>(userMap.values()));
                    act.setAssignedById(assignedById);
                    return act;
                })
                .toList();

        actRepository.saveAll(acts);
    }

    /**
     * Only the act's attached employee(s) may save it, and only before it
     * has been sent to LIS — freely, any number of times, regardless of
     * whether it's still {@link ActStatus#NEW}, already
     * {@link ActStatus#IN_PROGRESS}, or has been marked {@link ActStatus#READY}
     * (saving again moves it back to {@code IN_PROGRESS}, since it's no
     * longer exactly what was marked ready).
     */
    @Transactional
    public ActDetailResponse update(Long actId, ActRequest request) {
        Act act = requireAttachedUserAct(actId);

        requireTransition(canBeUpdated(act.getActStatus()), act.getActStatus());

        if (act.getActType() != request.type()) {
            throw new UnsupportedActTypeException(request.type());
        }

        ActTypeHandler<?, ?, ?> handler = handlerRegistry.get(request.type());
        handler.handleUpdate(act, request);
        act.setActStatus(ActStatus.IN_PROGRESS);
        Act saved = actRepository.save(act);
        return handler.handleToResponse(saved);
    }

    /**
     * The attached employee marks the act as finished filling in — the next
     * step is sending it to LIS. Also reachable straight from
     * {@link ActStatus#SEND_FAILED} for a plain retry (nothing to fix, e.g.
     * a network blip) without a redundant no-op edit first.
     */
    @Transactional
    public void markReady(Long actId) {
        Act act = requireAttachedUserAct(actId);
        requireTransition(
                act.getActStatus() == ActStatus.IN_PROGRESS || act.getActStatus() == ActStatus.SEND_FAILED,
                act.getActStatus()
        );
        act.setActStatus(ActStatus.READY);
        actRepository.save(act);
    }

    /**
     * First of the three LIS-send transactions: validates the caller and
     * status, records the attempt (bumping {@code lisInfo.attempt}, stamping
     * {@code lisInfo.sentDate}, clearing any previous
     * {@code lisInfo.lastError}), and optimistically moves the act to
     * {@link ActStatus#SENT} — before the actual HTTP call, so a second
     * concurrent send request is rejected by the status check instead of
     * racing this one. {@link #recordLisSendFailure} is the way back out if
     * the call that follows this doesn't succeed.
     *
     * <p>Returns the loaded act (full JOINED-inheritance subtype, with its
     * child collections) so the caller can build the LIS payload from it
     * without a second fetch.
     */
    @Transactional
    public Act markSendingToLis(Long actId) {
        Act act = requireAttachedUserAct(actId);
        requireTransition(
                act.getActStatus() == ActStatus.READY || act.getActStatus() == ActStatus.SEND_FAILED,
                act.getActStatus()
        );
        act.getLisInfo().markSendAttempt();
        act.setActStatus(ActStatus.SENT);
        return actRepository.save(act);
    }

    /**
     * Records LIS's own id for the act once it has accepted the submission.
     * The status is already {@link ActStatus#SENT} from
     * {@link #markSendingToLis} — this just attaches the correlation id.
     */
    @Transactional
    public void recordLisSendSuccess(Long actId, Long lisActId) {
        Act act = actRepository.findById(actId)
                .orElseThrow(() -> new ActNotFoundException(actId));
        act.getLisInfo().setActId(lisActId);
        actRepository.save(act);
    }

    /**
     * The send itself failed (network, upstream rejection, malformed
     * response) — moves the act back out of {@link ActStatus#SENT} into
     * {@link ActStatus#SEND_FAILED} and records why, so the attached
     * employee can fix the act (via {@link #update}) and try again.
     */
    @Transactional
    public void recordLisSendFailure(Long actId, String errorDescription) {
        Act act = actRepository.findById(actId)
                .orElseThrow(() -> new ActNotFoundException(actId));
        act.setActStatus(ActStatus.SEND_FAILED);
        act.getLisInfo().setLastError(errorDescription);
        actRepository.save(act);
    }

    /**
     * Called back by LIS once it has processed a sent act — stores its raw
     * response and moves the act to {@link ActStatus#COMPLETED}, which
     * concludes the act's lifecycle.
     */
    @Transactional
    public void receiveLisResponse(Long actId, Long lisActId, Map<String, Object> response) {
        Act act = actRepository.findById(actId)
                .orElseThrow(() -> new ActNotFoundException(actId));
        requireTransition(act.getActStatus() == ActStatus.SENT, act.getActStatus());

        act.getLisInfo().setActId(lisActId);
        act.getLisInfo().setResponse(response);
        act.setActStatus(ActStatus.COMPLETED);
        actRepository.save(act);
    }

    /**
     * Blocked once the act has already been sent to LIS — deleting it at
     * that point would leave the external system referencing a record that
     * no longer exists on our side. Soft delete only (mirrors
     * {@code DeleteForm058Service}): the row stays, marked via
     * {@code deleteInfo}, and {@link ActRepository#findActiveByIdForUpdate}
     * plus {@code ActSpecification} keep it out of further lookups/listings.
     */
    @Transactional
    public void delete(Long actId, String reason) {
        Act act = actRepository.findActiveByIdForUpdate(actId)
                .orElseThrow(() -> new ActNotFoundException(actId));

        if (act.getActStatus() == ActStatus.SENT || act.getActStatus() == ActStatus.COMPLETED) {
            throw new ActAlreadySentToLisException("error.act.already-sent-to-lis");
        }

        act.softDelete(currentUserProvider.userIdOrNull(), reason);
    }

    /**
     * NEW, IN_PROGRESS, READY, and SEND_FAILED all still precede a
     * successful send to LIS, so saving is allowed from any of them; once
     * SENT or COMPLETED, the act has left our hands.
     */
    private boolean canBeUpdated(ActStatus status) {
        return switch (status) {
            case NEW, IN_PROGRESS, READY, SEND_FAILED -> true;
            case SENT, COMPLETED -> false;
        };
    }

    private Act requireAttachedUserAct(Long actId) {
        Act act = actRepository.findById(actId)
                .orElseThrow(() -> new ActNotFoundException(actId));

        Long userId = currentUserProvider.userIdOrNull();
        boolean attached = userId != null && act.getUsers().stream()
                .anyMatch(user -> userId.equals(user.getId()));

        if (!attached) {
            throw new ActScopeViolationException();
        }
        return act;
    }

    private void requireTransition(boolean allowed, ActStatus current) {
        if (!allowed) {
            throw new InvalidActStatusException("error.act.invalid-status-transition", current);
        }
    }

    /**
     * Resolves distinct, non-null user ids into their entities, rejecting
     * the whole operation if any id doesn't exist.
     */
    private Map<Long, User> resolveUsers(List<Long> rawUserIds) {
        List<Long> userIds = rawUserIds.stream().filter(Objects::nonNull).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        if (userMap.size() != userIds.size()) {
            throw new ActValidationException("error.act.assign-user-not-found");
        }
        return userMap;
    }
}
