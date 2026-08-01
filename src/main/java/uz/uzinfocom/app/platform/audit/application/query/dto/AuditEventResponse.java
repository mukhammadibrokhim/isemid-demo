package uz.uzinfocom.app.platform.audit.application.query.dto;

import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.domain.AuditEventType;

import java.time.Instant;

public record AuditEventResponse(
        Long id,
        AuditEventType eventType,
        AuditEntityType entityType,
        Long entityId,
        String oldStatus,
        String newStatus,
        Long oldOrgId,
        Long newOrgId,
        Long actorUserId,
        String reason,
        Instant occurredAt
) {
}
