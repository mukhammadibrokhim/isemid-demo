package uz.uzinfocom.app.platform.security.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.auth.IntegrationClientAuthentication;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IntegrationBasicAuthenticationFilterTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final IntegrationClientRepository integrationClientRepository = mock(IntegrationClientRepository.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final IntegrationCredentialAuthenticator authenticator =
            new IntegrationCredentialAuthenticator(organizationRepository, integrationClientRepository);
    private final IntegrationBasicAuthenticationFilter filter = new IntegrationBasicAuthenticationFilter(
            integrationClientRepository, passwordEncoder, authenticator);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesValidBasicCredentials() throws Exception {
        UUID organizationUuid = UUID.randomUUID();
        IntegrationClient client = client(passwordEncoder.encode("secret"));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/integration/v1/dmed/form-058");
        request.addHeader(HttpHeaders.AUTHORIZATION, basic("ic_test", "secret"));

        when(integrationClientRepository.findByClientId("ic_test")).thenReturn(Optional.of(client));
        when(organizationRepository.findById(42L)).thenReturn(Optional.of(organization(organizationUuid)));
        when(integrationClientRepository.save(client)).thenReturn(client);

        FilterChain chain = (servletRequest, servletResponse) -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isInstanceOf(IntegrationClientAuthentication.class);
            assertThat(((IntegrationClientAuthentication) authentication).getPrincipal().clientId())
                    .isEqualTo("ic_test");
        };

        filter.doFilter(request, new MockHttpServletResponse(), chain);
    }

    @Test
    void leavesTheRequestUnauthenticatedForAWrongPassword() throws Exception {
        IntegrationClient client = client(passwordEncoder.encode("secret"));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/integration/v1/dmed/form-058");
        request.addHeader(HttpHeaders.AUTHORIZATION, basic("ic_test", "wrong"));

        when(integrationClientRepository.findByClientId("ic_test")).thenReturn(Optional.of(client));

        FilterChain chain = (servletRequest, servletResponse) ->
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        filter.doFilter(request, new MockHttpServletResponse(), chain);
    }

    @Test
    void ignoresABearerAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/integration/v1/dmed/form-058");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer some-jwt");

        FilterChain chain = (servletRequest, servletResponse) ->
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        filter.doFilter(request, new MockHttpServletResponse(), chain);
    }

    private String basic(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private IntegrationClient client(String basicAuthSecretHash) {
        return IntegrationClient.builder()
                .clientId("ic_test")
                .authType(IntegrationAuthType.BASIC_AUTH)
                .basicAuthSecretHash(basicAuthSecretHash)
                .organizationId(42L)
                .sourceKey("dmed")
                .name("Test Client")
                .scopes("form058:submit")
                .active(true)
                .build();
    }

    private Organization organization(UUID uuid) {
        Organization organization = new Organization();
        organization.setId(42L);
        organization.setUuid(uuid);
        return organization;
    }
}
