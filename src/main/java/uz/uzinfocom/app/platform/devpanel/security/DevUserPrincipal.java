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
 */
@Getter
public class DevUserPrincipal extends User {

    private final Long devUserId;

    public DevUserPrincipal(DevUser devUser) {
        super(devUser.getUsername(), devUser.getPasswordHash(), devUser.isEnabled(),
                true, true, true, authorities(devUser));
        this.devUserId = devUser.getId();
    }

    private static List<GrantedAuthority> authorities(DevUser devUser) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_DEV_MONITORING"));
        if (devUser.isRoot()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_DEV_ROOT"));
        }
        return authorities;
    }

}
