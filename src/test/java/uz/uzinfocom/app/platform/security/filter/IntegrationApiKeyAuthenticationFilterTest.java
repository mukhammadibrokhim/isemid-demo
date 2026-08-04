package uz.uzinfocom.app.platform.security.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.auth.IntegrationClientAuthentication;
import uz.uzinfocom.app.platform.security.context.SecurityHeaders;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IntegrationApiKeyAuthenticationFilterTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final IntegrationClientRepository integrationClientRepository = mock(IntegrationClientRepository.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final IntegrationCredentialAuthenticator authenticator =
            new IntegrationCredentialAuthenticator(organizationRepository, integrationClientRepository);
    private final IntegrationApiKeyAuthenticationFilter filter = new IntegrationApiKeyAuthenticationFilter(
            integrationClientRepository, passwordEncoder, authenticator);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesAValidApiKey() throws Exception {
        UUID organizationUuid = UUID.randomUUID();
        String token = "raw-token";
        IntegrationClient client = client(IntegrationAuthType.API_KEY, passwordEncoder.encode(token));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/integration/v1/dmed/form-058");
        request.addHeader(SecurityHeaders.INTEGRATION_API_KEY, "ic_test." + token);

        when(integrationClientRepository.findByClientId("ic_test")).thenReturn(Optional.of(client));
        when(organizationRepository.findById(42L)).thenReturn(Optional.of(organization(organizationUuid)));
        when(integrationClientRepository.save(client)).thenReturn(client);

        FilterChain chain = (servletRequest, servletResponse) -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isInstanceOf(IntegrationClientAuthentication.class);
            assertThat(((IntegrationClientAuthentication) authentication).getPrincipal().clientId())
                    .isEqualTo("ic_test");
            assertThat(authentication.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("SCOPE_form058:submit");
        };

        filter.doFilter(request, new MockHttpServletResponse(), chain);
    }

    @Test
    void leavesTheRequestUnauthenticatedForAWrongToken() throws Exception {
        IntegrationClient client = client(IntegrationAuthType.API_KEY, passwordEncoder.encode("raw-token"));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/integration/v1/dmed/form-058");
        request.addHeader(SecurityHeaders.INTEGRATION_API_KEY, "ic_test.wrong-token");

        when(integrationClientRepository.findByClientId("ic_test")).thenReturn(Optional.of(client));

        FilterChain chain = (servletRequest, servletResponse) ->
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        filter.doFilter(request, new MockHttpServletResponse(), chain);
    }

    @Test
    void leavesTheRequestUnauthenticatedForAClientRegisteredWithADifferentAuthType() throws Exception {
        String token = "raw-token";
        IntegrationClient client = client(IntegrationAuthType.CLIENT_CREDENTIALS, passwordEncoder.encode(token));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/integration/v1/dmed/form-058");
        request.addHeader(SecurityHeaders.INTEGRATION_API_KEY, "ic_test." + token);

        when(integrationClientRepository.findByClientId("ic_test")).thenReturn(Optional.of(client));

        FilterChain chain = (servletRequest, servletResponse) ->
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        filter.doFilter(request, new MockHttpServletResponse(), chain);
    }

    @Test
    void doesNotRunOutsideTheIntegrationApiSurface() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/patients");
        request.addHeader(SecurityHeaders.INTEGRATION_API_KEY, "ic_test.raw-token");

        FilterChain chain = (servletRequest, servletResponse) ->
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        filter.doFilter(request, new MockHttpServletResponse(), chain);
    }

    private IntegrationClient client(IntegrationAuthType authType, String apiKeyHash) {
        return IntegrationClient.builder()
                .clientId("ic_test")
                .authType(authType)
                .apiKeyHash(apiKeyHash)
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
