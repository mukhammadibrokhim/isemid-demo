package uz.uzinfocom.app.platform.notification.application;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActRepository;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.EntityCreatedEvent;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.export.domain.event.ExportJobCompletedEvent;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.notification.domain.Notification;
import uz.uzinfocom.app.platform.notification.domain.NotificationType;
import uz.uzinfocom.app.platform.notification.repository.NotificationRepository;
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

    private final NotificationEventListener listener = new NotificationEventListener(
            form058Repository, form0581Repository, cardRepository, actRepository,
            userRepository, notificationRepository, systemSettingResolver, JsonMapper.builder().build()
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
}
