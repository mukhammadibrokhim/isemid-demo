package uz.uzinfocom.app.platform.devpanel.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import uz.uzinfocom.app.platform.devpanel.domain.DevUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Carries the {@link DevUser} id through Spring Security's {@code UserDetails}
 * so command services can attribute an action (e.g. resolving a {@code DevErrorLog})
 * to a specific dev-panel account without a second DB lookup.
 *
 * <p>Granted authorities are cumulative by {@link uz.uzinfocom.app.platform.devpanel.domain.DevUserRole}:
 * {@code ROLE_DEV_USER} (read, every account), {@code ROLE_DEV_ADMIN} (write,
 * ADMIN and SUPER_ADMIN) and {@code ROLE_DEV_SUPER_ADMIN} (delete + account
 * management, SUPER_ADMIN only) - so a single {@code hasRole(...)} check on a
 * controller method covers every tier allowed to call it.
 */
@Getter
public class DevUserPrincipal extends User {

    private final Long devUserId;

    /**
     * Snapshotted at authentication time, same as {@link #devUserId} - cheap
     * to check per-request in {@code DevPasswordChangeGuardFilter} without a
     * second DB lookup. Goes stale only within {@code DevAuthenticationCache}'s
     * TTL, and only for the now-superseded old-password cache entry, since a
     * password change necessarily changes the cache key (username+password).
     */
    private final boolean mustChangePassword;

    public DevUserPrincipal(DevUser devUser) {
        super(devUser.getUsername(), devUser.getPasswordHash(), devUser.isEnabled(),
                true, true, true, authorities(devUser));
        this.devUserId = devUser.getId();
        this.mustChangePassword = devUser.isMustChangePassword();
    }

    private static List<GrantedAuthority> authorities(DevUser devUser) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_DEV_MONITORING"));
        authorities.add(new SimpleGrantedAuthority("ROLE_DEV_USER"));

        switch (devUser.getRole()) {
            case SUPER_ADMIN -> {
                authorities.add(new SimpleGrantedAuthority("ROLE_DEV_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_DEV_SUPER_ADMIN"));
            }
            case ADMIN -> authorities.add(new SimpleGrantedAuthority("ROLE_DEV_ADMIN"));
            case USER -> {
                // ROLE_DEV_USER only, already added above.
            }
        }

        return authorities;
    }

}
