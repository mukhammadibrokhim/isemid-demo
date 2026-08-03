package uz.uzinfocom.app.platform.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.persistence.entity.BaseEntity;

import java.time.Instant;

/**
 * One row per (event, recipient) — fanned out at creation time by
 * {@code NotificationEventListener} rather than modeled as one row per event
 * plus a separate per-user read-tracking join, mirroring how {@code Card.users}/
 * {@code Act.users} already model direct assignment in this codebase. Not an
 * {@link uz.uzinfocom.app.platform.persistence.entity.AuditableEntity}: like
 * {@code AuditEvent}, this row IS the record, attributed via
 * {@link #recipientUserId} rather than a separate "creator".
 */
@Getter
@Setter
@Entity
@Table(
        name = "notification",
        indexes = {
                @Index(name = "idx_notification_recipient_read_occurred", columnList = "recipient_user_id,read,occurred_at"),
                @Index(name = "idx_notification_entity", columnList = "entity_type,entity_id")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private AuditEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "message_key", nullable = false, length = 100)
    private String messageKey;

    /** JSON-encoded named params for {@code messageKey}, resolved at read time via MessageResolver. */
    @Column(name = "message_params", length = 2000)
    private String messageParams;

    @Column(name = "read", nullable = false)
    @Builder.Default
    private Boolean read = false;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public void markRead() {
        if (Boolean.TRUE.equals(read)) {
            return;
        }
        read = true;
        readAt = Instant.now();
    }
}
