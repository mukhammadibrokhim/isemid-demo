package uz.uzinfocom.app.platform.security.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.auth.IntegrationClientAuthentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IntegrationIpAllowlistAuthenticationFilterTest {

    private final IntegrationClientRepository integrationClientRepository = mock(IntegrationClientRepository.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final IntegrationCredentialAuthenticator authenticator =
            new IntegrationCredentialAuthenticator(organizationRepository, integrationClientRepository);
    private final IntegrationIpAllowlistAuthenticationFilter filter =
            new IntegrationIpAllowlistAuthenticationFilter(integrationClientRepository, authenticator);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesTheSingleClientWhoseAllowlistMatchesTheCallerIp() throws Exception {
        UUID organizationUuid = UUID.randomUUID();
        IntegrationClient client = client("10.0.5.0/24");

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/integration/v1/dmed/form-058");
        request.setRemoteAddr("10.0.5.42");

        when(integrationClientRepository.findAllByAuthTypeAndActiveTrue(IntegrationAuthType.IP_ALLOWLIST))
                .thenReturn(List.of(client));
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
    void leavesTheRequestUnauthenticatedWhenNoClientMatches() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/integration/v1/dmed/form-058");
        request.setRemoteAddr("203.0.113.5");

        when(integrationClientRepository.findAllByAuthTypeAndActiveTrue(IntegrationAuthType.IP_ALLOWLIST))
                .thenReturn(List.of(client("10.0.5.0/24")));

        FilterChain chain = (servletRequest, servletResponse) ->
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        filter.doFilter(request, new MockHttpServletResponse(), chain);
    }

    @Test
    void leavesTheRequestUnauthenticatedWhenTheIpMatchesMoreThanOneClient() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/integration/v1/dmed/form-058");
        request.setRemoteAddr("10.0.5.42");

        when(integrationClientRepository.findAllByAuthTypeAndActiveTrue(IntegrationAuthType.IP_ALLOWLIST))
                .thenReturn(List.of(client("10.0.5.0/24"), client("10.0.5.0/24")));

        FilterChain chain = (servletRequest, servletResponse) ->
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        filter.doFilter(request, new MockHttpServletResponse(), chain);
    }

    @Test
    void doesNotRunWhenAnApiKeyHeaderIsPresent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/integration/v1/dmed/form-058");
        request.setRemoteAddr("10.0.5.42");
        request.addHeader("X-Api-Key", "ic_test.token");

        FilterChain chain = (servletRequest, servletResponse) ->
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        filter.doFilter(request, new MockHttpServletResponse(), chain);
    }

    private IntegrationClient client(String allowedIps) {
        return IntegrationClient.builder()
                .clientId("ic_test")
                .authType(IntegrationAuthType.IP_ALLOWLIST)
                .organizationId(42L)
                .sourceKey("dmed")
                .name("Test Client")
                .scopes("form058:submit")
                .allowedIps(allowedIps)
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
