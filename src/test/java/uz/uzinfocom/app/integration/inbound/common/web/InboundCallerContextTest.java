package uz.uzinfocom.app.integration.inbound.common.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import uz.uzinfocom.app.modules.iam.domain.Organization;
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
 * {@code OrganizationContextFilter} for either caller type). Scope gating
 * applies to both caller types too — an integration client via its
 * {@code SCOPE_*} authority, an SSO/DHP caller via the equivalent
 * {@code PERMISSION_*} authority (see {@code IntegrationScope#permissionAuthority()}).
 * Source-key matching stays integration-client-only.
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

        assertThatCode(() -> InboundCallerContext.requireScope(IntegrationScope.FORM058_SUBMIT))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsAnIntegrationClientMissingTheRequiredScope() {
        IntegrationClientPrincipal principal = new IntegrationClientPrincipal("ic_test", "dmed", 42L, UUID.randomUUID());
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "ic_test").build();
        SecurityContextHolder.getContext().setAuthentication(new IntegrationClientAuthenticationToken(
                jwt, principal, Set.of(new SimpleGrantedAuthority("SCOPE_form0581:submit"))));

        assertThatThrownBy(() -> InboundCallerContext.requireScope(IntegrationScope.FORM058_SUBMIT))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("integration.scope.missing");
    }

    @Test
    void allowsAnSsoOrDhpCallerWithTheEquivalentPermission() {
        // No SCOPE_* authority (only IntegrationJwtAuthenticationConverter grants those) - instead
        // the RBAC-equivalent PERMISSION_* authority, granted through the ordinary Role/Permission/
        // Action admin CRUD (see IntegrationScope#permissionAuthority()).
        SecurityContextHolder.getContext().setAuthentication(
                federatedAuthentication(new SimpleGrantedAuthority("PERMISSION_FORM058_SUBMIT")));

        assertThatCode(() -> InboundCallerContext.requireScope(IntegrationScope.FORM058_SUBMIT))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsAnSsoOrDhpCallerMissingTheEquivalentPermission() {
        SecurityContextHolder.getContext().setAuthentication(federatedAuthentication());

        assertThatThrownBy(() -> InboundCallerContext.requireScope(IntegrationScope.FORM058_SUBMIT))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("integration.scope.missing");
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

    private FederatedAuthenticationToken federatedAuthentication(SimpleGrantedAuthority... authorities) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "subject").build();
        PrincipalUser principal = new PrincipalUser(1L, UUID.randomUUID(), "doctor", "123456789", true, null);
        return new FederatedAuthenticationToken(jwt, principal, List.of(authorities), List.of());
    }
}
