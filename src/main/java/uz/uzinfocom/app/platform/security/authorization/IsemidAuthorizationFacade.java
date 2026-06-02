package uz.uzinfocom.app.platform.security.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.security.properties.AuthorizationProperties;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

@Component("isemidAuth")
@RequiredArgsConstructor
public class IsemidAuthorizationFacade {

    private final AuthorizationProperties properties;

    public boolean hasRole(String roleName) {
        return hasAuthority(AuthorityNames.role(roleName));
    }

    public boolean hasAnyRole(String... roleNames) {
        return roleNames != null && Arrays.stream(roleNames)
                .filter(Objects::nonNull)
                .anyMatch(this::hasRole);
    }

    public boolean hasPermission(String subject, String action) {
        if (hasAnyRole(properties.getPermissionBypassRoles().toArray(String[]::new))) {
            return true;
        }

        return hasAuthority(AuthorityNames.permission(subject, action))
                || hasAuthority(AuthorityNames.permission(subject, "*"))
                || hasAuthority(AuthorityNames.permission("*", action))
                || hasAuthority(AuthorityNames.permission("*", "*"));
    }

    private boolean hasAuthority(String expectedAuthority) {
        String normalizedExpected = expectedAuthority.toUpperCase(Locale.ROOT);
        return authorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .map(value -> value.toUpperCase(Locale.ROOT))
                .anyMatch(normalizedExpected::equals);
    }

    private Collection<? extends GrantedAuthority> authorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return java.util.List.of();
        }
        return authentication.getAuthorities();
    }
}
