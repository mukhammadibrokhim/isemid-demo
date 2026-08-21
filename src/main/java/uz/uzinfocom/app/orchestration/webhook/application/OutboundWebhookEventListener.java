package uz.uzinfocom.app.orchestration.webhook.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.NotificationRoutingContext.FormRouting;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;

import java.time.Instant;

/**
 * Consumes the same {@code StatusChangedEvent} the audit trail and
 * {@code NotificationEventListener} already do, and enqueues an outbound
 * webhook dispatch for FORM058/FORM0581 - never sends synchronously, only
 * ever calls {@link OutboundWebhookDispatchService#enqueue}, so the actual
 * HTTP attempt (first try and every retry alike) always goes through
 * {@code OutboundWebhookDispatchScheduler} /
 * {@code OutboundWebhookDispatchSendOrchestrator}.
 *
 * <p>The routing key ({@code sourceIntegrationClientId}) comes from the
 * event's {@code routing()} field, set by the publisher at the moment it
 * already has the form loaded — this listener never re-fetches
 * {@code Form058}/{@code Form0581} itself, deliberately keeping {@code
 * orchestration.webhook} free of any {@code modules.*} repository
 * dependency. The webhook payload doesn't need the form either — it's a
 * small self-contained record built entirely from the event's own fields.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundWebhookEventListener {

    private final IntegrationClientRepository integrationClientRepository;
    private final OutboundWebhookDispatchService outboundWebhookDispatchService;
    private final JsonMapper objectMapper;

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(StatusChangedEvent event) {
        switch (event.entityType()) {
            case FORM058 -> enqueueIfWebhookActive(event, AuditEntityType.FORM058);
            case FORM0581 -> enqueueIfWebhookActive(event, AuditEntityType.FORM0581);
            case FORM129 -> enqueueIfWebhookActive(event, AuditEntityType.FORM129);
            default -> { }
        }
    }

    private void enqueueIfWebhookActive(StatusChangedEvent event, AuditEntityType entityType) {
        FormRouting routing = (FormRouting) event.routing();
        Long sourceIntegrationClientId = routing.sourceIntegrationClientId();
        if (sourceIntegrationClientId == null) {
            return;
        }

        IntegrationClient client = integrationClientRepository.findById(sourceIntegrationClientId).orElse(null);
        if (client == null || !client.isActive() || !client.isWebhookActive()) {
            return;
        }

        String payload = buildPayload(entityType, event);
        if (payload == null) {
            return;
        }

        outboundWebhookDispatchService.enqueue(
                sourceIntegrationClientId, entityType, event.entityId(),
                event.oldStatus(), event.newStatus(), payload
        );
    }

    private String buildPayload(AuditEntityType entityType, StatusChangedEvent event) {
        WebhookPayload payload = new WebhookPayload(
                entityType.name(), event.entityId(), event.oldStatus(), event.newStatus(), Instant.now()
        );
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException serializationFailure) {
            log.error("event=webhook_dispatch_payload_serialization_failure entityType={} entityId={} failureType={}",
                    entityType, event.entityId(), serializationFailure.getClass().getName());
            return null;
        }
    }

    private record WebhookPayload(String entityType, Long entityId, String oldStatus, String newStatus, Instant occurredAt) {
    }
}
