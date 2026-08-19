package uz.uzinfocom.app.orchestration.notification.application;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActRepository;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.infrastructure.persistence.repository.PatientAffiliationJpaRepository;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.EntityCreatedEvent;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.export.domain.event.ExportJobCompletedEvent;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.orchestration.notification.domain.Notification;
import uz.uzinfocom.app.orchestration.notification.domain.NotificationType;
import uz.uzinfocom.app.orchestration.notification.repository.NotificationRepository;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationEventListenerTest {

    private final Form058JpaRepository form058Repository = mock(Form058JpaRepository.class);
    private final Form0581JpaRepository form0581Repository = mock(Form0581JpaRepository.class);
    private final CardRepository cardRepository = mock(CardRepository.class);
    private final ActRepository actRepository = mock(ActRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final SystemSettingResolver systemSettingResolver = mock(SystemSettingResolver.class);
    private final PatientAffiliationJpaRepository patientAffiliationRepository = mock(PatientAffiliationJpaRepository.class);

    private final NotificationEventListener listener = new NotificationEventListener(
            form058Repository, form0581Repository, cardRepository, actRepository,
            userRepository, notificationRepository, systemSettingResolver, JsonMapper.builder().build(),
            patientAffiliationRepository
    );

    @Test
    void formReceivedFansOutToActiveOrganizationUsersExcludingTheActor() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        Form058 form058 = mock(Form058.class);
        when(form058.getReceiverOrganizationId()).thenReturn(5L);
        when(form058Repository.findById(1L)).thenReturn(Optional.of(form058));
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(10L, 20L, 30L));

        listener.on(new EntityCreatedEvent(AuditEntityType.FORM058, 1L, 20L));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).extracting(Notification::getRecipientUserId).containsExactlyInAnyOrder(10L, 30L);
        assertThat(saved).allSatisfy(notification -> {
            assertThat(notification.getType()).isEqualTo(NotificationType.FORM058_RECEIVED);
            assertThat(notification.getEntityType()).isEqualTo(AuditEntityType.FORM058);
            assertThat(notification.getEntityId()).isEqualTo(1L);
            assertThat(notification.getOrganizationId()).isEqualTo(5L);
            assertThat(notification.getRead()).isFalse();
        });
    }

    @Test
    void formReceivedNotificationIsSkippedWhenDisabledBySetting() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(false);

        listener.on(new EntityCreatedEvent(AuditEntityType.FORM058, 1L, 20L));

        verify(form058Repository, never()).findById(any());
        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void formReceivedAlsoNotifiesAffiliatedOrganizationsExcludingSenderAndReceiver() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        Patient patient = mock(Patient.class);
        when(patient.getId()).thenReturn(50L);

        Form058 form058 = mock(Form058.class);
        when(form058.getSenderOrganizationId()).thenReturn(3L);
        when(form058.getReceiverOrganizationId()).thenReturn(5L);
        when(form058.getPatient()).thenReturn(patient);
        when(form058Repository.findById(1L)).thenReturn(Optional.of(form058));
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(10L));

        // 3L is the sender - already notified separately, must be filtered out here.
        when(patientAffiliationRepository.findDistinctOrganizationIdsByPatientIdAndTypeIn(
                50L, uz.uzinfocom.app.orchestration.scope.FormAccessScopeResolver.AFFILIATION_TYPES
        )).thenReturn(List.of(3L, 7L));
        when(userRepository.findActiveIdsByOrganizationId(7L)).thenReturn(List.of(70L));

        listener.on(new EntityCreatedEvent(AuditEntityType.FORM058, 1L, 20L));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).saveAll(captor.capture());

        List<List<Notification>> allSaved = captor.getAllValues();
        assertThat(allSaved.get(0)).extracting(Notification::getType).containsExactly(NotificationType.FORM058_RECEIVED);
        assertThat(allSaved.get(0)).extracting(Notification::getRecipientUserId).containsExactly(10L);
        assertThat(allSaved.get(1)).extracting(Notification::getType).containsExactly(NotificationType.FORM058_AFFILIATED_RECEIVED);
        assertThat(allSaved.get(1)).extracting(Notification::getRecipientUserId).containsExactly(70L);
    }

    @Test
    void cardAssignedNotifiesAttachedUsersExcludingTheAssigner() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        User assigner = mock(User.class);
        when(assigner.getId()).thenReturn(99L);
        User attached = mock(User.class);
        when(attached.getId()).thenReturn(100L);

        Form058 form058 = mock(Form058.class);
        when(form058.getReceiverOrganizationId()).thenReturn(5L);

        Card card = mock(Card.class);
        when(card.getId()).thenReturn(7L);
        when(card.getUsers()).thenReturn(Set.of(assigner, attached));
        when(card.getForm058()).thenReturn(form058);
        when(cardRepository.findById(7L)).thenReturn(Optional.of(card));

        listener.on(new EntityCreatedEvent(AuditEntityType.CARD, 7L, 99L));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getRecipientUserId()).isEqualTo(100L);
        assertThat(saved.get(0).getType()).isEqualTo(NotificationType.CARD_ASSIGNED);
        assertThat(saved.get(0).getOrganizationId()).isEqualTo(5L);
    }

    @Test
    void cardAcceptedByUserNotifiesTheAssigningSupervisorOnlyOnNewToAcceptedTransition() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        listener.on(new StatusChangedEvent(AuditEntityType.CARD, 7L, "ACCEPTED_BY_USER", "IN_PROGRESS", 100L, null));
        verify(cardRepository, never()).findById(any());

        Card card = mock(Card.class);
        when(card.getId()).thenReturn(7L);
        when(card.getAssignedById()).thenReturn(99L);
        when(card.getForm058()).thenReturn(null);
        when(card.getForm0581()).thenReturn(null);
        when(cardRepository.findById(7L)).thenReturn(Optional.of(card));

        listener.on(new StatusChangedEvent(AuditEntityType.CARD, 7L, "NEW", "ACCEPTED_BY_USER", 100L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getRecipientUserId()).isEqualTo(99L);
        assertThat(saved.get(0).getType()).isEqualTo(NotificationType.CARD_ACCEPTED_BY_USER);
    }

    @Test
    void cardRejectedByUserNotifiesTheAssigningSupervisorFromEitherNewOrAcceptedByUser() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        Card card = mock(Card.class);
        when(card.getId()).thenReturn(7L);
        when(card.getAssignedById()).thenReturn(99L);
        when(cardRepository.findById(7L)).thenReturn(Optional.of(card));

        listener.on(new StatusChangedEvent(AuditEntityType.CARD, 7L, "ACCEPTED_BY_USER", "REJECTED_BY_USER", 100L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getRecipientUserId()).isEqualTo(99L);
        assertThat(saved.get(0).getType()).isEqualTo(NotificationType.CARD_REJECTED_BY_USER);
    }

    @Test
    void cardCompletedNotifiesTheAssigningSupervisorRegardlessOfPriorStatus() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        Card card = mock(Card.class);
        when(card.getId()).thenReturn(7L);
        when(card.getAssignedById()).thenReturn(99L);
        when(cardRepository.findById(7L)).thenReturn(Optional.of(card));

        // COMPLETED is reachable from ACCEPTED_BY_USER, IN_PROGRESS, or REJECTED
        // (rework) — matched on the new status alone, same as Form058Approved.
        listener.on(new StatusChangedEvent(AuditEntityType.CARD, 7L, "REJECTED", "COMPLETED", 100L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getRecipientUserId()).isEqualTo(99L);
        assertThat(saved.get(0).getType()).isEqualTo(NotificationType.CARD_COMPLETED);
    }

    @Test
    void cardApprovedNotifiesAttachedUsersExcludingTheSupervisor() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        User attached = mock(User.class);
        when(attached.getId()).thenReturn(100L);

        Card card = mock(Card.class);
        when(card.getId()).thenReturn(7L);
        when(card.getUsers()).thenReturn(Set.of(attached));
        when(cardRepository.findById(7L)).thenReturn(Optional.of(card));

        listener.on(new StatusChangedEvent(AuditEntityType.CARD, 7L, "COMPLETED", "APPROVED", 99L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getRecipientUserId()).isEqualTo(100L);
        assertThat(saved.get(0).getType()).isEqualTo(NotificationType.CARD_APPROVED);
    }

    @Test
    void cardRejectedBySupervisorNotifiesAttachedUsersExcludingTheSupervisor() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        User attached = mock(User.class);
        when(attached.getId()).thenReturn(100L);

        Card card = mock(Card.class);
        when(card.getId()).thenReturn(7L);
        when(card.getUsers()).thenReturn(Set.of(attached));
        when(cardRepository.findById(7L)).thenReturn(Optional.of(card));

        listener.on(new StatusChangedEvent(AuditEntityType.CARD, 7L, "COMPLETED", "REJECTED", 99L, "Incomplete"));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getRecipientUserId()).isEqualTo(100L);
        assertThat(saved.get(0).getType()).isEqualTo(NotificationType.CARD_REJECTED);
    }

    @Test
    void actLisResponseFiresOnlyForTheSentToCompletedTransition() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        listener.on(new StatusChangedEvent(AuditEntityType.ACT, 3L, "IN_PROGRESS", "READY", 1L, null));
        verify(actRepository, never()).findById(any());

        User attached = mock(User.class);
        when(attached.getId()).thenReturn(200L);
        Act act = mock(Act.class);
        when(act.getId()).thenReturn(3L);
        when(act.getUsers()).thenReturn(Set.of(attached));
        when(act.getCard()).thenReturn(null);
        when(actRepository.findById(3L)).thenReturn(Optional.of(act));

        listener.on(new StatusChangedEvent(AuditEntityType.ACT, 3L, "SENT", "COMPLETED", 1L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(Notification::getType)
                .containsExactly(NotificationType.ACT_LIS_RESPONSE);
    }

    @Test
    void exportJobCompletedNotifiesOnlyTheRequestingUser() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        listener.on(new ExportJobCompletedEvent(42L, 7L, "FORM058", "form058_42.xlsx"));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getRecipientUserId()).isEqualTo(7L);
        assertThat(saved.get(0).getType()).isEqualTo(NotificationType.EXPORT_READY);
        assertThat(saved.get(0).getEntityType()).isEqualTo(AuditEntityType.EXPORT_JOB);
        assertThat(saved.get(0).getEntityId()).isEqualTo(42L);
    }

    @Test
    void exportJobCompletedIsSkippedWhenDisabledBySetting() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(false);

        listener.on(new ExportJobCompletedEvent(42L, 7L, "FORM058", "form058_42.xlsx"));

        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void form058AcknowledgedNotifiesSenderOrganizationOnlyOnSentToAcceptedTransition() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "SENT", "CARD_LINKED", 1L, null));
        verify(form058Repository, never()).findById(any());

        Form058 form058 = mock(Form058.class);
        when(form058.getSenderOrganizationId()).thenReturn(3L);
        when(form058Repository.findById(9L)).thenReturn(Optional.of(form058));
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "SENT", "ACCEPTED", 22L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).extracting(Notification::getRecipientUserId).containsExactly(11L);
        assertThat(saved).allSatisfy(notification -> {
            assertThat(notification.getType()).isEqualTo(NotificationType.FORM058_ACKNOWLEDGED);
            assertThat(notification.getOrganizationId()).isEqualTo(3L);
        });
    }

    @Test
    void form058CanceledNotifiesBothOrganizationsExcludingTheActor() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        Form058 form058 = mock(Form058.class);
        when(form058.getSenderOrganizationId()).thenReturn(3L);
        when(form058.getReceiverOrganizationId()).thenReturn(5L);
        when(form058Repository.findById(9L)).thenReturn(Optional.of(form058));
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(33L, 44L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "SENT", "CANCELED", 22L, "reason"));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).saveAll(captor.capture());

        List<List<Notification>> allSaved = captor.getAllValues();
        assertThat(allSaved.get(0)).extracting(Notification::getRecipientUserId).containsExactly(11L);
        assertThat(allSaved.get(1)).extracting(Notification::getRecipientUserId)
                .containsExactlyInAnyOrder(33L, 44L);
        assertThat(allSaved.stream().flatMap(List::stream))
                .allSatisfy(notification -> assertThat(notification.getType())
                        .isEqualTo(NotificationType.FORM058_CANCELED));
    }

    @Test
    void form058CardLinkedNotifiesSenderOrganizationOnlyOnAcceptedToCardLinkedTransition() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "SENT", "CARD_LINKED", 1L, null));
        verify(form058Repository, never()).findById(any());

        Form058 form058 = mock(Form058.class);
        when(form058.getSenderOrganizationId()).thenReturn(3L);
        when(form058Repository.findById(9L)).thenReturn(Optional.of(form058));
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "ACCEPTED", "CARD_LINKED", 22L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).extracting(Notification::getRecipientUserId).containsExactly(11L);
        assertThat(saved).allSatisfy(notification ->
                assertThat(notification.getType()).isEqualTo(NotificationType.FORM058_CARD_LINKED));
    }

    @Test
    void form058CardLinkedAlsoNotifiesAffiliatedOrganizationsExcludingSenderAndReceiver() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        Patient patient = mock(Patient.class);
        when(patient.getId()).thenReturn(50L);

        Form058 form058 = mock(Form058.class);
        when(form058.getSenderOrganizationId()).thenReturn(3L);
        when(form058.getReceiverOrganizationId()).thenReturn(5L);
        when(form058.getPatient()).thenReturn(patient);
        when(form058Repository.findById(9L)).thenReturn(Optional.of(form058));
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L));

        // 5L is the receiver - not notified by this transition at all (see
        // form058CardLinkedNotifiesSenderOrganizationOnlyOnAcceptedToCardLinkedTransition),
        // so it must be filtered out of the affiliated fan-out too.
        when(patientAffiliationRepository.findDistinctOrganizationIdsByPatientIdAndTypeIn(
                50L, uz.uzinfocom.app.orchestration.scope.FormAccessScopeResolver.AFFILIATION_TYPES
        )).thenReturn(List.of(5L, 7L));
        when(userRepository.findActiveIdsByOrganizationId(7L)).thenReturn(List.of(70L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "ACCEPTED", "CARD_LINKED", 22L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).saveAll(captor.capture());

        List<List<Notification>> allSaved = captor.getAllValues();
        assertThat(allSaved.get(0)).extracting(Notification::getType).containsExactly(NotificationType.FORM058_CARD_LINKED);
        assertThat(allSaved.get(0)).extracting(Notification::getRecipientUserId).containsExactly(11L);
        assertThat(allSaved.get(1)).extracting(Notification::getType).containsExactly(NotificationType.FORM058_AFFILIATED_CARD_LINKED);
        assertThat(allSaved.get(1)).extracting(Notification::getRecipientUserId).containsExactly(70L);
    }

    @Test
    void form058ApprovedNotifiesReceiverOrganizationOnApprovedTransition() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        Form058 form058 = mock(Form058.class);
        when(form058.getReceiverOrganizationId()).thenReturn(5L);
        when(form058Repository.findById(9L)).thenReturn(Optional.of(form058));
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(33L, 44L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "CARD_LINKED", "APPROVED", 1L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).extracting(Notification::getRecipientUserId).containsExactlyInAnyOrder(33L, 44L);
        assertThat(saved).allSatisfy(notification ->
                assertThat(notification.getType()).isEqualTo(NotificationType.FORM058_APPROVED));
    }

    @Test
    void form058ReopenedNotifiesBothOrganizationsOnCanceledToSentTransition() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        Form058 form058 = mock(Form058.class);
        when(form058.getSenderOrganizationId()).thenReturn(3L);
        when(form058.getReceiverOrganizationId()).thenReturn(5L);
        when(form058Repository.findById(9L)).thenReturn(Optional.of(form058));
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(33L, 44L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "CANCELED", "SENT", 1L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).saveAll(captor.capture());

        List<List<Notification>> allSaved = captor.getAllValues();
        assertThat(allSaved.get(0)).extracting(Notification::getRecipientUserId).containsExactlyInAnyOrder(11L, 22L);
        assertThat(allSaved.get(1)).extracting(Notification::getRecipientUserId).containsExactlyInAnyOrder(33L, 44L);
        assertThat(allSaved.stream().flatMap(List::stream))
                .allSatisfy(notification -> assertThat(notification.getType())
                        .isEqualTo(NotificationType.FORM058_REOPENED));
    }

    @Test
    void form058CanceledIsSkippedWhenDisabledBySetting() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(false);

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "SENT", "CANCELED", 22L, "reason"));

        verify(form058Repository, never()).findById(any());
        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void form0581AcknowledgedNotifiesSenderOrganizationOnlyOnSentToAcceptedTransition() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "SENT", "CARD_LINKED", 1L, null));
        verify(form0581Repository, never()).findById(any());

        Form0581 form0581 = mock(Form0581.class);
        when(form0581.getSenderOrganizationId()).thenReturn(3L);
        when(form0581Repository.findById(9L)).thenReturn(Optional.of(form0581));
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "SENT", "ACCEPTED", 22L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).extracting(Notification::getRecipientUserId).containsExactly(11L);
        assertThat(saved).allSatisfy(notification -> {
            assertThat(notification.getType()).isEqualTo(NotificationType.FORM0581_ACKNOWLEDGED);
            assertThat(notification.getOrganizationId()).isEqualTo(3L);
        });
    }

    @Test
    void form0581CanceledNotifiesBothOrganizationsExcludingTheActor() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        Form0581 form0581 = mock(Form0581.class);
        when(form0581.getSenderOrganizationId()).thenReturn(3L);
        when(form0581.getReceiverOrganizationId()).thenReturn(5L);
        when(form0581Repository.findById(9L)).thenReturn(Optional.of(form0581));
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(33L, 44L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "SENT", "CANCELED", 22L, "reason"));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).saveAll(captor.capture());

        List<List<Notification>> allSaved = captor.getAllValues();
        assertThat(allSaved.get(0)).extracting(Notification::getRecipientUserId).containsExactly(11L);
        assertThat(allSaved.get(1)).extracting(Notification::getRecipientUserId)
                .containsExactlyInAnyOrder(33L, 44L);
        assertThat(allSaved.stream().flatMap(List::stream))
                .allSatisfy(notification -> assertThat(notification.getType())
                        .isEqualTo(NotificationType.FORM0581_CANCELED));
    }

    @Test
    void form0581CardLinkedNotifiesSenderOrganizationOnlyOnAcceptedToCardLinkedTransition() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "SENT", "CARD_LINKED", 1L, null));
        verify(form0581Repository, never()).findById(any());

        Form0581 form0581 = mock(Form0581.class);
        when(form0581.getSenderOrganizationId()).thenReturn(3L);
        when(form0581Repository.findById(9L)).thenReturn(Optional.of(form0581));
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "ACCEPTED", "CARD_LINKED", 22L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).extracting(Notification::getRecipientUserId).containsExactly(11L);
        assertThat(saved).allSatisfy(notification ->
                assertThat(notification.getType()).isEqualTo(NotificationType.FORM0581_CARD_LINKED));
    }

    @Test
    void form0581ApprovedNotifiesReceiverOrganizationOnApprovedTransition() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        Form0581 form0581 = mock(Form0581.class);
        when(form0581.getReceiverOrganizationId()).thenReturn(5L);
        when(form0581Repository.findById(9L)).thenReturn(Optional.of(form0581));
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(33L, 44L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "CARD_LINKED", "APPROVED", 1L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).extracting(Notification::getRecipientUserId).containsExactlyInAnyOrder(33L, 44L);
        assertThat(saved).allSatisfy(notification ->
                assertThat(notification.getType()).isEqualTo(NotificationType.FORM0581_APPROVED));
    }

    @Test
    void form0581ReopenedNotifiesBothOrganizationsOnCanceledToSentTransition() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        Form0581 form0581 = mock(Form0581.class);
        when(form0581.getSenderOrganizationId()).thenReturn(3L);
        when(form0581.getReceiverOrganizationId()).thenReturn(5L);
        when(form0581Repository.findById(9L)).thenReturn(Optional.of(form0581));
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(33L, 44L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "CANCELED", "SENT", 1L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).saveAll(captor.capture());

        List<List<Notification>> allSaved = captor.getAllValues();
        assertThat(allSaved.get(0)).extracting(Notification::getRecipientUserId).containsExactlyInAnyOrder(11L, 22L);
        assertThat(allSaved.get(1)).extracting(Notification::getRecipientUserId).containsExactlyInAnyOrder(33L, 44L);
        assertThat(allSaved.stream().flatMap(List::stream))
                .allSatisfy(notification -> assertThat(notification.getType())
                        .isEqualTo(NotificationType.FORM0581_REOPENED));
    }

    @Test
    void form0581ReceivedAlsoNotifiesAffiliatedOrganizationsExcludingSenderAndReceiver() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        Patient patient = mock(Patient.class);
        when(patient.getId()).thenReturn(50L);

        Form0581 form0581 = mock(Form0581.class);
        when(form0581.getSenderOrganizationId()).thenReturn(3L);
        when(form0581.getReceiverOrganizationId()).thenReturn(5L);
        when(form0581.getPatient()).thenReturn(patient);
        when(form0581Repository.findById(1L)).thenReturn(Optional.of(form0581));
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(10L));

        // 3L is the sender - already notified separately, must be filtered out here.
        when(patientAffiliationRepository.findDistinctOrganizationIdsByPatientIdAndTypeIn(
                50L, uz.uzinfocom.app.orchestration.scope.FormAccessScopeResolver.AFFILIATION_TYPES
        )).thenReturn(List.of(3L, 7L));
        when(userRepository.findActiveIdsByOrganizationId(7L)).thenReturn(List.of(70L));

        listener.on(new EntityCreatedEvent(AuditEntityType.FORM0581, 1L, 20L));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).saveAll(captor.capture());

        List<List<Notification>> allSaved = captor.getAllValues();
        assertThat(allSaved.get(0)).extracting(Notification::getType).containsExactly(NotificationType.FORM0581_RECEIVED);
        assertThat(allSaved.get(0)).extracting(Notification::getRecipientUserId).containsExactly(10L);
        assertThat(allSaved.get(1)).extracting(Notification::getType).containsExactly(NotificationType.FORM0581_AFFILIATED_RECEIVED);
        assertThat(allSaved.get(1)).extracting(Notification::getRecipientUserId).containsExactly(70L);
    }

    @Test
    void form0581CardLinkedAlsoNotifiesAffiliatedOrganizationsExcludingSenderAndReceiver() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        Patient patient = mock(Patient.class);
        when(patient.getId()).thenReturn(50L);

        Form0581 form0581 = mock(Form0581.class);
        when(form0581.getSenderOrganizationId()).thenReturn(3L);
        when(form0581.getReceiverOrganizationId()).thenReturn(5L);
        when(form0581.getPatient()).thenReturn(patient);
        when(form0581Repository.findById(9L)).thenReturn(Optional.of(form0581));
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L));

        // 5L is the receiver - not notified by this transition at all, so it
        // must be filtered out of the affiliated fan-out too.
        when(patientAffiliationRepository.findDistinctOrganizationIdsByPatientIdAndTypeIn(
                50L, uz.uzinfocom.app.orchestration.scope.FormAccessScopeResolver.AFFILIATION_TYPES
        )).thenReturn(List.of(5L, 7L));
        when(userRepository.findActiveIdsByOrganizationId(7L)).thenReturn(List.of(70L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "ACCEPTED", "CARD_LINKED", 22L, null));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).saveAll(captor.capture());

        List<List<Notification>> allSaved = captor.getAllValues();
        assertThat(allSaved.get(0)).extracting(Notification::getType).containsExactly(NotificationType.FORM0581_CARD_LINKED);
        assertThat(allSaved.get(0)).extracting(Notification::getRecipientUserId).containsExactly(11L);
        assertThat(allSaved.get(1)).extracting(Notification::getType).containsExactly(NotificationType.FORM0581_AFFILIATED_CARD_LINKED);
        assertThat(allSaved.get(1)).extracting(Notification::getRecipientUserId).containsExactly(70L);
    }

    @Test
    void form0581CanceledIsSkippedWhenDisabledBySetting() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(false);

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "SENT", "CANCELED", 22L, "reason"));

        verify(form0581Repository, never()).findById(any());
        verify(notificationRepository, never()).saveAll(any());
    }
}
