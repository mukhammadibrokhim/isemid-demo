package uz.uzinfocom.app.orchestration.notification.application;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.EntityCreatedEvent;
import uz.uzinfocom.app.platform.audit.event.NotificationRoutingContext.ActRouting;
import uz.uzinfocom.app.platform.audit.event.NotificationRoutingContext.CardRouting;
import uz.uzinfocom.app.platform.audit.event.NotificationRoutingContext.FormRouting;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.export.domain.event.ExportJobCompletedEvent;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.orchestration.notification.domain.Notification;
import uz.uzinfocom.app.orchestration.notification.domain.NotificationType;
import uz.uzinfocom.app.orchestration.notification.repository.NotificationRepository;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationEventListenerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final SystemSettingResolver systemSettingResolver = mock(SystemSettingResolver.class);

    private final NotificationEventListener listener = new NotificationEventListener(
            userRepository, notificationRepository, systemSettingResolver, JsonMapper.builder().build()
    );

    @Test
    void formReceivedFansOutToActiveOrganizationUsersExcludingTheActor() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(10L, 20L, 30L));

        listener.on(new EntityCreatedEvent(AuditEntityType.FORM058, 1L, 20L, formRouting(3L, 5L)));

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

        listener.on(new EntityCreatedEvent(AuditEntityType.FORM058, 1L, 20L, formRouting(3L, 5L)));

        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void formReceivedAlsoNotifiesAffiliatedOrganizationsExcludingSenderAndReceiver() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(10L));
        when(userRepository.findActiveIdsByOrganizationId(7L)).thenReturn(List.of(70L));

        // 3L is the sender - the publisher already excludes it from affiliatedOrganizationIds.
        listener.on(new EntityCreatedEvent(AuditEntityType.FORM058, 1L, 20L,
                new FormRouting(3L, 5L, List.of(7L), null)));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, times(2)).saveAll(captor.capture());

        List<List<Notification>> allSaved = captor.getAllValues();
        assertThat(allSaved.get(0)).extracting(Notification::getType).containsExactly(NotificationType.FORM058_RECEIVED);
        assertThat(allSaved.get(0)).extracting(Notification::getRecipientUserId).containsExactly(10L);
        assertThat(allSaved.get(1)).extracting(Notification::getType).containsExactly(NotificationType.FORM058_AFFILIATED_RECEIVED);
        assertThat(allSaved.get(1)).extracting(Notification::getRecipientUserId).containsExactly(70L);
    }

    @Test
    void cardAssignedNotifiesAttachedUsersExcludingTheAssigner() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);

        listener.on(new EntityCreatedEvent(AuditEntityType.CARD, 7L, 99L,
                new CardRouting(5L, List.of(99L, 100L), 99L)));

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

        listener.on(new StatusChangedEvent(AuditEntityType.CARD, 7L, "ACCEPTED_BY_USER", "IN_PROGRESS", 100L, null,
                new CardRouting(null, List.of(), 99L)));
        verify(notificationRepository, never()).saveAll(any());

        listener.on(new StatusChangedEvent(AuditEntityType.CARD, 7L, "NEW", "ACCEPTED_BY_USER", 100L, null,
                new CardRouting(null, List.of(), 99L)));

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

        listener.on(new StatusChangedEvent(AuditEntityType.CARD, 7L, "ACCEPTED_BY_USER", "REJECTED_BY_USER", 100L, null,
                new CardRouting(null, List.of(), 99L)));

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

        // COMPLETED is reachable from ACCEPTED_BY_USER, IN_PROGRESS, or REJECTED
        // (rework) — matched on the new status alone, same as Form058Approved.
        listener.on(new StatusChangedEvent(AuditEntityType.CARD, 7L, "REJECTED", "COMPLETED", 100L, null,
                new CardRouting(null, List.of(), 99L)));

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

        listener.on(new StatusChangedEvent(AuditEntityType.CARD, 7L, "COMPLETED", "APPROVED", 99L, null,
                new CardRouting(null, List.of(100L), null)));

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

        listener.on(new StatusChangedEvent(AuditEntityType.CARD, 7L, "COMPLETED", "REJECTED", 99L, "Incomplete",
                new CardRouting(null, List.of(100L), null)));

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

        listener.on(new StatusChangedEvent(AuditEntityType.ACT, 3L, "IN_PROGRESS", "READY", 1L, null,
                new ActRouting(null, List.of())));
        verify(notificationRepository, never()).saveAll(any());

        listener.on(new StatusChangedEvent(AuditEntityType.ACT, 3L, "SENT", "COMPLETED", 1L, null,
                new ActRouting(null, List.of(200L))));

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
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "SENT", "CARD_LINKED", 1L, null,
                formRouting(3L, 5L)));
        verify(notificationRepository, never()).saveAll(any());

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "SENT", "ACCEPTED", 22L, null,
                formRouting(3L, 5L)));

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
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(33L, 44L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "SENT", "CANCELED", 22L, "reason",
                formRouting(3L, 5L)));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, times(2)).saveAll(captor.capture());

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
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "SENT", "CARD_LINKED", 1L, null,
                formRouting(3L, 5L)));
        verify(notificationRepository, never()).saveAll(any());

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "ACCEPTED", "CARD_LINKED", 22L, null,
                formRouting(3L, 5L)));

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
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L));
        when(userRepository.findActiveIdsByOrganizationId(7L)).thenReturn(List.of(70L));

        // 5L is the receiver - not notified by this transition at all (see
        // form058CardLinkedNotifiesSenderOrganizationOnlyOnAcceptedToCardLinkedTransition),
        // so the publisher already excludes it from affiliatedOrganizationIds too.
        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "ACCEPTED", "CARD_LINKED", 22L, null,
                new FormRouting(3L, 5L, List.of(7L), null)));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, times(2)).saveAll(captor.capture());

        List<List<Notification>> allSaved = captor.getAllValues();
        assertThat(allSaved.get(0)).extracting(Notification::getType).containsExactly(NotificationType.FORM058_CARD_LINKED);
        assertThat(allSaved.get(0)).extracting(Notification::getRecipientUserId).containsExactly(11L);
        assertThat(allSaved.get(1)).extracting(Notification::getType).containsExactly(NotificationType.FORM058_AFFILIATED_CARD_LINKED);
        assertThat(allSaved.get(1)).extracting(Notification::getRecipientUserId).containsExactly(70L);
    }

    @Test
    void form058ApprovedNotifiesReceiverOrganizationOnApprovedTransition() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(33L, 44L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "CARD_LINKED", "APPROVED", 1L, null,
                formRouting(3L, 5L)));

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
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(33L, 44L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "CANCELED", "SENT", 1L, null,
                formRouting(3L, 5L)));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, times(2)).saveAll(captor.capture());

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

        listener.on(new StatusChangedEvent(AuditEntityType.FORM058, 9L, "SENT", "CANCELED", 22L, "reason",
                formRouting(3L, 5L)));

        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void form0581AcknowledgedNotifiesSenderOrganizationOnlyOnSentToAcceptedTransition() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "SENT", "CARD_LINKED", 1L, null,
                formRouting(3L, 5L)));
        verify(notificationRepository, never()).saveAll(any());

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "SENT", "ACCEPTED", 22L, null,
                formRouting(3L, 5L)));

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
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(33L, 44L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "SENT", "CANCELED", 22L, "reason",
                formRouting(3L, 5L)));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, times(2)).saveAll(captor.capture());

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
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "SENT", "CARD_LINKED", 1L, null,
                formRouting(3L, 5L)));
        verify(notificationRepository, never()).saveAll(any());

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "ACCEPTED", "CARD_LINKED", 22L, null,
                formRouting(3L, 5L)));

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
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(33L, 44L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "CARD_LINKED", "APPROVED", 1L, null,
                formRouting(3L, 5L)));

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
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L, 22L));
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(33L, 44L));

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "CANCELED", "SENT", 1L, null,
                formRouting(3L, 5L)));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, times(2)).saveAll(captor.capture());

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
        when(userRepository.findActiveIdsByOrganizationId(5L)).thenReturn(List.of(10L));
        when(userRepository.findActiveIdsByOrganizationId(7L)).thenReturn(List.of(70L));

        // 3L is the sender - the publisher already excludes it from affiliatedOrganizationIds.
        listener.on(new EntityCreatedEvent(AuditEntityType.FORM0581, 1L, 20L,
                new FormRouting(3L, 5L, List.of(7L), null)));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, times(2)).saveAll(captor.capture());

        List<List<Notification>> allSaved = captor.getAllValues();
        assertThat(allSaved.get(0)).extracting(Notification::getType).containsExactly(NotificationType.FORM0581_RECEIVED);
        assertThat(allSaved.get(0)).extracting(Notification::getRecipientUserId).containsExactly(10L);
        assertThat(allSaved.get(1)).extracting(Notification::getType).containsExactly(NotificationType.FORM0581_AFFILIATED_RECEIVED);
        assertThat(allSaved.get(1)).extracting(Notification::getRecipientUserId).containsExactly(70L);
    }

    @Test
    void form0581CardLinkedAlsoNotifiesAffiliatedOrganizationsExcludingSenderAndReceiver() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(true);
        when(userRepository.findActiveIdsByOrganizationId(3L)).thenReturn(List.of(11L));
        when(userRepository.findActiveIdsByOrganizationId(7L)).thenReturn(List.of(70L));

        // 5L is the receiver - not notified by this transition at all, so the
        // publisher already excludes it from affiliatedOrganizationIds too.
        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "ACCEPTED", "CARD_LINKED", 22L, null,
                new FormRouting(3L, 5L, List.of(7L), null)));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, times(2)).saveAll(captor.capture());

        List<List<Notification>> allSaved = captor.getAllValues();
        assertThat(allSaved.get(0)).extracting(Notification::getType).containsExactly(NotificationType.FORM0581_CARD_LINKED);
        assertThat(allSaved.get(0)).extracting(Notification::getRecipientUserId).containsExactly(11L);
        assertThat(allSaved.get(1)).extracting(Notification::getType).containsExactly(NotificationType.FORM0581_AFFILIATED_CARD_LINKED);
        assertThat(allSaved.get(1)).extracting(Notification::getRecipientUserId).containsExactly(70L);
    }

    @Test
    void form0581CanceledIsSkippedWhenDisabledBySetting() {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean())).thenReturn(false);

        listener.on(new StatusChangedEvent(AuditEntityType.FORM0581, 9L, "SENT", "CANCELED", 22L, "reason",
                formRouting(3L, 5L)));

        verify(notificationRepository, never()).saveAll(any());
    }

    private FormRouting formRouting(Long senderOrganizationId, Long receiverOrganizationId) {
        return new FormRouting(senderOrganizationId, receiverOrganizationId, List.of(), null);
    }
}
