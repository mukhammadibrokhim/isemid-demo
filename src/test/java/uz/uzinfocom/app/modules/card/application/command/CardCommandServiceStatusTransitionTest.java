package uz.uzinfocom.app.modules.card.application.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.card.application.exception.CardScopeViolationException;
import uz.uzinfocom.app.modules.card.application.exception.CardValidationException;
import uz.uzinfocom.app.modules.card.application.exception.InvalidCardStatusException;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandlerRegistry;
import uz.uzinfocom.app.modules.card.application.shared.CurrentCardUser;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.domain.model.card161.Card161;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.modules.card.web.dto.request.ReassignCardUsersRequest;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers the status-transition rules extracted from the legacy
 * {@code CardServiceImpl} (accept/reject-by-user, complete, supervisor
 * approve/reject) and the assign-act flow, all now expressed through
 * {@link CardStatus}'s predicate methods instead of inline {@code if}
 * chains.
 */
class CardCommandServiceStatusTransitionTest {

    private static final Long CARD_ID = 1L;
    private static final Long ATTACHED_USER_ID = 10L;
    private static final Long SUPERVISOR_ID = 20L;

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private CardTypeHandlerRegistry handlerRegistry;
    private CurrentCardUser currentCardUser;
    private CardCommandService service;

    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);
        currentCardUser = mock(CurrentCardUser.class);
        Form058JpaRepository form058Repository = mock(Form058JpaRepository.class);
        userRepository = mock(UserRepository.class);
        handlerRegistry = mock(CardTypeHandlerRegistry.class);

        service = new CardCommandService(cardRepository, form058Repository, userRepository, handlerRegistry, currentCardUser);

        when(cardRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void acceptByUserSucceedsForAttachedUserOnNewCard() {
        Card card = cardWith(CardStatus.NEW, attachedUserId(ATTACHED_USER_ID), null);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        service.acceptByUser(CARD_ID);

        assertThat(card.getStatus()).isEqualTo(CardStatus.ACCEPTED_BY_USER);
    }

    @Test
    void acceptByUserRejectsUnattachedUser() {
        Card card = cardWith(CardStatus.NEW, attachedUserId(ATTACHED_USER_ID), null);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(999L);

        assertThatThrownBy(() -> service.acceptByUser(CARD_ID))
                .isInstanceOf(CardScopeViolationException.class);
    }

    @Test
    void acceptByUserRejectsAlreadyCompletedCard() {
        Card card = cardWith(CardStatus.COMPLETED, attachedUserId(ATTACHED_USER_ID), null);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.acceptByUser(CARD_ID))
                .isInstanceOf(InvalidCardStatusException.class);
    }

    @Test
    void rejectByUserSetsCommentAndStatus() {
        Card card = cardWith(CardStatus.IN_PROGRESS, attachedUserId(ATTACHED_USER_ID), null);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        service.rejectByUser(CARD_ID, "Wrong data");

        assertThat(card.getStatus()).isEqualTo(CardStatus.REJECTED_BY_USER);
        assertThat(card.getAttachedUserComment()).isEqualTo("Wrong data");
    }

    @Test
    void rejectByUserRejectsCompletedCard() {
        Card card = cardWith(CardStatus.COMPLETED, attachedUserId(ATTACHED_USER_ID), null);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.rejectByUser(CARD_ID, "too late"))
                .isInstanceOf(InvalidCardStatusException.class);
    }

    @Test
    void completeSetsCompletedDateAndStatus() {
        Card card = cardWith(CardStatus.ACCEPTED_BY_USER, attachedUserId(ATTACHED_USER_ID), null);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        service.complete(CARD_ID);

        assertThat(card.getStatus()).isEqualTo(CardStatus.COMPLETED);
        assertThat(card.getCompletedDate()).isNotNull();
    }

    @Test
    void completeRejectsBeforeAcceptance() {
        Card card = cardWith(CardStatus.NEW, attachedUserId(ATTACHED_USER_ID), null);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.complete(CARD_ID))
                .isInstanceOf(InvalidCardStatusException.class);
    }

    @Test
    void completeRejectsAfterUserRejection() {
        Card card = cardWith(CardStatus.REJECTED_BY_USER, attachedUserId(ATTACHED_USER_ID), null);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.complete(CARD_ID))
                .isInstanceOf(InvalidCardStatusException.class);
    }

    @Test
    void approveBySupervisorSucceedsForAssignedSupervisorOnCompletedCard() {
        Card card = cardWith(CardStatus.COMPLETED, Set.of(), SUPERVISOR_ID);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(SUPERVISOR_ID);

        service.approveBySupervisor(CARD_ID);

        assertThat(card.getStatus()).isEqualTo(CardStatus.APPROVED);
    }

    @Test
    void approveBySupervisorRejectsWrongSupervisor() {
        Card card = cardWith(CardStatus.COMPLETED, Set.of(), SUPERVISOR_ID);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(999L);

        assertThatThrownBy(() -> service.approveBySupervisor(CARD_ID))
                .isInstanceOf(CardScopeViolationException.class);
    }

    @Test
    void approveBySupervisorRejectsNonCompletedCard() {
        Card card = cardWith(CardStatus.IN_PROGRESS, Set.of(), SUPERVISOR_ID);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(SUPERVISOR_ID);

        assertThatThrownBy(() -> service.approveBySupervisor(CARD_ID))
                .isInstanceOf(InvalidCardStatusException.class);
    }

    @Test
    void rejectBySupervisorRequiresNonBlankComment() {
        Card card = cardWith(CardStatus.COMPLETED, Set.of(), SUPERVISOR_ID);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(SUPERVISOR_ID);

        assertThatThrownBy(() -> service.rejectBySupervisor(CARD_ID, "   "))
                .isInstanceOf(CardValidationException.class);
    }

    @Test
    void rejectBySupervisorSetsCommentAndStatus() {
        Card card = cardWith(CardStatus.COMPLETED, Set.of(), SUPERVISOR_ID);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(SUPERVISOR_ID);

        service.rejectBySupervisor(CARD_ID, "Incomplete investigation");

        assertThat(card.getStatus()).isEqualTo(CardStatus.REJECTED);
        assertThat(card.getSupervisorComment()).isEqualTo("Incomplete investigation");
    }

    @Test
    void completeAllowsReworkAfterSupervisorRejection() {
        Card card = cardWith(CardStatus.REJECTED, attachedUserId(ATTACHED_USER_ID), SUPERVISOR_ID);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        service.complete(CARD_ID);

        assertThat(card.getStatus()).isEqualTo(CardStatus.COMPLETED);
    }

    @Test
    void completeRejectsAlreadyApprovedCard() {
        Card card = cardWith(CardStatus.APPROVED, attachedUserId(ATTACHED_USER_ID), SUPERVISOR_ID);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.complete(CARD_ID))
                .isInstanceOf(InvalidCardStatusException.class);
    }

    @Test
    void rejectByUserRejectsAlreadyApprovedCard() {
        Card card = cardWith(CardStatus.APPROVED, attachedUserId(ATTACHED_USER_ID), SUPERVISOR_ID);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(ATTACHED_USER_ID);

        assertThatThrownBy(() -> service.rejectByUser(CARD_ID, "too late"))
                .isInstanceOf(InvalidCardStatusException.class);
    }

    @Test
    void updateBlocksEditingAnApprovedCard() {
        Card card = cardWith(CardStatus.APPROVED, attachedUserId(ATTACHED_USER_ID), SUPERVISOR_ID);
        givenCard(card);

        // The canBeUpdated() check throws before the request body is ever
        // touched, so a null request is enough to prove the card is locked.
        assertThatThrownBy(() -> service.update(CARD_ID, null))
                .isInstanceOf(InvalidCardStatusException.class);
    }

    @Test
    void updateBlocksEditingBeforeAcceptance() {
        Card card = cardWith(CardStatus.NEW, attachedUserId(ATTACHED_USER_ID), null);
        givenCard(card);

        assertThatThrownBy(() -> service.update(CARD_ID, null))
                .isInstanceOf(InvalidCardStatusException.class);
    }

    @Test
    void updateBlocksEditingAfterUserRejection() {
        Card card = cardWith(CardStatus.REJECTED_BY_USER, attachedUserId(ATTACHED_USER_ID), null);
        givenCard(card);

        assertThatThrownBy(() -> service.update(CARD_ID, null))
                .isInstanceOf(InvalidCardStatusException.class);
    }

    @Test
    void updatePassesTheStatusGateOnceAcceptedOrReworkingAfterSupervisorRejection() {
        // CardRequest is sealed and its only implementations are large
        // records, so a null request stands in here — what matters is
        // *which* exception surfaces: canBeUpdated() throws
        // InvalidCardStatusException before request is ever touched (see
        // the blocked-status tests above), so for these two allowed
        // statuses the gate must pass silently and blow up on
        // request.type() instead — proving it let the call through.
        for (CardStatus status : List.of(CardStatus.ACCEPTED_BY_USER, CardStatus.REJECTED)) {
            Card161 card = new Card161();
            card.setStatus(status);
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

            assertThatThrownBy(() -> service.update(CARD_ID, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void reassignUsersSucceedsForUserRejectedCard() {
        Card161 card = new Card161();
        card.setStatus(CardStatus.REJECTED_BY_USER);
        card.setAttachedUserComment("Wrong data");
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(SUPERVISOR_ID);

        User newEmployee = userWithId(99L);
        when(userRepository.findAllById(List.of(99L))).thenReturn(List.of(newEmployee));

        service.reassignUsers(CARD_ID, new ReassignCardUsersRequest(List.of(99L)));

        assertThat(card.getStatus()).isEqualTo(CardStatus.NEW);
        assertThat(card.getUsers()).containsExactly(newEmployee);
        assertThat(card.getAssignedById()).isEqualTo(SUPERVISOR_ID);
        assertThat(card.getAttachedUserComment()).isNull();
    }

    @Test
    void reassignUsersRejectsCardNotYetRejectedByUser() {
        Card card = cardWith(CardStatus.NEW, Set.of(), null);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(SUPERVISOR_ID);

        assertThatThrownBy(() -> service.reassignUsers(CARD_ID, new ReassignCardUsersRequest(List.of(1L))))
                .isInstanceOf(InvalidCardStatusException.class);
    }

    @Test
    void reassignUsersRequiresAnAuthenticatedCaller() {
        Card card = cardWith(CardStatus.REJECTED_BY_USER, Set.of(), null);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(null);

        assertThatThrownBy(() -> service.reassignUsers(CARD_ID, new ReassignCardUsersRequest(List.of(1L))))
                .isInstanceOf(CardScopeViolationException.class);
    }

    @Test
    void reassignUsersRejectsUnknownUserId() {
        Card card = cardWith(CardStatus.REJECTED_BY_USER, Set.of(), null);
        givenCard(card);
        when(currentCardUser.userIdOrNull()).thenReturn(SUPERVISOR_ID);
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.reassignUsers(CARD_ID, new ReassignCardUsersRequest(List.of(1L))))
                .isInstanceOf(CardValidationException.class);
    }

    @Test
    void assignActAddsActRegardlessOfStatus() {
        Card card = cardWith(CardStatus.APPROVED, Set.of(), SUPERVISOR_ID);
        givenCard(card);

        service.assignAct(CARD_ID, "ACT153");

        assertThat(card.getActs()).hasSize(1);
        assertThat(card.getActs().getFirst().getActType()).isEqualTo("ACT153");
        assertThat(card.getActs().getFirst().getCard()).isSameAs(card);
    }

    private void givenCard(Card card) {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
    }

    private Card cardWith(CardStatus status, Set<User> users, Long assignedById) {
        Card161 card = new Card161();
        card.setStatus(status);
        card.setUsers(users);
        card.setAssignedById(assignedById);
        return card;
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
