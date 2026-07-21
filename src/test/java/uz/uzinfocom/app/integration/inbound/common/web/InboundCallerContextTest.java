package uz.uzinfocom.app.integration.inbound.common.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.platform.security.auth.FederatedAuthenticationToken;
import uz.uzinfocom.app.platform.security.auth.IntegrationClientAuthenticationToken;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.principal.IntegrationClientPrincipal;
import uz.uzinfocom.app.platform.security.principal.PrincipalUser;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * This is a resource server, not a gate exclusive to the self-issued
 * integration token: both an {@link IntegrationClientAuthenticationToken}
 * (external system, provisioned via the admin API) and a
 * {@link FederatedAuthenticationToken} (SSO/DHP-authenticated internal
 * system or human) must be able to reach the inbound endpoints. Sender
 * organization resolution is identical for both (via
 * {@link CurrentOrganizationContext}, already populated by
 * {@code OrganizationContextFilter} for either caller type); scope gating
 * and source-key matching only apply to the integration-client caller.
 */
class InboundCallerContextTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        CurrentOrganizationContext.clear();
    }

    @Test
    void resolvesSenderOrganizationFromCurrentOrganizationContextForAnIntegrationClient() {
        IntegrationClientPrincipal principal = new IntegrationClientPrincipal("ic_test", "dmed", 42L, UUID.randomUUID());
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "ic_test").build();
        SecurityContextHolder.getContext().setAuthentication(
                new IntegrationClientAuthenticationToken(jwt, principal, Set.of()));

        Organization organization = new Organization();
        organization.setId(42L);
        CurrentOrganizationContext.set(organization);

        assertThat(InboundCallerContext.resolveSenderOrganizationId()).isEqualTo(42L);
    }

    @Test
    void resolvesSenderOrganizationFromCurrentOrganizationContextForAnSsoOrDhpCaller() {
        SecurityContextHolder.getContext().setAuthentication(federatedAuthentication());

        Organization organization = new Organization();
        organization.setId(99L);
        CurrentOrganizationContext.set(organization);

        assertThat(InboundCallerContext.resolveSenderOrganizationId()).isEqualTo(99L);
    }

    @Test
    void allowsAnIntegrationClientWithTheRequiredScope() {
        IntegrationClientPrincipal principal = new IntegrationClientPrincipal("ic_test", "dmed", 42L, UUID.randomUUID());
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "ic_test").build();
        SecurityContextHolder.getContext().setAuthentication(new IntegrationClientAuthenticationToken(
                jwt, principal, Set.of(new SimpleGrantedAuthority("SCOPE_form058:submit"))));

        assertThatCode(() -> InboundCallerContext.requireScopeIfIntegrationClient(IntegrationScope.FORM058_SUBMIT))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsAnIntegrationClientMissingTheRequiredScope() {
        IntegrationClientPrincipal principal = new IntegrationClientPrincipal("ic_test", "dmed", 42L, UUID.randomUUID());
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "ic_test").build();
        SecurityContextHolder.getContext().setAuthentication(new IntegrationClientAuthenticationToken(
                jwt, principal, Set.of(new SimpleGrantedAuthority("SCOPE_form0581:submit"))));

        assertThatThrownBy(() -> InboundCallerContext.requireScopeIfIntegrationClient(IntegrationScope.FORM058_SUBMIT))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("integration.scope.missing");
    }

    @Test
    void doesNotScopeGateAnSsoOrDhpCaller() {
        // A human/service caller authenticated via SSO or DHP never carries SCOPE_* authorities
        // (only IntegrationJwtAuthenticationConverter grants those) - the real access control for
        // this caller type is Form058CreateValidator's existing organization-scope check, not this
        // scope gate, so no exception should be thrown here regardless of granted authorities.
        SecurityContextHolder.getContext().setAuthentication(federatedAuthentication());

        assertThatCode(() -> InboundCallerContext.requireScopeIfIntegrationClient(IntegrationScope.FORM058_SUBMIT))
                .doesNotThrowAnyException();
    }

    @Test
    void allowsAnIntegrationClientWhoseSourceKeyMatchesThePathSegment() {
        IntegrationClientPrincipal principal = new IntegrationClientPrincipal("ic_test", "dmed", 42L, UUID.randomUUID());
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "ic_test").build();
        SecurityContextHolder.getContext().setAuthentication(
                new IntegrationClientAuthenticationToken(jwt, principal, Set.of()));

        assertThatCode(() -> InboundCallerContext.requireMatchingSourceKey("DMED"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsAnIntegrationClientWhoseSourceKeyDoesNotMatchThePathSegment() {
        IntegrationClientPrincipal principal = new IntegrationClientPrincipal("ic_test", "dmed", 42L, UUID.randomUUID());
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "ic_test").build();
        SecurityContextHolder.getContext().setAuthentication(
                new IntegrationClientAuthenticationToken(jwt, principal, Set.of()));

        assertThatThrownBy(() -> InboundCallerContext.requireMatchingSourceKey("lab-x"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("integration.source_key.mismatch");
    }

    @Test
    void doesNotSourceKeyGateAnSsoOrDhpCaller() {
        SecurityContextHolder.getContext().setAuthentication(federatedAuthentication());

        assertThatCode(() -> InboundCallerContext.requireMatchingSourceKey("anything"))
                .doesNotThrowAnyException();
    }

    private FederatedAuthenticationToken federatedAuthentication() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "subject").build();
        PrincipalUser principal = new PrincipalUser(1L, UUID.randomUUID(), "doctor", "123456789", true, null);
        return new FederatedAuthenticationToken(jwt, principal, List.of(), List.of());
    }
}
