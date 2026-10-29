package uz.uzinfocom.app.orchestration.notification.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.orchestration.notification.application.exception.NotificationNotFoundException;
import uz.uzinfocom.app.orchestration.notification.domain.Notification;
import uz.uzinfocom.app.orchestration.notification.repository.NotificationRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void markRead(Long notificationId, Long currentUserId) {
        Notification notification = notificationRepository.findByIdAndRecipientUserId(notificationId, currentUserId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        notification.markRead();
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(Long currentUserId) {
        notificationRepository.markAllRead(currentUserId, Instant.now());
    }
}
