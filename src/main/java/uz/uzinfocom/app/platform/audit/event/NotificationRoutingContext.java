package uz.uzinfocom.app.platform.audit.event;

import java.util.List;

/**
 * Carried on {@link EntityCreatedEvent}/{@link StatusChangedEvent} so
 * {@code orchestration.notification}/{@code orchestration.webhook} can route
 * without re-fetching the module entity the event is about — the publisher
 * already has it loaded at the moment of {@code publishEvent(...)}.
 * {@code AuditEventListener} ignores this field entirely.
 */
public sealed interface NotificationRoutingContext {

    /**
     * {@code affiliatedOrganizationIds} is pre-filtered to exclude
     * {@code senderOrganizationId}/{@code receiverOrganizationId} (they get
     * their own dedicated notification) and left empty by publishers whose
     * transition never triggers an affiliated-organization notification —
     * see each publish call site for which case applies.
     */
    record FormRouting(
            Long senderOrganizationId,
            Long receiverOrganizationId,
            List<Long> affiliatedOrganizationIds,
            Long sourceIntegrationClientId
    ) implements NotificationRoutingContext {
    }

    record CardRouting(Long organizationId, List<Long> userIds, Long assignedById)
            implements NotificationRoutingContext {
    }

    record ActRouting(Long organizationId, List<Long> userIds)
            implements NotificationRoutingContext {
    }
}
