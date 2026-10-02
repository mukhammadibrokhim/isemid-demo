package uz.uzinfocom.app.platform.audit.event;

import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;

import java.time.Instant;

public record OrganizationReassignedEvent(
        AuditEntityType entityType,
        Long entityId,
        Long oldOrgId,
        Long newOrgId,
        Long actorUserId,
        Instant occurredAt
) {
    public OrganizationReassignedEvent(
            AuditEntityType entityType, Long entityId, Long oldOrgId, Long newOrgId, Long actorUserId
    ) {
        this(entityType, entityId, oldOrgId, newOrgId, actorUserId, Instant.now());
    }
}
