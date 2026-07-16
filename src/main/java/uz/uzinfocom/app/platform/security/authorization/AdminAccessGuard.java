package uz.uzinfocom.app.platform.security.authorization;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * Single, canonical admin-authorization check for the whole app — replaces
 * the dead {@code SecurityUtils} and the near-duplicate
 * {@code Form058AccessGuard}/{@code Form0581AccessGuard} guards that each
 * independently re-implemented the same "isemid_super_admin" authority check.
 *
 * <p>Both tiers are pure external-authority checks against the token's
 * roles — {@code isemid_super_admin} for {@link #isSuperAdmin()} and
 * {@code isemid_admin} for {@link #isPanelAdmin()}. Neither is grantable
 * through this app's own API; both come exclusively from the external IAM
 * sync, so there is nothing here for that sync to ever silently overwrite.
 * Which specific admin-panel menus an {@code isemid_admin} may use is
 * governed separately by the existing Role/Permission system (see
 * {@code RoleController}/{@code PermissionController}), not by this guard.
 */
@Component
public class AdminAccessGuard {

    private static final String SUPER_ADMIN_ROLE = "isemid_super_admin";
    private static final String SPRING_SUPER_ADMIN_ROLE = "ROLE_" + SUPER_ADMIN_ROLE;

    private static final String ADMIN_ROLE = "isemid_admin";
    private static final String SPRING_ADMIN_ROLE = "ROLE_" + ADMIN_ROLE;

    public void requireSuperAdmin() {
        if (!isSuperAdmin()) {
            throw new AccessDeniedException("This action is available only for isemid_super_admin");
        }
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new AccessDeniedException("This action is available only for administrators");
        }
    }

    public boolean isAdmin() {
        return isSuperAdmin() || isPanelAdmin();
    }

    public boolean isSuperAdmin() {
        return hasAuthority(this::isSuperAdminAuthority);
    }

    public boolean isPanelAdmin() {
        return hasAuthority(this::isAdminAuthority);
    }

    private boolean hasAuthority(Predicate<String> authorityMatcher) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authorityMatcher);
    }

    private boolean isSuperAdminAuthority(String authority) {
        return SUPER_ADMIN_ROLE.equalsIgnoreCase(authority)
                || SPRING_SUPER_ADMIN_ROLE.equalsIgnoreCase(authority);
    }

    private boolean isAdminAuthority(String authority) {
        return ADMIN_ROLE.equalsIgnoreCase(authority)
                || SPRING_ADMIN_ROLE.equalsIgnoreCase(authority);
    }
}
