package uz.uzinfocom.app.modules.form058.application.shared;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.security.auth.FederatedAuthenticationToken;

@Component
public class CurrentForm058User {

    public Long userIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof FederatedAuthenticationToken token) {
            return token.getUserId();
        }
        return null;
    }
}
