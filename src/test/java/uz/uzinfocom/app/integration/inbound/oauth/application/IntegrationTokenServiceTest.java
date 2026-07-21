package uz.uzinfocom.app.integration.inbound.oauth.application;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.uzinfocom.app.integration.inbound.oauth.application.exception.InvalidIntegrationCredentialsException;
import uz.uzinfocom.app.integration.inbound.oauth.web.dto.IntegrationTokenRequest;
import uz.uzinfocom.app.integration.inbound.oauth.web.dto.IntegrationTokenResponse;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.jwt.integration.IntegrationTokenIssuer;
import uz.uzinfocom.app.platform.security.jwt.integration.IntegrationTokenSigningKey;
import uz.uzinfocom.app.platform.security.jwt.properties.IntegrationTokenProperties;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IntegrationTokenServiceTest {

    private final IntegrationClientRepository integrationClientRepository = mock(IntegrationClientRepository.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final IntegrationTokenIssuer tokenIssuer = new IntegrationTokenIssuer(
            new IntegrationTokenSigningKey(), new IntegrationTokenProperties());

    private final IntegrationTokenService service = new IntegrationTokenService(
            integrationClientRepository, organizationRepository, passwordEncoder, tokenIssuer);

    @Test
    void issuesATokenForACorrectlyAuthenticatedActiveClient() {
        String rawSecret = "correct-secret";
        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test")
                .clientSecretHash(passwordEncoder.encode(rawSecret))
                .organizationId(42L)
                .name("Test Client")
                .scopes("form058:submit")
                .active(true)
                .build();

        Organization organization = new Organization();
        organization.setId(42L);
        organization.setUuid(UUID.randomUUID());

        when(integrationClientRepository.findByClientId("ic_test")).thenReturn(Optional.of(client));
        when(organizationRepository.findById(42L)).thenReturn(Optional.of(organization));

        IntegrationTokenResponse response = service.issueToken(
                new IntegrationTokenRequest("ic_test", rawSecret, "client_credentials"));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.scope()).isEqualTo("form058:submit");
        verify(integrationClientRepository).save(any(IntegrationClient.class));
    }

    @Test
    void rejectsAWrongSecret() {
        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test")
                .clientSecretHash(passwordEncoder.encode("correct-secret"))
                .organizationId(42L)
                .name("Test Client")
                .scopes("form058:submit")
                .active(true)
                .build();

        when(integrationClientRepository.findByClientId("ic_test")).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.issueToken(
                new IntegrationTokenRequest("ic_test", "wrong-secret", "client_credentials")))
                .isInstanceOf(InvalidIntegrationCredentialsException.class);
    }

    @Test
    void rejectsARevokedClientEvenWithTheCorrectSecret() {
        IntegrationClient client = IntegrationClient.builder()
                .clientId("ic_test")
                .clientSecretHash(passwordEncoder.encode("correct-secret"))
                .organizationId(42L)
                .name("Test Client")
                .scopes("form058:submit")
                .active(false)
                .build();

        when(integrationClientRepository.findByClientId("ic_test")).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.issueToken(
                new IntegrationTokenRequest("ic_test", "correct-secret", "client_credentials")))
                .isInstanceOf(InvalidIntegrationCredentialsException.class);
    }

    @Test
    void rejectsAnUnknownClientId() {
        when(integrationClientRepository.findByClientId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.issueToken(
                new IntegrationTokenRequest("unknown", "any-secret", "client_credentials")))
                .isInstanceOf(InvalidIntegrationCredentialsException.class);
    }
}
