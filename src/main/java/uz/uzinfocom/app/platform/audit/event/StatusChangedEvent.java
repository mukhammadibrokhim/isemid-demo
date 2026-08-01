package uz.uzinfocom.app.platform.audit.event;

import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;

import java.time.Instant;

public record StatusChangedEvent(
        AuditEntityType entityType,
        Long entityId,
        String oldStatus,
        String newStatus,
        Long actorUserId,
        String reason,
        Instant occurredAt
) {
    public StatusChangedEvent(
            AuditEntityType entityType, Long entityId, String oldStatus, String newStatus,
            Long actorUserId, String reason
    ) {
        this(entityType, entityId, oldStatus, newStatus, actorUserId, reason, Instant.now());
    }
}
