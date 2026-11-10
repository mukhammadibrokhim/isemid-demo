package uz.uzinfocom.app.platform.devpanel.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Authenticated dev-panel request. {@code mfaVerified} is per token, not per
 * account: a token issued before the second factor was enrolled (or when
 * enrolment is still outstanding) is "limited" and {@code DevTwoFactorGuardFilter}
 * confines it to the change-password / 2FA-setup endpoints.
 */
public class DevAuthenticationToken extends AbstractAuthenticationToken {

    private final DevUserPrincipal principal;

    @Getter
    private final boolean mfaVerified;

    public DevAuthenticationToken(DevUserPrincipal principal, boolean mfaVerified) {
        super(principal.getAuthorities());
        this.principal = principal;
        this.mfaVerified = mfaVerified;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return principal.getUsername();
    }
}
