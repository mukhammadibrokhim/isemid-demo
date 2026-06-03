package uz.uzinfocom.app.platform.security.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.auth.FederatedAuthenticationToken;
import uz.uzinfocom.app.platform.security.auth.SecurityAuthorityService;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.SecurityHeaders;
import uz.uzinfocom.app.platform.security.principal.PrincipalOrganization;
import uz.uzinfocom.app.platform.security.principal.PrincipalUser;
import uz.uzinfocom.app.platform.security.route.RequestPolicy;
import uz.uzinfocom.app.platform.security.route.RequestPolicyResolver;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizationContextFilterTest {

    private final RequestPolicyResolver requestPolicyResolver = mock(RequestPolicyResolver.class);
    private final SecurityAuthorityService securityAuthorityService = mock(SecurityAuthorityService.class);
    private final OrganizationContextFilter filter = new OrganizationContextFilter(
            requestPolicyResolver,
            securityAuthorityService
    );

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        CurrentOrganizationContext.clear();
    }

    @Test
    void validatesMembershipAndKeepsOriginalAuthorities() throws Exception {
        UUID organizationUuid = UUID.randomUUID();
        Organization organization = organization(20L, organizationUuid);
        FederatedAuthenticationToken authentication = authentication();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/patients");
        request.addHeader(SecurityHeaders.ORGANIZATION_ID, organizationUuid.toString());

        when(requestPolicyResolver.resolve(request)).thenReturn(new RequestPolicy(false, true, true, "test"));
        when(securityAuthorityService.findOrganizationByUuid(organizationUuid)).thenReturn(Optional.of(organization));
        when(securityAuthorityService.userBelongsToOrganization(1L, 20L)).thenReturn(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        FilterChain chain = (servletRequest, servletResponse) -> {
            Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();

            assertThat(CurrentOrganizationContext.require().getId()).isEqualTo(20L);
            assertThat(currentAuthentication).isSameAs(authentication);
            assert currentAuthentication != null;
            assertThat(currentAuthentication.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_GLOBAL_ADMIN");
        };

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(CurrentOrganizationContext.getOptional()).isEmpty();
    }

    @Test
    void rejectsSelectedOrganizationWhenUserIsNotAMember() {
        UUID organizationUuid = UUID.randomUUID();
        Organization organization = organization(20L, organizationUuid);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/patients");
        request.addHeader(SecurityHeaders.ORGANIZATION_ID, organizationUuid.toString());

        when(requestPolicyResolver.resolve(request)).thenReturn(new RequestPolicy(false, true, true, "test"));
        when(securityAuthorityService.findOrganizationByUuid(organizationUuid)).thenReturn(Optional.of(organization));
        when(securityAuthorityService.userBelongsToOrganization(1L, 20L)).thenReturn(false);

        SecurityContextHolder.getContext().setAuthentication(authentication());

        assertThatThrownBy(() -> filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("organization.not_allowed");

        verify(securityAuthorityService).userBelongsToOrganization(1L, 20L);
    }

    private Organization organization(Long id, UUID uuid) {
        Organization organization = new Organization();
        organization.setId(id);
        organization.setUuid(uuid);
        organization.setName("Central Clinic");
        return organization;
    }

    private FederatedAuthenticationToken authentication() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "subject")
                .build();
        PrincipalUser principal = new PrincipalUser(
                1L,
                UUID.randomUUID(),
                "doctor",
                "123456789",
                true,
                null
        );

        return new FederatedAuthenticationToken(
                jwt,
                principal,
                List.of(new SimpleGrantedAuthority("ROLE_GLOBAL_ADMIN")),
                List.<PrincipalOrganization>of()
        );
    }
}
