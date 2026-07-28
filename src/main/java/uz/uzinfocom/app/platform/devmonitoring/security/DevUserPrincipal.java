package uz.uzinfocom.app.platform.devmonitoring.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevUser;

import java.util.List;

/**
 * Carries the {@link DevUser} id through Spring Security's {@code UserDetails}
 * so command services can attribute an action (e.g. resolving a {@code DevErrorLog})
 * to a specific dev-panel account without a second DB lookup.
 */
public class DevUserPrincipal extends User {

    private final Long devUserId;

    public DevUserPrincipal(DevUser devUser) {
        super(devUser.getUsername(), devUser.getPasswordHash(), devUser.isEnabled(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_DEV_MONITORING")));
        this.devUserId = devUser.getId();
    }

    public Long getDevUserId() {
        return devUserId;
    }
}
