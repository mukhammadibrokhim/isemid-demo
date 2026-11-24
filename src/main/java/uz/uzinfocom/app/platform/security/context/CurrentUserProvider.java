package uz.uzinfocom.app.platform.security.context;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.security.auth.FederatedAuthenticationToken;
import uz.uzinfocom.app.platform.security.jwt.JwtProviderResolver;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final JwtProviderResolver jwtProviderResolver;

    public Long userIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof FederatedAuthenticationToken token) {
            return token.getUserId();
        }
        return null;
    }

    public String rawTokenOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof FederatedAuthenticationToken token) {
            return token.getJwt().getTokenValue();
        }
        return null;
    }

    /**
     * The IAM provider ("sso"/"dhp") that issued the current request's own
     * token — resolved the same way authentication itself resolves it, from
     * the JWT's issuer/claims, not from any stored data.
     */
    public String currentProviderKeyOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof FederatedAuthenticationToken token) {
            return jwtProviderResolver.resolveProviderKey(token.getJwt());
        }
        return null;
    }
}
