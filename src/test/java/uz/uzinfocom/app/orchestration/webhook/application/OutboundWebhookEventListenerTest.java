package uz.uzinfocom.app.orchestration.webhook.application;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.NotificationRoutingContext.FormRouting;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboundWebhookEventListenerTest {

    private final IntegrationClientRepository integrationClientRepository = mock(IntegrationClientRepository.class);
    private final OutboundWebhookDispatchService outboundWebhookDispatchService = mock(OutboundWebhookDispatchService.class);
    private final JsonMapper objectMapper = JsonMapper.builder().build();

    private final OutboundWebhookEventListener listener = new OutboundWebhookEventListener(
            integrationClientRepository, outboundWebhookDispatchService, objectMapper);

    @Test
    void doesNothingWhenTheFormHasNoSourceIntegrationClient() {
        listener.on(statusChangedEvent(AuditEntityType.FORM058, 100L, formRouting(null)));

        verify(outboundWebhookDispatchService, never()).enqueue(any(), any(), any(), any(), any(), any());
        verify(integrationClientRepository, never()).findById(any());
    }

    @Test
    void doesNothingWhenTheIntegrationClientIsMissing() {
        when(integrationClientRepository.findById(10L)).thenReturn(Optional.empty());

        listener.on(statusChangedEvent(AuditEntityType.FORM058, 100L, formRouting(10L)));

        verify(outboundWebhookDispatchService, never()).enqueue(any(), any(), any(), any(), any(), any());
    }

    @Test
    void doesNothingWhenTheClientsWebhookIsInactive() {
        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test").organizationId(1L).sourceKey("dmed").name("Test")
                .scopes("form058:submit").active(true).webhookActive(false).build();
        client.setId(10L);
        when(integrationClientRepository.findById(10L)).thenReturn(Optional.of(client));

        listener.on(statusChangedEvent(AuditEntityType.FORM058, 100L, formRouting(10L)));

        verify(outboundWebhookDispatchService, never()).enqueue(any(), any(), any(), any(), any(), any());
    }

    @Test
    void doesNothingWhenTheClientItselfIsRevoked() {
        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test").organizationId(1L).sourceKey("dmed").name("Test")
                .scopes("form058:submit").active(false).webhookActive(true).build();
        client.setId(10L);
        when(integrationClientRepository.findById(10L)).thenReturn(Optional.of(client));

        listener.on(statusChangedEvent(AuditEntityType.FORM058, 100L, formRouting(10L)));

        verify(outboundWebhookDispatchService, never()).enqueue(any(), any(), any(), any(), any(), any());
    }

    @Test
    void enqueuesADispatchWhenTheSourceClientHasAnActiveWebhook() {
        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test").organizationId(1L).sourceKey("dmed").name("Test")
                .scopes("form058:submit").active(true).webhookActive(true).build();
        client.setId(10L);
        when(integrationClientRepository.findById(10L)).thenReturn(Optional.of(client));

        listener.on(statusChangedEvent(AuditEntityType.FORM058, 100L, formRouting(10L)));

        verify(outboundWebhookDispatchService, times(1)).enqueue(
                eq(10L), eq(AuditEntityType.FORM058), eq(100L), eq("SENT"), eq("APPROVED"), any());
    }

    @Test
    void enqueuesForForm0581TooWhenItsSourceClientHasAnActiveWebhook() {
        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test2").organizationId(1L).sourceKey("dmed").name("Test")
                .scopes("form0581:submit").active(true).webhookActive(true).build();
        client.setId(20L);
        when(integrationClientRepository.findById(20L)).thenReturn(Optional.of(client));

        listener.on(statusChangedEvent(AuditEntityType.FORM0581, 200L, formRouting(20L)));

        verify(outboundWebhookDispatchService, times(1)).enqueue(
                eq(20L), eq(AuditEntityType.FORM0581), eq(200L), eq("SENT"), eq("APPROVED"), any());
    }

    @Test
    void ignoresEntityTypesOtherThanFormsEntirely() {
        listener.on(statusChangedEvent(AuditEntityType.ACT, 1L, null));

        verify(integrationClientRepository, never()).findById(any());
        verify(outboundWebhookDispatchService, never()).enqueue(any(), any(), any(), any(), any(), any());
    }

    private StatusChangedEvent statusChangedEvent(AuditEntityType entityType, Long entityId, FormRouting routing) {
        return new StatusChangedEvent(entityType, entityId, "SENT", "APPROVED", 1L, null, routing);
    }

    private FormRouting formRouting(Long sourceIntegrationClientId) {
        return new FormRouting(3L, 5L, List.of(), sourceIntegrationClientId);
    }
}
