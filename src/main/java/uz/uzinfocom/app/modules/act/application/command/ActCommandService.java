package uz.uzinfocom.app.modules.act.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.act.application.exception.ActNotFoundException;
import uz.uzinfocom.app.modules.act.application.exception.ActScopeViolationException;
import uz.uzinfocom.app.modules.act.application.exception.ActValidationException;
import uz.uzinfocom.app.modules.act.application.exception.InvalidActStatusException;
import uz.uzinfocom.app.modules.act.application.query.dto.ActDetailResponse;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActMapper;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActRepository;
import uz.uzinfocom.app.modules.act.web.dto.request.AssignActsRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.ReassignActUsersRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.UpdateActRequest;
import uz.uzinfocom.app.modules.card.application.exception.CardNotFoundException;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mirrors {@link uz.uzinfocom.app.modules.card.application.command.CardCommandService}'s
 * structure and status-transition rules exactly (see {@link ActStatus}) —
 * the same "assign blank shell(s), fill in later via update, supervisor
 * approves or rejects" pattern, applied to acts attached to a card instead
 * of cards attached to a form.
 */
@Service
@RequiredArgsConstructor
public class ActCommandService {

    private final ActRepository actRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final ActMapper actMapper;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Bulk-assigns one blank act per distinct requested {@code actType} to a
     * card, all sharing the same set of attached employees, with
     * {@code assignedById} set to whoever is calling this. Mirrors
     * {@code CardCommandService.assignCards} exactly.
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
        List<String> actTypes = request.actTypes().stream().filter(Objects::nonNull).distinct().toList();

        List<Act> acts = actTypes.stream()
                .map(actType -> {
                    Act act = new Act();
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
     * Only allowed while the attached user actually has the ball (accepted,
     * already in progress, or reworking after a supervisor rejection). Every
     * successful save moves the status to {@link ActStatus#IN_PROGRESS}.
     */
    @Transactional
    public ActDetailResponse update(Long actId, UpdateActRequest request) {
        Act act = actRepository.findById(actId)
                .orElseThrow(() -> new ActNotFoundException(actId));

        requireTransition(act.getActStatus().canBeUpdated(), act.getActStatus());

        act.setResultComment(request.resultComment());
        act.setActStatus(ActStatus.IN_PROGRESS);
        Act saved = actRepository.save(act);
        return actMapper.toDetailResponse(saved);
    }

    /**
     * Only the supervisor an act is already assigned to may hand it to
     * different employee(s) — replaces the users entirely and resets the
     * act to NEW so they go through the normal accept/reject cycle
     * themselves.
     */
    @Transactional
    public void reassignUsers(Long actId, ReassignActUsersRequest request) {
        Act act = requireAssignedSupervisorAct(actId);

        requireTransition(act.getActStatus().canBeReassigned(), act.getActStatus());

        Map<Long, User> userMap = resolveUsers(request.assignUserIds());

        act.setUsers(new HashSet<>(userMap.values()));
        act.setAttachedUserComment(null);
        act.setActStatus(ActStatus.NEW);
        actRepository.save(act);
    }

    /**
     * Only safe before any real data exists on the act.
     */
    @Transactional
    public void delete(Long actId) {
        Act act = actRepository.findById(actId)
                .orElseThrow(() -> new ActNotFoundException(actId));

        requireTransition(act.getActStatus().canBeDeleted(), act.getActStatus());

        actRepository.delete(act);
    }

    /**
     * The attached user marks the act as reviewed/correct.
     */
    @Transactional
    public void acceptByUser(Long actId) {
        Act act = requireAttachedUserAct(actId);
        requireTransition(act.getActStatus().canBeAcceptedByUser(), act.getActStatus());
        act.setActStatus(ActStatus.ACCEPTED_BY_USER);
        actRepository.save(act);
    }

    /**
     * The attached user flags the act as incorrect.
     */
    @Transactional
    public void rejectByUser(Long actId, String comment) {
        Act act = requireAttachedUserAct(actId);
        requireTransition(act.getActStatus().canBeRejectedByUser(), act.getActStatus());
        act.setActStatus(ActStatus.REJECTED_BY_USER);
        act.setAttachedUserComment(comment);
        actRepository.save(act);
    }

    /**
     * The attached user marks the act as finished, ready for supervisor
     * review.
     */
    @Transactional
    public void complete(Long actId) {
        Act act = requireAttachedUserAct(actId);
        requireTransition(act.getActStatus().canBeUpdated(), act.getActStatus());
        act.setCompletedDate(LocalDate.now());
        act.setActStatus(ActStatus.COMPLETED);
        actRepository.save(act);
    }

    /**
     * Only the supervisor the act was assigned to may approve it, and only
     * once it is completed.
     */
    @Transactional
    public void approveBySupervisor(Long actId) {
        Act act = requireAssignedSupervisorAct(actId);
        requireTransition(act.getActStatus().canBeApprovedBySupervisor(), act.getActStatus());
        act.setActStatus(ActStatus.APPROVED);
        actRepository.save(act);
    }

    /**
     * Only the supervisor the act was assigned to may reject it, only once
     * it is completed, and only with a non-blank reason.
     */
    @Transactional
    public void rejectBySupervisor(Long actId, String comment) {
        Act act = requireAssignedSupervisorAct(actId);
        requireTransition(act.getActStatus().canBeRejectedBySupervisor(), act.getActStatus());
        if (!StringUtils.hasText(comment)) {
            throw new ActValidationException("error.act.rejection-reason-required");
        }
        act.setActStatus(ActStatus.REJECTED);
        act.setSupervisorComment(comment);
        actRepository.save(act);
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

    private Act requireAssignedSupervisorAct(Long actId) {
        Act act = actRepository.findById(actId)
                .orElseThrow(() -> new ActNotFoundException(actId));

        Long userId = currentUserProvider.userIdOrNull();
        if (userId == null || !userId.equals(act.getAssignedById())) {
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
     * the whole operation if any id doesn't exist — shared by
     * {@link #assignActs} and {@link #reassignUsers}.
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
