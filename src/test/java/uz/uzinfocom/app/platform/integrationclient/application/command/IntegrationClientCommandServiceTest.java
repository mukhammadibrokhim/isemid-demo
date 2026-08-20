package uz.uzinfocom.app.platform.integrationclient.application.command;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.uzinfocom.app.orchestration.webhook.crypto.WebhookSecretCipher;
import uz.uzinfocom.app.platform.integrationclient.application.OrganizationLookup;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientAllowedIpsUpdateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientCreateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientCreateResponse;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientUpdateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientWebhookUpdateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.exception.AllowedIpsRequiredException;
import uz.uzinfocom.app.platform.integrationclient.application.exception.InvalidAllowedIpException;
import uz.uzinfocom.app.platform.integrationclient.application.exception.InvalidWebhookConfigException;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.platform.integrationclient.domain.OutboundHttpMethod;
import uz.uzinfocom.app.platform.integrationclient.domain.OutboundWebhookAuthType;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IntegrationClientCommandServiceTest {

    private final IntegrationClientRepository integrationClientRepository = mock(IntegrationClientRepository.class);
    private final OrganizationLookup organizationLookup = mock(OrganizationLookup.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final WebhookSecretCipher webhookSecretCipher = mock(WebhookSecretCipher.class);

    private final IntegrationClientCommandService service = new IntegrationClientCommandService(
            integrationClientRepository, organizationLookup, passwordEncoder, webhookSecretCipher);

    @Test
    void createReturnsThePlaintextSecretOnlyOnceAndNeverPersistsIt() {
        UUID organizationUuid = UUID.randomUUID();
        when(organizationLookup.resolveActiveId(organizationUuid)).thenReturn(42L);
        when(integrationClientRepository.save(any(IntegrationClient.class)))
                .thenAnswer(invocation -> {
                    IntegrationClient client = invocation.getArgument(0);
                    client.setId(1L);
                    return client;
                });

        IntegrationClientCreateResponse response = service.create(new IntegrationClientCreateRequest(
                "Test Lab System", IntegrationAuthType.CLIENT_CREDENTIALS, "dmed", organizationUuid,
                List.of(IntegrationScope.FORM058_SUBMIT), null));

        assertThat(response.clientSecret()).isNotBlank();
        assertThat(response.apiKey()).isNull();
        assertThat(response.basicAuthSecret()).isNull();
        assertThat(response.organizationId()).isEqualTo(42L);
        assertThat(response.scopes()).containsExactly("form058:submit");
        assertThat(response.allowedIps()).isEmpty();

        ArgumentCaptor<IntegrationClient> savedClient = ArgumentCaptor.forClass(IntegrationClient.class);
        verify(integrationClientRepository).save(savedClient.capture());

        // The persisted entity carries only the BCrypt hash - never the plaintext secret
        // returned in the response above.
        assertThat(savedClient.getValue().getClientSecretHash()).isNotEqualTo(response.clientSecret());
        assertThat(passwordEncoder.matches(response.clientSecret(), savedClient.getValue().getClientSecretHash()))
                .isTrue();
    }

    @Test
    void createNormalizesAndPersistsAllowedIps() {
        UUID organizationUuid = UUID.randomUUID();
        when(organizationLookup.resolveActiveId(organizationUuid)).thenReturn(42L);
        when(integrationClientRepository.save(any(IntegrationClient.class)))
                .thenAnswer(invocation -> {
                    IntegrationClient client = invocation.getArgument(0);
                    client.setId(1L);
                    return client;
                });

        IntegrationClientCreateResponse response = service.create(new IntegrationClientCreateRequest(
                "Test Lab System", IntegrationAuthType.CLIENT_CREDENTIALS, "dmed", organizationUuid,
                List.of(IntegrationScope.FORM058_SUBMIT), List.of(" 10.0.5.0/24 ", "10.0.6.7")));

        assertThat(response.allowedIps()).containsExactly("10.0.5.0/24", "10.0.6.7");
    }

    @Test
    void createRejectsAMalformedAllowedIpEntry() {
        UUID organizationUuid = UUID.randomUUID();
        when(organizationLookup.resolveActiveId(organizationUuid)).thenReturn(42L);

        assertThatThrownBy(() -> service.create(new IntegrationClientCreateRequest(
                "Test Lab System", IntegrationAuthType.CLIENT_CREDENTIALS, "dmed", organizationUuid,
                List.of(IntegrationScope.FORM058_SUBMIT), List.of("not-an-ip"))))
                .isInstanceOf(InvalidAllowedIpException.class);
    }

    @Test
    void createIssuesAnApiKeyPrefixedByTheClientIdAndPersistsOnlyItsHash() {
        UUID organizationUuid = UUID.randomUUID();
        when(organizationLookup.resolveActiveId(organizationUuid)).thenReturn(42L);
        when(integrationClientRepository.save(any(IntegrationClient.class)))
                .thenAnswer(invocation -> {
                    IntegrationClient client = invocation.getArgument(0);
                    client.setId(1L);
                    return client;
                });

        IntegrationClientCreateResponse response = service.create(new IntegrationClientCreateRequest(
                "Test Lab System", IntegrationAuthType.API_KEY, "dmed", organizationUuid,
                List.of(IntegrationScope.FORM058_SUBMIT), null));

        assertThat(response.apiKey()).startsWith(response.clientId() + ".");
        assertThat(response.clientSecret()).isNull();
        assertThat(response.basicAuthSecret()).isNull();

        ArgumentCaptor<IntegrationClient> savedClient = ArgumentCaptor.forClass(IntegrationClient.class);
        verify(integrationClientRepository).save(savedClient.capture());

        assertThat(savedClient.getValue().getClientSecretHash()).isNull();
        String tokenHalf = response.apiKey().substring(response.clientId().length() + 1);
        assertThat(passwordEncoder.matches(tokenHalf, savedClient.getValue().getApiKeyHash())).isTrue();
    }

    @Test
    void createIssuesABasicAuthSecretAndPersistsOnlyItsHash() {
        UUID organizationUuid = UUID.randomUUID();
        when(organizationLookup.resolveActiveId(organizationUuid)).thenReturn(42L);
        when(integrationClientRepository.save(any(IntegrationClient.class)))
                .thenAnswer(invocation -> {
                    IntegrationClient client = invocation.getArgument(0);
                    client.setId(1L);
                    return client;
                });

        IntegrationClientCreateResponse response = service.create(new IntegrationClientCreateRequest(
                "Test Lab System", IntegrationAuthType.BASIC_AUTH, "dmed", organizationUuid,
                List.of(IntegrationScope.FORM058_SUBMIT), null));

        assertThat(response.basicAuthSecret()).isNotBlank();
        assertThat(response.clientSecret()).isNull();
        assertThat(response.apiKey()).isNull();

        ArgumentCaptor<IntegrationClient> savedClient = ArgumentCaptor.forClass(IntegrationClient.class);
        verify(integrationClientRepository).save(savedClient.capture());

        assertThat(passwordEncoder.matches(response.basicAuthSecret(), savedClient.getValue().getBasicAuthSecretHash()))
                .isTrue();
    }

    @Test
    void createAnIpAllowlistClientRequiresAllowedIpsAndIssuesNoSecret() {
        UUID organizationUuid = UUID.randomUUID();
        when(organizationLookup.resolveActiveId(organizationUuid)).thenReturn(42L);

        assertThatThrownBy(() -> service.create(new IntegrationClientCreateRequest(
                "Test Lab System", IntegrationAuthType.IP_ALLOWLIST, "dmed", organizationUuid,
                List.of(IntegrationScope.FORM058_SUBMIT), null)))
                .isInstanceOf(AllowedIpsRequiredException.class);

        when(integrationClientRepository.save(any(IntegrationClient.class)))
                .thenAnswer(invocation -> {
                    IntegrationClient client = invocation.getArgument(0);
                    client.setId(1L);
                    return client;
                });

        IntegrationClientCreateResponse response = service.create(new IntegrationClientCreateRequest(
                "Test Lab System", IntegrationAuthType.IP_ALLOWLIST, "dmed", organizationUuid,
                List.of(IntegrationScope.FORM058_SUBMIT), List.of("10.0.5.0/24")));

        assertThat(response.clientSecret()).isNull();
        assertThat(response.apiKey()).isNull();
        assertThat(response.basicAuthSecret()).isNull();
        assertThat(response.allowedIps()).containsExactly("10.0.5.0/24");
    }

    @Test
    void updateAllowedIpsReplacesTheStoredListAndCanClearIt() {
        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test")
                .clientSecretHash("hash")
                .organizationId(42L)
                .sourceKey("dmed")
                .name("Test Client")
                .scopes("form058:submit")
                .allowedIps("10.0.0.0/24")
                .active(true)
                .build();

        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(integrationClientRepository.save(any(IntegrationClient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.updateAllowedIps(1L, new IntegrationClientAllowedIpsUpdateRequest(List.of("192.168.1.1")));
        assertThat(client.getAllowedIps()).isEqualTo("192.168.1.1");

        service.updateAllowedIps(1L, new IntegrationClientAllowedIpsUpdateRequest(null));
        assertThat(client.getAllowedIps()).isNull();
    }

    @Test
    void updateAllowedIpsRejectsClearingItForAnIpAllowlistClient() {
        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test")
                .authType(IntegrationAuthType.IP_ALLOWLIST)
                .organizationId(42L)
                .sourceKey("dmed")
                .name("Test Client")
                .scopes("form058:submit")
                .allowedIps("10.0.0.0/24")
                .active(true)
                .build();

        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.updateAllowedIps(1L, new IntegrationClientAllowedIpsUpdateRequest(null)))
                .isInstanceOf(AllowedIpsRequiredException.class);
        assertThat(client.getAllowedIps()).isEqualTo("10.0.0.0/24");
    }

    @Test
    void updateReplacesNameScopesAndActiveButLeavesIdentityAndCredentialsAlone() {
        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test")
                .authType(IntegrationAuthType.API_KEY)
                .apiKeyHash("hash")
                .organizationId(42L)
                .sourceKey("dmed")
                .name("Old Name")
                .scopes("form058:submit")
                .active(false)
                .build();

        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(integrationClientRepository.save(any(IntegrationClient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.update(1L, new IntegrationClientUpdateRequest(
                "New Name", List.of(IntegrationScope.FORM058_SUBMIT, IntegrationScope.FORM0581_SUBMIT), true));

        assertThat(client.getName()).isEqualTo("New Name");
        assertThat(client.getScopes()).isEqualTo("form058:submit,form0581:submit");
        assertThat(client.isActive()).isTrue();
        assertThat(client.getClientId()).isEqualTo("ic_test");
        assertThat(client.getSourceKey()).isEqualTo("dmed");
        assertThat(client.getAuthType()).isEqualTo(IntegrationAuthType.API_KEY);
        assertThat(client.getApiKeyHash()).isEqualTo("hash");
    }

    @Test
    void updateCanDeactivateAndLaterReactivateTheSameClient() {
        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test")
                .clientSecretHash("hash")
                .organizationId(42L)
                .sourceKey("dmed")
                .name("Test Client")
                .scopes("form058:submit")
                .active(true)
                .build();

        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(integrationClientRepository.save(any(IntegrationClient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.update(1L, new IntegrationClientUpdateRequest(
                "Test Client", List.of(IntegrationScope.FORM058_SUBMIT), false));
        assertThat(client.isActive()).isFalse();

        service.update(1L, new IntegrationClientUpdateRequest(
                "Test Client", List.of(IntegrationScope.FORM058_SUBMIT), true));
        assertThat(client.isActive()).isTrue();
    }

    @Test
    void revokeDeactivatesTheClientWithoutTouchingItsCredentials() {
        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test")
                .clientSecretHash("hash")
                .organizationId(42L)
                .name("Test Client")
                .scopes("form058:submit")
                .active(true)
                .build();

        when(integrationClientRepository.findById(1L)).thenReturn(java.util.Optional.of(client));
        when(integrationClientRepository.save(any(IntegrationClient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.revoke(1L);

        assertThat(client.isActive()).isFalse();
        assertThat(client.getClientSecretHash()).isEqualTo("hash");
    }

    @Test
    void updateWebhookRejectsANonHttpsCallbackUrlWhenActive() {
        IntegrationClient client = webhookTestClient();
        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.updateWebhook(1L, new IntegrationClientWebhookUpdateRequest(
                "http://partner.example.com/callback", OutboundHttpMethod.POST, OutboundWebhookAuthType.NONE,
                null, null, null, true)))
                .isInstanceOf(InvalidWebhookConfigException.class);
    }

    @Test
    void updateWebhookRejectsAMissingCallbackUrlWhenActive() {
        IntegrationClient client = webhookTestClient();
        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.updateWebhook(1L, new IntegrationClientWebhookUpdateRequest(
                null, OutboundHttpMethod.POST, OutboundWebhookAuthType.NONE, null, null, null, true)))
                .isInstanceOf(InvalidWebhookConfigException.class);
    }

    @Test
    void updateWebhookRejectsAMissingHttpMethodWhenActive() {
        IntegrationClient client = webhookTestClient();
        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.updateWebhook(1L, new IntegrationClientWebhookUpdateRequest(
                "https://partner.example.com/callback", null, OutboundWebhookAuthType.NONE, null, null, null, true)))
                .isInstanceOf(InvalidWebhookConfigException.class);
    }

    @Test
    void updateWebhookRejectsBasicAuthWithoutAUsername() {
        IntegrationClient client = webhookTestClient();
        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.updateWebhook(1L, new IntegrationClientWebhookUpdateRequest(
                "https://partner.example.com/callback", OutboundHttpMethod.POST, OutboundWebhookAuthType.BASIC_AUTH,
                null, null, "secret", true)))
                .isInstanceOf(InvalidWebhookConfigException.class);
    }

    @Test
    void updateWebhookRejectsBasicAuthWithoutASecretWhenNoneIsStoredYet() {
        IntegrationClient client = webhookTestClient();
        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.updateWebhook(1L, new IntegrationClientWebhookUpdateRequest(
                "https://partner.example.com/callback", OutboundHttpMethod.POST, OutboundWebhookAuthType.BASIC_AUTH,
                "svc-user", null, null, true)))
                .isInstanceOf(InvalidWebhookConfigException.class);
    }

    @Test
    void updateWebhookRejectsBearerTokenWithoutASecret() {
        IntegrationClient client = webhookTestClient();
        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.updateWebhook(1L, new IntegrationClientWebhookUpdateRequest(
                "https://partner.example.com/callback", OutboundHttpMethod.POST, OutboundWebhookAuthType.BEARER_TOKEN,
                null, null, null, true)))
                .isInstanceOf(InvalidWebhookConfigException.class);
    }

    @Test
    void updateWebhookRejectsApiKeyHeaderWithoutAHeaderName() {
        IntegrationClient client = webhookTestClient();
        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.updateWebhook(1L, new IntegrationClientWebhookUpdateRequest(
                "https://partner.example.com/callback", OutboundHttpMethod.POST, OutboundWebhookAuthType.API_KEY_HEADER,
                null, null, "secret", true)))
                .isInstanceOf(InvalidWebhookConfigException.class);
    }

    @Test
    void updateWebhookAcceptsAndEncryptsAValidBasicAuthConfig() {
        IntegrationClient client = webhookTestClient();
        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(integrationClientRepository.save(any(IntegrationClient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(webhookSecretCipher.encrypt("s3cr3t")).thenReturn("ENCRYPTED(s3cr3t)");

        service.updateWebhook(1L, new IntegrationClientWebhookUpdateRequest(
                "https://partner.example.com/callback", OutboundHttpMethod.POST, OutboundWebhookAuthType.BASIC_AUTH,
                "svc-user", null, "s3cr3t", true));

        assertThat(client.getWebhookCallbackUrl()).isEqualTo("https://partner.example.com/callback");
        assertThat(client.getWebhookHttpMethod()).isEqualTo(OutboundHttpMethod.POST);
        assertThat(client.getWebhookAuthType()).isEqualTo(OutboundWebhookAuthType.BASIC_AUTH);
        assertThat(client.getWebhookAuthUsername()).isEqualTo("svc-user");
        assertThat(client.getWebhookAuthSecretEncrypted()).isEqualTo("ENCRYPTED(s3cr3t)");
        assertThat(client.isWebhookActive()).isTrue();
    }

    @Test
    void updateWebhookKeepsThePreviouslyStoredSecretWhenNoneIsResubmitted() {
        IntegrationClient client = webhookTestClient();
        client.setWebhookAuthType(OutboundWebhookAuthType.BEARER_TOKEN);
        client.setWebhookAuthSecretEncrypted("ALREADY-ENCRYPTED");
        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(integrationClientRepository.save(any(IntegrationClient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.updateWebhook(1L, new IntegrationClientWebhookUpdateRequest(
                "https://partner.example.com/callback", OutboundHttpMethod.PATCH, OutboundWebhookAuthType.BEARER_TOKEN,
                null, null, null, true));

        assertThat(client.getWebhookAuthSecretEncrypted()).isEqualTo("ALREADY-ENCRYPTED");
        assertThat(client.getWebhookHttpMethod()).isEqualTo(OutboundHttpMethod.PATCH);
    }

    @Test
    void updateWebhookSkipsValidationWhenDeactivating() {
        IntegrationClient client = webhookTestClient();
        when(integrationClientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(integrationClientRepository.save(any(IntegrationClient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.updateWebhook(1L, new IntegrationClientWebhookUpdateRequest(
                null, null, OutboundWebhookAuthType.NONE, null, null, null, false));

        assertThat(client.isWebhookActive()).isFalse();
    }

    private IntegrationClient webhookTestClient() {
        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test")
                .clientSecretHash("hash")
                .organizationId(42L)
                .sourceKey("dmed")
                .name("Test Client")
                .scopes("form058:submit")
                .active(true)
                .build();
        client.setId(1L);
        return client;
    }
}
