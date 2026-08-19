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
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
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
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundWebhookEventListener {

    private final Form058JpaRepository form058Repository;
    private final Form0581JpaRepository form0581Repository;
    private final IntegrationClientRepository integrationClientRepository;
    private final OutboundWebhookDispatchService outboundWebhookDispatchService;
    private final JsonMapper objectMapper;

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(StatusChangedEvent event) {
        switch (event.entityType()) {
            case FORM058 -> handleForm058(event);
            case FORM0581 -> handleForm0581(event);
            default -> { }
        }
    }

    private void handleForm058(StatusChangedEvent event) {
        Form058 form058 = form058Repository.findById(event.entityId()).orElse(null);
        if (form058 == null) {
            log.warn("event=webhook_dispatch_source_not_found entityType=FORM058 entityId={}", event.entityId());
            return;
        }
        enqueueIfWebhookActive(event, AuditEntityType.FORM058, form058.getSourceIntegrationClientId());
    }

    private void handleForm0581(StatusChangedEvent event) {
        Form0581 form0581 = form0581Repository.findById(event.entityId()).orElse(null);
        if (form0581 == null) {
            log.warn("event=webhook_dispatch_source_not_found entityType=FORM0581 entityId={}", event.entityId());
            return;
        }
        enqueueIfWebhookActive(event, AuditEntityType.FORM0581, form0581.getSourceIntegrationClientId());
    }

    private void enqueueIfWebhookActive(StatusChangedEvent event, AuditEntityType entityType, Long sourceIntegrationClientId) {
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
