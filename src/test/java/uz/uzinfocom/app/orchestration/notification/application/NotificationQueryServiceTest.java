package uz.uzinfocom.app.orchestration.notification.application;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.orchestration.notification.domain.NotificationType;
import uz.uzinfocom.app.orchestration.notification.repository.NotificationRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationQueryServiceTest {

    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final MessageResolver messageResolver = mock(MessageResolver.class);

    private final NotificationQueryService service = new NotificationQueryService(
            notificationRepository, messageResolver, JsonMapper.builder().build()
    );

    @Test
    void countUnreadByTypeDefaultsEveryTypeToZeroAndFillsInWhatIsFound() {
        NotificationRepository.UnreadCountByType formReceived = mock(NotificationRepository.UnreadCountByType.class);
        when(formReceived.getType()).thenReturn(NotificationType.FORM058_RECEIVED);
        when(formReceived.getCount()).thenReturn(3L);

        NotificationRepository.UnreadCountByType exportReady = mock(NotificationRepository.UnreadCountByType.class);
        when(exportReady.getType()).thenReturn(NotificationType.EXPORT_READY);
        when(exportReady.getCount()).thenReturn(2L);

        when(notificationRepository.countByRecipientUserIdAndReadFalseGroupedByType(7L))
                .thenReturn(List.of(formReceived, exportReady));

        Map<NotificationType, Long> counts = service.countUnreadByType(7L);

        assertThat(counts).hasSize(NotificationType.values().length);
        assertThat(counts.get(NotificationType.FORM058_RECEIVED)).isEqualTo(3L);
        assertThat(counts.get(NotificationType.EXPORT_READY)).isEqualTo(2L);
        assertThat(counts.get(NotificationType.CARD_ASSIGNED)).isEqualTo(0L);
        assertThat(counts.get(NotificationType.ACT_ASSIGNED)).isEqualTo(0L);
    }
}
