package uz.uzinfocom.app.platform.audit.event;

import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;

import java.time.Instant;

public record EntityCreatedEvent(
        AuditEntityType entityType,
        Long entityId,
        Long actorUserId,
        Instant occurredAt
) {
    public EntityCreatedEvent(AuditEntityType entityType, Long entityId, Long actorUserId) {
        this(entityType, entityId, actorUserId, Instant.now());
    }
}
