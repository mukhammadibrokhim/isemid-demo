package uz.uzinfocom.app.platform.audit.event;

import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;

import java.time.Instant;
import java.util.Map;

public record FieldsChangedEvent(
        AuditEntityType entityType,
        Long entityId,
        Map<String, Object> changes,
        Long actorUserId,
        Instant occurredAt
) {
    public FieldsChangedEvent(AuditEntityType entityType, Long entityId, Map<String, Object> changes, Long actorUserId) {
        this(entityType, entityId, changes, actorUserId, Instant.now());
    }
}
