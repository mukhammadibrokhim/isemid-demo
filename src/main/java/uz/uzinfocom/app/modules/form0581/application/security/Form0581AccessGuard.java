package uz.uzinfocom.app.modules.form0581.application.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class Form0581AccessGuard {

    private static final String SUPER_ADMIN_ROLE = "isemid_super_admin";
    private static final String SPRING_SUPER_ADMIN_ROLE =
            "ROLE_" + SUPER_ADMIN_ROLE;

    public void requireSuperAdmin() {
        if (!isSuperAdmin()) {
            throw new AccessDeniedException(
                    "ALL direction is available only for isemid_super_admin"
            );
        }
    }

    public boolean isSuperAdmin() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(this::isSuperAdminAuthority);
    }

    private boolean isSuperAdminAuthority(String authority) {
        return SUPER_ADMIN_ROLE.equalsIgnoreCase(authority)
                || SPRING_SUPER_ADMIN_ROLE.equalsIgnoreCase(authority);
    }
}
