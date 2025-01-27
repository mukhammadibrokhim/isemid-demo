package uz.uzinfocom.isemid.configs.jwt;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uz.uzinfocom.isemid.models.entities.Role;
import uz.uzinfocom.isemid.models.entities.User;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class AuthToken extends AbstractAuthenticationToken {
    private final Jwt jwt;
    private final User user;
    private final Set<Role> roles;

    public AuthToken(Jwt jwt, User user, Set<Role> roles) {
        super(rolesToAuthorities(roles));
        this.jwt = jwt;
        this.user = user;
        this.roles = roles;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt.getTokenValue();
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    private static Set<GrantedAuthority> rolesToAuthorities(Set<Role> roles) {
        return roles.stream()
                .filter(role -> role != null && role.getName() != null)
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
    }
}
