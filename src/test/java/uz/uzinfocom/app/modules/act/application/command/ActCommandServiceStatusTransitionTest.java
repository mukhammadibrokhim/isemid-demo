package uz.uzinfocom.app.modules.act.application.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.act.application.exception.ActScopeViolationException;
import uz.uzinfocom.app.modules.act.application.exception.ActValidationException;
import uz.uzinfocom.app.modules.act.application.exception.InvalidActStatusException;
import uz.uzinfocom.app.modules.act.application.query.dto.ActDetailResponse;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActMapper;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActRepository;
import uz.uzinfocom.app.modules.act.web.dto.request.ReassignActUsersRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.UpdateActRequest;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mirrors {@code CardCommandServiceStatusTransitionTest} — the same
 * status-transition rules, expressed through {@link ActStatus}'s predicate
 * methods, applied to {@link Act} instead of {@code Card}. Unlike Card,
 * {@code update()} here is a plain field assignment (no per-type handler
 * dispatch), so it's covered directly rather than through a real mapper.
 */
class ActCommandServiceStatusTransitionTest {

    private static final Long ACT_ID = 1L;
    private static final Long ATTACHED_USER_ID = 10L;
    private static final Long SUPERVISOR_ID = 20L;

    private ActRepository actRepository;
    private UserRepository userRepository;
    private CurrentUserProvider currentUserProvider;
    private ActCommandService service;

    @BeforeEach
    void setUp() {
        actRepository = mock(ActRepository.class);
        CardRepository cardRepository = mock(CardRepository.class);
        userRepository = mock(UserRepository.class);
        currentUserProvider = mock(CurrentUserProvider.class);
        ActMapper actMapper = mock(ActMapper.class);

        service = new ActCommandService(actRepository, cardRepository, userRepository, actMapper, currentUserProvider);

        when(actRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(actMapper.toDetailResponse(any())).thenAnswer(invocation -> {
            Act act = invocation.getArgument(0);
            return new ActDetailResponse(act.getId(), act.getActType(), act.getActStatus(), null,
                    act.getAssignedById(), act.getSupervisorComment(), act.getAttachedUserComment(),
                    act.getCompletedDate(), act.getResultComment());
        });
    }

    @Test
    void acceptByUserSucceedsForAttachedUserOnNewAct() {
        Act act = actWith(ActStatus.NEW, attachedUserId(ATTACHED_USER_ID), null);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        service.acceptByUser(ACT_ID);

        assertThat(act.getActStatus()).isEqualTo(ActStatus.ACCEPTED_BY_USER);
    }

    @Test
    void acceptByUserRejectsUnattachedUser() {
        Act act = actWith(ActStatus.NEW, attachedUserId(ATTACHED_USER_ID), null);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(999L);

        assertThatThrownBy(() -> service.acceptByUser(ACT_ID))
                .isInstanceOf(ActScopeViolationException.class);
    }

    @Test
    void acceptByUserRejectsAlreadyCompletedAct() {
        Act act = actWith(ActStatus.COMPLETED, attachedUserId(ATTACHED_USER_ID), null);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.acceptByUser(ACT_ID))
                .isInstanceOf(InvalidActStatusException.class);
    }

    @Test
    void rejectByUserSetsCommentAndStatus() {
        Act act = actWith(ActStatus.ACCEPTED_BY_USER, attachedUserId(ATTACHED_USER_ID), null);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        service.rejectByUser(ACT_ID, "Wrong data");

        assertThat(act.getActStatus()).isEqualTo(ActStatus.REJECTED_BY_USER);
        assertThat(act.getAttachedUserComment()).isEqualTo("Wrong data");
    }

