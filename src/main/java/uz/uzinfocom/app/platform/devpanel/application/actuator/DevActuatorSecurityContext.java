package uz.uzinfocom.app.platform.devpanel.application.actuator;

import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The actuator's view of the dev-panel caller. Actuator decides things like
 * "show health details / property values?" by asking {@link #isUserInRole} with the
 * role names configured in {@code management.endpoint.<id>.roles}, so this maps
 * those names onto the caller's granted authorities ({@code DEV_USER} &rarr;
 * {@code ROLE_DEV_USER}).
 *
 * <p>Immutable snapshot: live streams keep sampling on a scheduler thread long after
 * the request's {@code SecurityContextHolder} is gone.
 */
public final class DevActuatorSecurityContext implements SecurityContext {

    /** Principal of the panel's own background publishers (no human caller). */
    private static final String SYSTEM_PRINCIPAL = "dev-panel-system";

    private final Principal principal;
    private final Set<String> authorities;

    private DevActuatorSecurityContext(Principal principal, Set<String> authorities) {
        this.principal = principal;
        this.authorities = authorities;
    }

    public static DevActuatorSecurityContext of(Authentication authentication) {
        Set<String> granted = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());
        return new DevActuatorSecurityContext(authentication, granted);
    }

    /**
     * For the panel's own scheduled publishers: the lowest tier only ({@code DEV_USER} /
     * {@code DEV_MONITORING}), enough for health details but never for property values.
     */
    public static DevActuatorSecurityContext system() {
        return new DevActuatorSecurityContext(() -> SYSTEM_PRINCIPAL, Set.of("ROLE_DEV_USER", "ROLE_DEV_MONITORING"));
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return authorities.contains("ROLE_" + role) || authorities.contains(role);
    }
}
