package uz.uzinfocom.app.platform.integrationclient.application.command;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationIdResolver;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientCreateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientCreateResponse;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IntegrationClientCommandServiceTest {

    private final IntegrationClientRepository integrationClientRepository = mock(IntegrationClientRepository.class);
    private final OrganizationIdResolver organizationIdResolver = mock(OrganizationIdResolver.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final IntegrationClientCommandService service = new IntegrationClientCommandService(
            integrationClientRepository, organizationIdResolver, passwordEncoder);

    @Test
    void createReturnsThePlaintextSecretOnlyOnceAndNeverPersistsIt() {
        UUID organizationUuid = UUID.randomUUID();
        when(organizationIdResolver.resolveActiveId(organizationUuid)).thenReturn(42L);
        when(integrationClientRepository.save(any(IntegrationClient.class)))
                .thenAnswer(invocation -> {
                    IntegrationClient client = invocation.getArgument(0);
                    client.setId(1L);
                    return client;
                });

        IntegrationClientCreateResponse response = service.create(new IntegrationClientCreateRequest(
                "Test Lab System", "dmed", organizationUuid, List.of(IntegrationScope.FORM058_SUBMIT)));

        assertThat(response.clientSecret()).isNotBlank();
        assertThat(response.organizationId()).isEqualTo(42L);
        assertThat(response.scopes()).containsExactly("form058:submit");

        ArgumentCaptor<IntegrationClient> savedClient = ArgumentCaptor.forClass(IntegrationClient.class);
        verify(integrationClientRepository).save(savedClient.capture());

        // The persisted entity carries only the BCrypt hash - never the plaintext secret
        // returned in the response above.
        assertThat(savedClient.getValue().getClientSecretHash()).isNotEqualTo(response.clientSecret());
        assertThat(passwordEncoder.matches(response.clientSecret(), savedClient.getValue().getClientSecretHash()))
                .isTrue();
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
}
