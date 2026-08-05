package uz.uzinfocom.app.platform.webhook.application;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboundWebhookEventListenerTest {

    private final Form058JpaRepository form058Repository = mock(Form058JpaRepository.class);
    private final Form0581JpaRepository form0581Repository = mock(Form0581JpaRepository.class);
    private final IntegrationClientRepository integrationClientRepository = mock(IntegrationClientRepository.class);
    private final OutboundWebhookDispatchService outboundWebhookDispatchService = mock(OutboundWebhookDispatchService.class);
    private final JsonMapper objectMapper = JsonMapper.builder().build();

    private final OutboundWebhookEventListener listener = new OutboundWebhookEventListener(
            form058Repository, form0581Repository, integrationClientRepository,
            outboundWebhookDispatchService, objectMapper);

    @Test
    void doesNothingWhenTheFormHasNoSourceIntegrationClient() {
        Form058 form058 = Form058.builder().sourceIntegrationClientId(null).build();
        form058.setId(100L);
        when(form058Repository.findById(100L)).thenReturn(Optional.of(form058));

        listener.on(statusChangedEvent(AuditEntityType.FORM058, 100L));

        verify(outboundWebhookDispatchService, never()).enqueue(any(), any(), any(), any(), any(), any());
        verify(integrationClientRepository, never()).findById(any());
    }

    @Test
    void doesNothingWhenTheFormIsNotFound() {
        when(form058Repository.findById(100L)).thenReturn(Optional.empty());

        listener.on(statusChangedEvent(AuditEntityType.FORM058, 100L));

        verify(outboundWebhookDispatchService, never()).enqueue(any(), any(), any(), any(), any(), any());
    }

    @Test
    void doesNothingWhenTheIntegrationClientIsMissing() {
        Form058 form058 = Form058.builder().sourceIntegrationClientId(10L).build();
        form058.setId(100L);
        when(form058Repository.findById(100L)).thenReturn(Optional.of(form058));
        when(integrationClientRepository.findById(10L)).thenReturn(Optional.empty());

        listener.on(statusChangedEvent(AuditEntityType.FORM058, 100L));

        verify(outboundWebhookDispatchService, never()).enqueue(any(), any(), any(), any(), any(), any());
    }

    @Test
    void doesNothingWhenTheClientsWebhookIsInactive() {
        Form058 form058 = Form058.builder().sourceIntegrationClientId(10L).build();
        form058.setId(100L);
        when(form058Repository.findById(100L)).thenReturn(Optional.of(form058));

        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test").organizationId(1L).sourceKey("dmed").name("Test")
                .scopes("form058:submit").active(true).webhookActive(false).build();
        client.setId(10L);
        when(integrationClientRepository.findById(10L)).thenReturn(Optional.of(client));

        listener.on(statusChangedEvent(AuditEntityType.FORM058, 100L));

        verify(outboundWebhookDispatchService, never()).enqueue(any(), any(), any(), any(), any(), any());
    }

    @Test
    void doesNothingWhenTheClientItselfIsRevoked() {
        Form058 form058 = Form058.builder().sourceIntegrationClientId(10L).build();
        form058.setId(100L);
        when(form058Repository.findById(100L)).thenReturn(Optional.of(form058));

        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test").organizationId(1L).sourceKey("dmed").name("Test")
                .scopes("form058:submit").active(false).webhookActive(true).build();
        client.setId(10L);
        when(integrationClientRepository.findById(10L)).thenReturn(Optional.of(client));

        listener.on(statusChangedEvent(AuditEntityType.FORM058, 100L));

        verify(outboundWebhookDispatchService, never()).enqueue(any(), any(), any(), any(), any(), any());
    }

    @Test
    void enqueuesADispatchWhenTheSourceClientHasAnActiveWebhook() {
        Form058 form058 = Form058.builder().sourceIntegrationClientId(10L).build();
        form058.setId(100L);
        when(form058Repository.findById(100L)).thenReturn(Optional.of(form058));

        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test").organizationId(1L).sourceKey("dmed").name("Test")
                .scopes("form058:submit").active(true).webhookActive(true).build();
        client.setId(10L);
        when(integrationClientRepository.findById(10L)).thenReturn(Optional.of(client));

        listener.on(statusChangedEvent(AuditEntityType.FORM058, 100L));

        verify(outboundWebhookDispatchService, times(1)).enqueue(
                eq(10L), eq(AuditEntityType.FORM058), eq(100L), eq("SENT"), eq("APPROVED"), any());
    }

    @Test
    void enqueuesForForm0581TooWhenItsSourceClientHasAnActiveWebhook() {
        Form0581 form0581 = Form0581.builder().sourceIntegrationClientId(20L).build();
        form0581.setId(200L);
        when(form0581Repository.findById(200L)).thenReturn(Optional.of(form0581));

        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test2").organizationId(1L).sourceKey("dmed").name("Test")
                .scopes("form0581:submit").active(true).webhookActive(true).build();
        client.setId(20L);
        when(integrationClientRepository.findById(20L)).thenReturn(Optional.of(client));

        listener.on(statusChangedEvent(AuditEntityType.FORM0581, 200L));

        verify(outboundWebhookDispatchService, times(1)).enqueue(
                eq(20L), eq(AuditEntityType.FORM0581), eq(200L), eq("SENT"), eq("APPROVED"), any());
    }

    @Test
    void ignoresEntityTypesOtherThanFormsEntirely() {
        listener.on(statusChangedEvent(AuditEntityType.ACT, 1L));

        verify(form058Repository, never()).findById(any());
        verify(form0581Repository, never()).findById(any());
        verify(outboundWebhookDispatchService, never()).enqueue(any(), any(), any(), any(), any(), any());
    }

    private StatusChangedEvent statusChangedEvent(AuditEntityType entityType, Long entityId) {
        return new StatusChangedEvent(entityType, entityId, "SENT", "APPROVED", 1L, null);
    }
}
