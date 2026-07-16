package uz.uzinfocom.app.platform.security.authorization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import uz.uzinfocom.app.platform.security.auth.FederatedAuthenticationToken;
import uz.uzinfocom.app.platform.security.principal.PrincipalOrganization;
import uz.uzinfocom.app.platform.security.principal.PrincipalUser;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminAccessGuardTest {

    private final AdminAccessGuard guard = new AdminAccessGuard();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isSuperAdminTrueForSuperAdminAuthority() {
        authenticateAs(1L, "isemid_super_admin");

        assertThat(guard.isSuperAdmin()).isTrue();
        assertThat(guard.isAdmin()).isTrue();
    }

    @Test
    void isSuperAdminTrueForRolePrefixedAuthority() {
        authenticateAs(1L, "ROLE_isemid_super_admin");

        assertThat(guard.isSuperAdmin()).isTrue();
    }

    @Test
    void isSuperAdminFalseWithoutTheAuthority() {
        authenticateAs(1L, "isemid_user");

        assertThat(guard.isSuperAdmin()).isFalse();
    }

    @Test
    void isSuperAdminFalseWhenUnauthenticated() {
        assertThat(guard.isSuperAdmin()).isFalse();
    }

    @Test
    void isPanelAdminTrueForAdminAuthority() {
        authenticateAs(42L, "isemid_admin");

        assertThat(guard.isPanelAdmin()).isTrue();
        assertThat(guard.isAdmin()).isTrue();
    }

    @Test
    void isPanelAdminTrueForRolePrefixedAdminAuthority() {
        authenticateAs(42L, "ROLE_isemid_admin");

        assertThat(guard.isPanelAdmin()).isTrue();
    }

    @Test
    void isAdminFalseWhenNeitherSuperAdminNorAdminAuthority() {
        authenticateAs(42L, "isemid_user");

        assertThat(guard.isAdmin()).isFalse();
    }

    @Test
    void isPanelAdminFalseWhenUnauthenticated() {
        assertThat(guard.isPanelAdmin()).isFalse();
    }

    @Test
    void requireSuperAdminThrowsForNonSuperAdmin() {
        authenticateAs(1L, "isemid_user");

        assertThatThrownBy(guard::requireSuperAdmin)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void requireAdminThrowsWhenNeitherSuperAdminNorAdminAuthority() {
        authenticateAs(1L, "isemid_user");

        assertThatThrownBy(guard::requireAdmin)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void requireAdminSucceedsForAdminAuthority() {
        authenticateAs(1L, "isemid_admin");

        guard.requireAdmin();
    }

    private void authenticateAs(Long userId, String... authorityNames) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "subject")
                .build();
        PrincipalUser principal = new PrincipalUser(userId, UUID.randomUUID(), "user", "123456789", true, null);
        List<SimpleGrantedAuthority> authorities = Arrays.stream(authorityNames)
                .map(SimpleGrantedAuthority::new)
                .toList();

        FederatedAuthenticationToken token = new FederatedAuthenticationToken(
                jwt, principal, authorities, List.<PrincipalOrganization>of()
        );

        SecurityContextHolder.getContext().setAuthentication(token);
    }
}
