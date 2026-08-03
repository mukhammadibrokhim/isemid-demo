package uz.uzinfocom.app.modules.card.application.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import uz.uzinfocom.app.modules.card.application.exception.CardScopeViolationException;
import uz.uzinfocom.app.modules.card.application.exception.CardValidationException;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandler;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandlerRegistry;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.EntityCreatedEvent;
import uz.uzinfocom.app.platform.security.authorization.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.domain.model.card161.Card161;
import uz.uzinfocom.app.modules.card.domain.model.card174.Card174;
import uz.uzinfocom.app.modules.card.domain.model.card175.Card175;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.modules.card.web.dto.request.AssignCardsRequest;
import uz.uzinfocom.app.modules.form058.application.exception.Form058NotFoundException;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581NotFoundException;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the bulk "assign cards" flow ported from the legacy
 * {@code Form058ServiceImpl.assignCard} — one blank card per distinct
 * requested type, all sharing the same set of attached employees, with
 * {@code assignedById} set to whoever performed the assignment.
 */
class CardCommandServiceAssignCardsTest {

    private static final Long FORM_ID = 100L;
    private static final Long ACTOR_ID = 7L;

    private CardRepository cardRepository;
    private Form058JpaRepository form058Repository;
    private Form0581JpaRepository form0581Repository;
    private UserRepository userRepository;
    private CardTypeHandlerRegistry handlerRegistry;
    private CurrentUserProvider currentUserProvider;
    private ApplicationEventPublisher eventPublisher;
    private CardCommandService service;

    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);
        form058Repository = mock(Form058JpaRepository.class);
        form0581Repository = mock(Form0581JpaRepository.class);
        userRepository = mock(UserRepository.class);
        handlerRegistry = mock(CardTypeHandlerRegistry.class);
        currentUserProvider = mock(CurrentUserProvider.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        AdminAccessGuard adminAccessGuard = mock(AdminAccessGuard.class);
        when(adminAccessGuard.isSuperAdmin()).thenReturn(true);

        service = new CardCommandService(
                cardRepository, form058Repository, form0581Repository, userRepository, handlerRegistry,
                currentUserProvider, eventPublisher, adminAccessGuard
        );

        when(cardRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(form058Repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(form0581Repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsOneBlankCardPerDistinctTypeWithSharedUsersAndActorAsSupervisor() {
        Form058 form = formWith(FormStatus.RECEIVED);
        when(form058Repository.findByIdAndDeletedFalse(FORM_ID)).thenReturn(Optional.of(form));
        when(currentUserProvider.userIdOrNull()).thenReturn(ACTOR_ID);

        User employee1 = userWithId(1L);
        User employee2 = userWithId(2L);
        when(userRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(employee1, employee2));

        stubHandler(CardType.CARD161, new Card161());
        stubHandler(CardType.CARD174, new Card174());

        service.assignCards(FORM_ID, new AssignCardsRequest(
                List.of(CardType.CARD161, CardType.CARD174),
                List.of(1L, 2L)
        ));

        List<Card> saved = capturePersistedCards();
        assertThat(saved).hasSize(2);
        for (Card card : saved) {
            assertThat(card.getForm058()).isSameAs(form);
            assertThat(card.getAssignedById()).isEqualTo(ACTOR_ID);
            assertThat(card.getUsers()).containsExactlyInAnyOrder(employee1, employee2);
        }
        assertThat(form.getStatus()).isEqualTo(FormStatus.CARD_LINKED);
        assertThat(form.isHasLinkedCards()).isTrue();
    }

    @Test
    void publishesAnEntityCreatedEventPerCreatedCard() {
        Form058 form = formWith(FormStatus.RECEIVED);
        when(form058Repository.findByIdAndDeletedFalse(FORM_ID)).thenReturn(Optional.of(form));
        when(currentUserProvider.userIdOrNull()).thenReturn(ACTOR_ID);
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(userWithId(1L)));

        stubHandler(CardType.CARD161, new Card161());

        service.assignCards(FORM_ID, new AssignCardsRequest(List.of(CardType.CARD161), List.of(1L)));

        List<Card> saved = capturePersistedCards();
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, org.mockito.Mockito.atLeastOnce()).publishEvent(eventCaptor.capture());

        EntityCreatedEvent cardEvent = eventCaptor.getAllValues().stream()
                .filter(EntityCreatedEvent.class::isInstance)
                .map(EntityCreatedEvent.class::cast)
                .filter(event -> event.entityType() == AuditEntityType.CARD)
                .findFirst()
                .orElseThrow();

        assertThat(cardEvent.entityId()).isEqualTo(saved.get(0).getId());
        assertThat(cardEvent.actorUserId()).isEqualTo(ACTOR_ID);
    }

    @Test
    void deduplicatesRepeatedCardTypesAndUserIds() {
        Form058 form = formWith(FormStatus.RECEIVED);
        when(form058Repository.findByIdAndDeletedFalse(FORM_ID)).thenReturn(Optional.of(form));
        when(currentUserProvider.userIdOrNull()).thenReturn(ACTOR_ID);
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(userWithId(1L)));

        stubHandler(CardType.CARD161, new Card161());

        service.assignCards(FORM_ID, new AssignCardsRequest(
                List.of(CardType.CARD161, CardType.CARD161),
                List.of(1L, 1L)
        ));

        assertThat(capturePersistedCards()).hasSize(1);
    }

    @Test
    void rejectsWhenAnAssignedUserIdDoesNotExist() {
        Form058 form = formWith(FormStatus.RECEIVED);
        when(form058Repository.findByIdAndDeletedFalse(FORM_ID)).thenReturn(Optional.of(form));
        when(currentUserProvider.userIdOrNull()).thenReturn(ACTOR_ID);
        when(userRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(userWithId(1L)));

        assertThatThrownBy(() -> service.assignCards(FORM_ID, new AssignCardsRequest(
                List.of(CardType.CARD161),
                List.of(1L, 2L)
        ))).isInstanceOf(CardValidationException.class);
    }

    @Test
    void rejectsAnUnauthenticatedCaller() {
        Form058 form = formWith(FormStatus.RECEIVED);
        when(form058Repository.findByIdAndDeletedFalse(FORM_ID)).thenReturn(Optional.of(form));
        when(currentUserProvider.userIdOrNull()).thenReturn(null);

        assertThatThrownBy(() -> service.assignCards(FORM_ID, new AssignCardsRequest(
                List.of(CardType.CARD161),
                List.of(1L)
        ))).isInstanceOf(CardScopeViolationException.class);
    }

    @Test
    void failsWhenFormDoesNotExist() {
        when(form058Repository.findByIdAndDeletedFalse(FORM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignCards(FORM_ID, new AssignCardsRequest(
                List.of(CardType.CARD161),
                List.of(1L)
        ))).isInstanceOf(Form058NotFoundException.class);
    }

    @Test
    void doesNotRegressAFormThatIsAlreadyPastCardLinking() {
        Form058 form = formWith(FormStatus.APPROVED_PENDING);
        when(form058Repository.findByIdAndDeletedFalse(FORM_ID)).thenReturn(Optional.of(form));
        when(currentUserProvider.userIdOrNull()).thenReturn(ACTOR_ID);
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(userWithId(1L)));
        stubHandler(CardType.CARD161, new Card161());

        service.assignCards(FORM_ID, new AssignCardsRequest(List.of(CardType.CARD161), List.of(1L)));

        assertThat(form.getStatus()).isEqualTo(FormStatus.APPROVED_PENDING);
        assertThat(form.isHasLinkedCards()).isTrue();
    }

    @Test
    void assignCardsToForm0581CreatesCardsRestrictedToAllowedTypes() {
        Form0581 form = form0581With(Form0581Status.RECEIVED);
        when(form0581Repository.findByIdAndDeletedFalse(FORM_ID)).thenReturn(Optional.of(form));
        when(currentUserProvider.userIdOrNull()).thenReturn(ACTOR_ID);
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(userWithId(1L)));

        stubHandler(CardType.CARD175, new Card175());

        service.assignCardsToForm0581(FORM_ID, new AssignCardsRequest(List.of(CardType.CARD175), List.of(1L)));

        assertThat(form.isHasLinkedCards()).isTrue();
        verify(cardRepository).saveAll(any());
    }

    @Test
    void assignCardsToForm0581RejectsDisallowedCardType() {
        Form0581 form = form0581With(Form0581Status.RECEIVED);
        when(form0581Repository.findByIdAndDeletedFalse(FORM_ID)).thenReturn(Optional.of(form));

        assertThatThrownBy(() -> service.assignCardsToForm0581(FORM_ID, new AssignCardsRequest(
                List.of(CardType.CARD161), List.of(1L)
        ))).isInstanceOf(CardValidationException.class);
    }

    @Test
    void assignCardsToForm0581FailsWhenFormDoesNotExist() {
        when(form0581Repository.findByIdAndDeletedFalse(FORM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignCardsToForm0581(FORM_ID, new AssignCardsRequest(
                List.of(CardType.CARD175), List.of(1L)
        ))).isInstanceOf(Form0581NotFoundException.class);
    }

    private Form0581 form0581With(Form0581Status status) {
        Form0581 form = Form0581.builder().status(status).build();
        form.setId(FORM_ID);
        return form;
    }

    @SuppressWarnings("unchecked")
    private void stubHandler(CardType type, Card blank) {
        CardTypeHandler<?, ?, ?> handler = mock(CardTypeHandler.class);
        when(handler.handleCreateBlank()).thenReturn(blank);
        doReturn(handler).when(handlerRegistry).get(type);
    }

    @SuppressWarnings("unchecked")
    private List<Card> capturePersistedCards() {
        ArgumentCaptor<List<Card>> captor = ArgumentCaptor.forClass(List.class);
        verify(cardRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    private Form058 formWith(FormStatus status) {
        Form058 form = Form058.builder().status(status).build();
        form.setId(FORM_ID);
        return form;
    }

    private User userWithId(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
