package uz.uzinfocom.app.orchestration.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.orchestration.notification.domain.Notification;
import uz.uzinfocom.app.orchestration.notification.domain.NotificationType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientUserIdOrderByOccurredAtDesc(Long recipientUserId, Pageable pageable);

    Page<Notification> findByRecipientUserIdAndReadFalseOrderByOccurredAtDesc(Long recipientUserId, Pageable pageable);

    Optional<Notification> findByIdAndRecipientUserId(Long id, Long recipientUserId);

    long countByRecipientUserIdAndReadFalse(Long recipientUserId);

    @Query("""
            SELECT n.type as type, COUNT(n) as count
            FROM Notification n
            WHERE n.recipientUserId = :recipientUserId
              AND n.read = false
            GROUP BY n.type
            """)
    List<UnreadCountByType> countByRecipientUserIdAndReadFalseGroupedByType(@Param("recipientUserId") Long recipientUserId);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.read = true, n.readAt = :readAt
            WHERE n.recipientUserId = :recipientUserId
              AND n.read = false
            """)
    void markAllRead(@Param("recipientUserId") Long recipientUserId, @Param("readAt") Instant readAt);

    interface UnreadCountByType {
        NotificationType getType();

        long getCount();
    }
}
