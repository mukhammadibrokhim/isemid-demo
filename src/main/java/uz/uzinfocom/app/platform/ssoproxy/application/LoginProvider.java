package uz.uzinfocom.app.platform.ssoproxy.application;

import uz.uzinfocom.app.platform.ssoproxy.properties.LoginGrantType;
import uz.uzinfocom.app.platform.ssoproxy.web.dto.LoginRequest;

/**
 * One authentication provider this app can log an end user into on their
 * behalf (a login proxy), regardless of which OAuth2 grant it speaks.
 * Implementations are resolved by {@code providerKey()} through {@link
 * LoginProviderRegistry} - mirrors how {@code IdentityClaimExtractor} is
 * resolved on the inbound-JWT-validation side.
 *
 * <p>{@code refresh(String)} lives on the same interface as {@code
 * login(...)}, not a separate capability/marker interface: every provider
 * configured here already has the client_id/(optional) client_secret and
 * token endpoint a {@code refresh_token} grant needs - it's the same OAuth2
 * client, just a different {@code grant_type} value, so there's nothing a
 * provider would lack to support it.</p>
 */
public interface LoginProvider {

    String providerKey();

    LoginGrantType grantType();

    LoginResult login(LoginRequest request);

    LoginResult refresh(String refreshToken);

    /**
     * Best-effort cleanup on the provider's own side during logout (RFC 7009
     * revoke, end-session, ...) - never the thing that actually enforces
     * logout on this backend, see {@code TokenBlacklistService} for why.
     * Either argument may be blank if the caller only has one of the two
     * tokens.
     */
    void logout(String accessToken, String refreshToken);
}
