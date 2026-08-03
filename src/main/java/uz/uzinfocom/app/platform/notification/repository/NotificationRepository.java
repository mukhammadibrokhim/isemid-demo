package uz.uzinfocom.app.platform.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.notification.domain.Notification;

import java.time.Instant;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientUserIdOrderByOccurredAtDesc(Long recipientUserId, Pageable pageable);

    Page<Notification> findByRecipientUserIdAndReadFalseOrderByOccurredAtDesc(Long recipientUserId, Pageable pageable);

    Optional<Notification> findByIdAndRecipientUserId(Long id, Long recipientUserId);

    long countByRecipientUserIdAndReadFalse(Long recipientUserId);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.read = true, n.readAt = :readAt
            WHERE n.recipientUserId = :recipientUserId
              AND n.read = false
            """)
    int markAllRead(@Param("recipientUserId") Long recipientUserId, @Param("readAt") Instant readAt);
}
