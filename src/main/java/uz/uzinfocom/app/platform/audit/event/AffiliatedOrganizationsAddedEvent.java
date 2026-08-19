package uz.uzinfocom.app.platform.audit.event;

import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;

import java.time.Instant;
import java.util.List;

/**
 * A Form058/Form0581 update gave one or more organizations affiliation-based
 * visibility (via {@code GET /v1/form-058(-1)/affiliated}) that they didn't
 * have before - either a brand-new {@code PatientAffiliation} was added, or
 * an existing one's {@code organizationId} was changed to point at them.
 * {@code organizationIds} never includes the form's sender/receiver at the
 * time of the update - those already have their own notification.
 * Not consumed by {@code AuditEventListener} (no matching {@code
 * eventType}) - purely a {@code NotificationEventListener} trigger, unlike
 * this package's other events.
 */
public record AffiliatedOrganizationsAddedEvent(
        AuditEntityType entityType,
        Long entityId,
        List<Long> organizationIds,
        Long actorUserId,
        Instant occurredAt
) {
    public AffiliatedOrganizationsAddedEvent(
            AuditEntityType entityType, Long entityId, List<Long> organizationIds, Long actorUserId
    ) {
        this(entityType, entityId, organizationIds, actorUserId, Instant.now());
    }
}