    @Test
    void rejectByUserRejectsOnceInProgress() {
        Act act = actWith(ActStatus.IN_PROGRESS, attachedUserId(ATTACHED_USER_ID), null);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.rejectByUser(ACT_ID, "too late"))
                .isInstanceOf(InvalidActStatusException.class);
    }

    @Test
    void completeSetsCompletedDateAndStatus() {
        Act act = actWith(ActStatus.ACCEPTED_BY_USER, attachedUserId(ATTACHED_USER_ID), null);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        service.complete(ACT_ID);

        assertThat(act.getActStatus()).isEqualTo(ActStatus.COMPLETED);
        assertThat(act.getCompletedDate()).isNotNull();
    }

    @Test
    void completeRejectsBeforeAcceptance() {
        Act act = actWith(ActStatus.NEW, attachedUserId(ATTACHED_USER_ID), null);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.complete(ACT_ID))
                .isInstanceOf(InvalidActStatusException.class);
    }

    @Test
    void completeAllowsReworkAfterSupervisorRejection() {
        Act act = actWith(ActStatus.REJECTED, attachedUserId(ATTACHED_USER_ID), SUPERVISOR_ID);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        service.complete(ACT_ID);

        assertThat(act.getActStatus()).isEqualTo(ActStatus.COMPLETED);
    }

    @Test
    void completeRejectsAlreadyApprovedAct() {
        Act act = actWith(ActStatus.APPROVED, attachedUserId(ATTACHED_USER_ID), SUPERVISOR_ID);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.complete(ACT_ID))
                .isInstanceOf(InvalidActStatusException.class);
    }

    @Test
    void approveBySupervisorSucceedsForAssignedSupervisorOnCompletedAct() {
        Act act = actWith(ActStatus.COMPLETED, Set.of(), SUPERVISOR_ID);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(SUPERVISOR_ID);

        service.approveBySupervisor(ACT_ID);

        assertThat(act.getActStatus()).isEqualTo(ActStatus.APPROVED);
    }

    @Test
    void approveBySupervisorRejectsWrongSupervisor() {
        Act act = actWith(ActStatus.COMPLETED, Set.of(), SUPERVISOR_ID);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(999L);

        assertThatThrownBy(() -> service.approveBySupervisor(ACT_ID))
                .isInstanceOf(ActScopeViolationException.class);
    }

    @Test
    void rejectBySupervisorRequiresNonBlankComment() {
        Act act = actWith(ActStatus.COMPLETED, Set.of(), SUPERVISOR_ID);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(SUPERVISOR_ID);

        assertThatThrownBy(() -> service.rejectBySupervisor(ACT_ID, "   "))
                .isInstanceOf(ActValidationException.class);
    }

    @Test
    void rejectBySupervisorSetsCommentAndStatus() {
        Act act = actWith(ActStatus.COMPLETED, Set.of(), SUPERVISOR_ID);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(SUPERVISOR_ID);

        service.rejectBySupervisor(ACT_ID, "Incomplete investigation");

        assertThat(act.getActStatus()).isEqualTo(ActStatus.REJECTED);
        assertThat(act.getSupervisorComment()).isEqualTo("Incomplete investigation");
    }

    @Test
    void updateBlocksEditingAnApprovedAct() {
        Act act = actWith(ActStatus.APPROVED, attachedUserId(ATTACHED_USER_ID), SUPERVISOR_ID);
        givenAct(act);

        assertThatThrownBy(() -> service.update(ACT_ID, new UpdateActRequest("done")))
                .isInstanceOf(InvalidActStatusException.class);
    }

    @Test
    void updateBlocksEditingBeforeAcceptance() {
        Act act = actWith(ActStatus.NEW, attachedUserId(ATTACHED_USER_ID), null);
        givenAct(act);

        assertThatThrownBy(() -> service.update(ACT_ID, new UpdateActRequest("done")))
                .isInstanceOf(InvalidActStatusException.class);
    }

    @Test
    void updateBlocksEditingAfterUserRejection() {
        Act act = actWith(ActStatus.REJECTED_BY_USER, attachedUserId(ATTACHED_USER_ID), null);
        givenAct(act);

        assertThatThrownBy(() -> service.update(ACT_ID, new UpdateActRequest("done")))
                .isInstanceOf(InvalidActStatusException.class);
    }

    @Test
    void updateMovesStatusToInProgressOnASuccessfulSave() {
        Act act = actWith(ActStatus.ACCEPTED_BY_USER, attachedUserId(ATTACHED_USER_ID), null);
        givenAct(act);

        ActDetailResponse response = service.update(ACT_ID, new UpdateActRequest("Sample tested negative"));

        assertThat(act.getActStatus()).isEqualTo(ActStatus.IN_PROGRESS);
        assertThat(act.getResultComment()).isEqualTo("Sample tested negative");
        assertThat(response.status()).isEqualTo(ActStatus.IN_PROGRESS);
    }

    @Test
    void updateAllowsReworkAfterSupervisorRejection() {
        Act act = actWith(ActStatus.REJECTED, attachedUserId(ATTACHED_USER_ID), SUPERVISOR_ID);
        givenAct(act);

        service.update(ACT_ID, new UpdateActRequest("Retested"));

        assertThat(act.getActStatus()).isEqualTo(ActStatus.IN_PROGRESS);
    }

    @Test
    void deleteBlocksOnceRealDataExistsOrTheActHasMovedOn() {
        for (ActStatus status : List.of(ActStatus.IN_PROGRESS, ActStatus.COMPLETED, ActStatus.APPROVED, ActStatus.REJECTED)) {
            Act act = actWith(status, Set.of(), null);
            givenAct(act);

            assertThatThrownBy(() -> service.delete(ACT_ID))
                    .isInstanceOf(InvalidActStatusException.class);
        }
    }

    @Test
    void deleteSucceedsBeforeAnyRealDataExists() {
        for (ActStatus status : List.of(ActStatus.NEW, ActStatus.ACCEPTED_BY_USER, ActStatus.REJECTED_BY_USER)) {
            Act act = actWith(status, Set.of(), null);
            givenAct(act);

            service.delete(ACT_ID);
        }
    }

    @Test
    void reassignUsersSucceedsForTheAssignedSupervisorOnAUserRejectedAct() {
        Act act = actWith(ActStatus.REJECTED_BY_USER, Set.of(), SUPERVISOR_ID);
        act.setAttachedUserComment("Wrong data");
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(SUPERVISOR_ID);

        User newEmployee = userWithId(99L);
        when(userRepository.findAllById(List.of(99L))).thenReturn(List.of(newEmployee));

        service.reassignUsers(ACT_ID, new ReassignActUsersRequest(List.of(99L)));

        assertThat(act.getActStatus()).isEqualTo(ActStatus.NEW);
        assertThat(act.getUsers()).containsExactly(newEmployee);
        assertThat(act.getAssignedById()).isEqualTo(SUPERVISOR_ID);
        assertThat(act.getAttachedUserComment()).isNull();
    }

    @Test
    void reassignUsersRejectsWrongSupervisor() {
        Act act = actWith(ActStatus.REJECTED_BY_USER, Set.of(), SUPERVISOR_ID);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(999L);

        assertThatThrownBy(() -> service.reassignUsers(ACT_ID, new ReassignActUsersRequest(List.of(1L))))
                .isInstanceOf(ActScopeViolationException.class);
    }

    @Test
    void reassignUsersRejectsActNotYetRejectedByUser() {
        Act act = actWith(ActStatus.NEW, Set.of(), SUPERVISOR_ID);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(SUPERVISOR_ID);

        assertThatThrownBy(() -> service.reassignUsers(ACT_ID, new ReassignActUsersRequest(List.of(1L))))
                .isInstanceOf(InvalidActStatusException.class);
    }

    @Test
    void reassignUsersRejectsUnknownUserId() {
        Act act = actWith(ActStatus.REJECTED_BY_USER, Set.of(), SUPERVISOR_ID);
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(SUPERVISOR_ID);
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.reassignUsers(ACT_ID, new ReassignActUsersRequest(List.of(1L))))
                .isInstanceOf(ActValidationException.class);
    }

    private void givenAct(Act act) {
        when(actRepository.findById(ACT_ID)).thenReturn(Optional.of(act));
    }

    private Act actWith(ActStatus status, Set<User> users, Long assignedById) {
        Act act = new Act();
        act.setActType("ACT153");
        act.setActStatus(status);
        act.setUsers(users);
        act.setAssignedById(assignedById);
        return act;
    }

    private Set<User> attachedUserId(Long userId) {
        return Set.of(userWithId(userId));
    }

    private User userWithId(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
