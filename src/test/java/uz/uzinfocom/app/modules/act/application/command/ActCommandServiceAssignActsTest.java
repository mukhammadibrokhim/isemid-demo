package uz.uzinfocom.app.modules.act.application.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uz.uzinfocom.app.modules.act.application.exception.ActScopeViolationException;
import uz.uzinfocom.app.modules.act.application.exception.ActValidationException;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActRepository;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActMapper;
import uz.uzinfocom.app.modules.act.web.dto.request.AssignActsRequest;
import uz.uzinfocom.app.modules.card.application.exception.CardNotFoundException;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.domain.model.card161.Card161;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the bulk "assign acts" flow — mirrors
 * {@code CardCommandServiceAssignCardsTest} exactly: one blank act per
 * distinct requested type, all sharing the same set of attached employees,
 * with {@code assignedById} set to whoever performed the assignment.
 */
class ActCommandServiceAssignActsTest {

    private static final Long CARD_ID = 100L;
    private static final Long ACTOR_ID = 7L;

    private ActRepository actRepository;
    private CardRepository cardRepository;
    private UserRepository userRepository;
    private CurrentUserProvider currentUserProvider;
    private ActCommandService service;

    @BeforeEach
    void setUp() {
        actRepository = mock(ActRepository.class);
        cardRepository = mock(CardRepository.class);
        userRepository = mock(UserRepository.class);
        currentUserProvider = mock(CurrentUserProvider.class);
        ActMapper actMapper = mock(ActMapper.class);

        service = new ActCommandService(actRepository, cardRepository, userRepository, actMapper, currentUserProvider);

        when(actRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsOneBlankActPerDistinctTypeWithSharedUsersAndActorAsSupervisor() {
        Card card = cardWithId(CARD_ID);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(currentUserProvider.userIdOrNull()).thenReturn(ACTOR_ID);

        User employee1 = userWithId(1L);
        User employee2 = userWithId(2L);
        when(userRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(employee1, employee2));

        service.assignActs(CARD_ID, new AssignActsRequest(
                List.of("ACT153", "ACT154"),
                List.of(1L, 2L)
        ));

        List<Act> saved = capturePersistedActs();
        assertThat(saved).hasSize(2);
        for (Act act : saved) {
            assertThat(act.getCard()).isSameAs(card);
            assertThat(act.getAssignedById()).isEqualTo(ACTOR_ID);
            assertThat(act.getActStatus()).isEqualTo(ActStatus.NEW);
            assertThat(act.getUsers()).containsExactlyInAnyOrder(employee1, employee2);
        }
        assertThat(saved.stream().map(Act::getActType)).containsExactlyInAnyOrder("ACT153", "ACT154");
    }

    @Test
    void deduplicatesRepeatedActTypesAndUserIds() {
        Card card = cardWithId(CARD_ID);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(currentUserProvider.userIdOrNull()).thenReturn(ACTOR_ID);
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(userWithId(1L)));

        service.assignActs(CARD_ID, new AssignActsRequest(
                List.of("ACT153", "ACT153"),
                List.of(1L, 1L)
        ));

        assertThat(capturePersistedActs()).hasSize(1);
    }

    @Test
    void rejectsWhenAnAssignedUserIdDoesNotExist() {
        Card card = cardWithId(CARD_ID);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(currentUserProvider.userIdOrNull()).thenReturn(ACTOR_ID);
        when(userRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(userWithId(1L)));

        assertThatThrownBy(() -> service.assignActs(CARD_ID, new AssignActsRequest(
                List.of("ACT153"),
                List.of(1L, 2L)
        ))).isInstanceOf(ActValidationException.class);
    }

    @Test
    void rejectsAnUnauthenticatedCaller() {
        Card card = cardWithId(CARD_ID);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(currentUserProvider.userIdOrNull()).thenReturn(null);

        assertThatThrownBy(() -> service.assignActs(CARD_ID, new AssignActsRequest(
                List.of("ACT153"),
                List.of(1L)
        ))).isInstanceOf(ActScopeViolationException.class);
    }

    @Test
    void failsWhenCardDoesNotExist() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignActs(CARD_ID, new AssignActsRequest(
                List.of("ACT153"),
                List.of(1L)
        ))).isInstanceOf(CardNotFoundException.class);
    }

    @SuppressWarnings("unchecked")
    private List<Act> capturePersistedActs() {
        ArgumentCaptor<List<Act>> captor = ArgumentCaptor.forClass(List.class);
        verify(actRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    private Card cardWithId(Long id) {
        Card161 card = new Card161();
        card.setId(id);
        return card;
    }

    private User userWithId(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
