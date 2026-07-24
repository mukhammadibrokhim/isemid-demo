package uz.uzinfocom.app.modules.act.application.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.act.application.exception.ActAlreadySentToLisException;
import uz.uzinfocom.app.modules.act.application.exception.ActScopeViolationException;
import uz.uzinfocom.app.modules.act.application.exception.InvalidActStatusException;
import uz.uzinfocom.app.modules.act.application.exception.UnsupportedActTypeException;
import uz.uzinfocom.app.modules.act.application.handler.ActTypeHandler;
import uz.uzinfocom.app.modules.act.application.handler.ActTypeHandlerRegistry;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.Act153DetailResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.ActDetailResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.domain.model.act153.Act153;
import uz.uzinfocom.app.modules.act.domain.model.act154.Act154;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActRepository;
import uz.uzinfocom.app.modules.act.web.dto.request.ActRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.Act153Request;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers the six-state {@link ActStatus} lifecycle: NEW/IN_PROGRESS/READY/
 * SEND_FAILED are all freely re-saveable via {@link ActCommandService#update}
 * (each save moves the act to IN_PROGRESS); {@link ActCommandService#markReady}
 * accepts IN_PROGRESS or SEND_FAILED -> READY; {@link ActCommandService#markSendingToLis}
 * accepts READY or SEND_FAILED -> SENT (the first of the three LIS-send
 * transactions — see that method's javadoc); {@link ActCommandService#recordLisSendSuccess}/
 * {@link ActCommandService#recordLisSendFailure} are the other two, taken
 * after the actual (here mocked-out) HTTP call; {@link ActCommandService#receiveLisResponse}
 * only accepts SENT -> COMPLETED; {@link ActCommandService#delete} is blocked
 * once an act has reached SENT or COMPLETED.
 */
class ActCommandServiceStatusTransitionTest {

    private static final Long ACT_ID = 1L;
    private static final Long ATTACHED_USER_ID = 10L;

    private ActRepository actRepository;
    private CurrentUserProvider currentUserProvider;
    private ActTypeHandlerRegistry handlerRegistry;
    private ActTypeHandler<?, ?, ?> act153Handler;
    private ActCommandService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        actRepository = mock(ActRepository.class);
        CardRepository cardRepository = mock(CardRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        currentUserProvider = mock(CurrentUserProvider.class);
        handlerRegistry = mock(ActTypeHandlerRegistry.class);
        act153Handler = mock(ActTypeHandler.class);

        service = new ActCommandService(actRepository, cardRepository, userRepository, handlerRegistry, currentUserProvider);

        when(actRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(act153Handler).when(handlerRegistry).get(ActType.ACT153);
        when(act153Handler.handleToResponse(any())).thenAnswer(invocation -> {
            Act act = invocation.getArgument(0);
            return new Act153DetailResponse(
                    act.getId(), act.getActType(), act.getActStatus(), null,
                    act.getAssignedById(), act.getResultComment(), null,
                    null, null, null, null, null, null,
                    null, null, null, null, null,
                    null, null, null, null, null, null,
                    null
            );
        });
    }

    @Test
    void updateRejectsAnUnattachedUser() {
        Act act = actWith(ActStatus.NEW, attachedUserId(ATTACHED_USER_ID));
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(999L);

        assertThatThrownBy(() -> service.update(ACT_ID, blankAct153Request()))
                .isInstanceOf(ActScopeViolationException.class);
    }

    @Test
    void updateRejectsMismatchedActType() {
        Act act = actWith154(ActStatus.NEW, attachedUserId(ATTACHED_USER_ID));
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.update(ACT_ID, blankAct153Request()))
                .isInstanceOf(UnsupportedActTypeException.class);
    }

    @Test
    void updateFromNewMovesToInProgress() {
        Act act = actWith(ActStatus.NEW, attachedUserId(ATTACHED_USER_ID));
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        ActDetailResponse response = service.update(ACT_ID, blankAct153Request());

        assertThat(act.getActStatus()).isEqualTo(ActStatus.IN_PROGRESS);
        assertThat(response.status()).isEqualTo(ActStatus.IN_PROGRESS);
    }

    @Test
    void updateIsAllowedFromNewInProgressReadyAndSendFailed() {
        for (ActStatus status : Set.of(ActStatus.NEW, ActStatus.IN_PROGRESS, ActStatus.READY, ActStatus.SEND_FAILED)) {
            Act act = actWith(status, attachedUserId(ATTACHED_USER_ID));
            givenAct(act);
            when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

            service.update(ACT_ID, blankAct153Request());

            assertThat(act.getActStatus()).isEqualTo(ActStatus.IN_PROGRESS);
        }
    }

    @Test
    void updateBlocksOnceSentToLis() {
        Act act = actWith(ActStatus.SENT, attachedUserId(ATTACHED_USER_ID));
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.update(ACT_ID, blankAct153Request()))
                .isInstanceOf(InvalidActStatusException.class);
    }

    @Test
    void markReadyMovesInProgressToReady() {
        Act act = actWith(ActStatus.IN_PROGRESS, attachedUserId(ATTACHED_USER_ID));
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        service.markReady(ACT_ID);

        assertThat(act.getActStatus()).isEqualTo(ActStatus.READY);
    }

    @Test
    void markReadyRejectsFromNew() {
        Act act = actWith(ActStatus.NEW, attachedUserId(ATTACHED_USER_ID));
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.markReady(ACT_ID))
                .isInstanceOf(InvalidActStatusException.class);
    }

    @Test
    void markSendingToLisMovesReadyToSentAndRecordsAttempt() {
        Act act = actWith(ActStatus.READY, attachedUserId(ATTACHED_USER_ID));
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        Act returned = service.markSendingToLis(ACT_ID);

        assertThat(returned).isSameAs(act);
        assertThat(act.getActStatus()).isEqualTo(ActStatus.SENT);
        assertThat(act.getLisInfo().getAttempt()).isEqualTo(1);
        assertThat(act.getLisInfo().getSentDate()).isNotNull();
    }

    @Test
    void markSendingToLisAlsoAllowedFromSendFailedAndClearsPreviousError() {
        Act act = actWith(ActStatus.SEND_FAILED, attachedUserId(ATTACHED_USER_ID));
        act.getLisInfo().setLastError("LIS_TIMEOUT");
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        service.markSendingToLis(ACT_ID);

        assertThat(act.getActStatus()).isEqualTo(ActStatus.SENT);
        assertThat(act.getLisInfo().getAttempt()).isEqualTo(1);
        assertThat(act.getLisInfo().getLastError()).isNull();
    }

    @Test
    void markSendingToLisRejectsFromInProgress() {
        Act act = actWith(ActStatus.IN_PROGRESS, attachedUserId(ATTACHED_USER_ID));
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.markSendingToLis(ACT_ID))
                .isInstanceOf(InvalidActStatusException.class);
    }

    @Test
    void recordLisSendSuccessAttachesLisActId() {
        Act act = actWith(ActStatus.SENT, Set.of());
        givenAct(act);

        service.recordLisSendSuccess(ACT_ID, 777L);

        assertThat(act.getLisInfo().getActId()).isEqualTo(777L);
        assertThat(act.getActStatus()).isEqualTo(ActStatus.SENT);
    }

    @Test
    void recordLisSendFailureMovesSentToSendFailedWithReason() {
        Act act = actWith(ActStatus.SENT, Set.of());
        givenAct(act);

        service.recordLisSendFailure(ACT_ID, "LIS_TIMEOUT (HTTP 504)");

        assertThat(act.getActStatus()).isEqualTo(ActStatus.SEND_FAILED);
        assertThat(act.getLisInfo().getLastError()).isEqualTo("LIS_TIMEOUT (HTTP 504)");
    }

    @Test
    void updateIsAllowedFromSendFailed() {
        Act act = actWith(ActStatus.SEND_FAILED, attachedUserId(ATTACHED_USER_ID));
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        service.update(ACT_ID, blankAct153Request());

        assertThat(act.getActStatus()).isEqualTo(ActStatus.IN_PROGRESS);
    }

    @Test
    void markReadyAlsoAllowedFromSendFailed() {
        Act act = actWith(ActStatus.SEND_FAILED, attachedUserId(ATTACHED_USER_ID));
        givenAct(act);
        when(currentUserProvider.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        service.markReady(ACT_ID);

        assertThat(act.getActStatus()).isEqualTo(ActStatus.READY);
    }

    @Test
    void deleteSucceedsWhileSendFailed() {
        Act act = actWith(ActStatus.SEND_FAILED, Set.of());
        givenAct(act);

        service.delete(ACT_ID, "no longer needed");

        assertThat(act.isDeleted()).isTrue();
    }

    @Test
    void receiveLisResponseMovesSentToCompleted() {
        Act act = actWith(ActStatus.SENT, attachedUserId(ATTACHED_USER_ID));
        givenAct(act);

        service.receiveLisResponse(ACT_ID, 555L, Map.of("result", "ok"));

        assertThat(act.getActStatus()).isEqualTo(ActStatus.COMPLETED);
        assertThat(act.getLisInfo().getActId()).isEqualTo(555L);
        assertThat(act.getLisInfo().getResponse()).containsEntry("result", "ok");
    }

    @Test
    void receiveLisResponseRejectsFromReady() {
        Act act = actWith(ActStatus.READY, attachedUserId(ATTACHED_USER_ID));
        givenAct(act);

        assertThatThrownBy(() -> service.receiveLisResponse(ACT_ID, 555L, Map.of()))
                .isInstanceOf(InvalidActStatusException.class);
    }

    @Test
    void deleteSucceedsWhileStillNew() {
        Act act = actWith(ActStatus.NEW, Set.of());
        givenAct(act);

        service.delete(ACT_ID, "no longer needed");

        assertThat(act.isDeleted()).isTrue();
    }

    @Test
    void deleteSucceedsWhileReady() {
        Act act = actWith(ActStatus.READY, Set.of());
        givenAct(act);

        service.delete(ACT_ID, "no longer needed");

        assertThat(act.isDeleted()).isTrue();
    }

    @Test
    void deleteBlocksOnceSentToLis() {
        Act act = actWith(ActStatus.SENT, Set.of());
        givenAct(act);

        assertThatThrownBy(() -> service.delete(ACT_ID, "no longer needed"))
                .isInstanceOf(ActAlreadySentToLisException.class);

        assertThat(act.isDeleted()).isFalse();
    }

    @Test
    void deleteBlocksOnceCompleted() {
        Act act = actWith(ActStatus.COMPLETED, Set.of());
        givenAct(act);

        assertThatThrownBy(() -> service.delete(ACT_ID, "no longer needed"))
                .isInstanceOf(ActAlreadySentToLisException.class);

        assertThat(act.isDeleted()).isFalse();
    }

    private void givenAct(Act act) {
        when(actRepository.findById(ACT_ID)).thenReturn(Optional.of(act));
        when(actRepository.findActiveByIdForUpdate(ACT_ID)).thenReturn(Optional.of(act));
    }

    private Act actWith(ActStatus status, Set<User> users) {
        Act act = new Act153();
        act.setActType(ActType.ACT153);
        act.setActStatus(status);
        act.setUsers(users);
        return act;
    }

    private Act actWith154(ActStatus status, Set<User> users) {
        Act act = new Act154();
        act.setActType(ActType.ACT154);
        act.setActStatus(status);
        act.setUsers(users);
        return act;
    }

    private ActRequest blankAct153Request() {
        return new Act153Request(
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null
        );
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
