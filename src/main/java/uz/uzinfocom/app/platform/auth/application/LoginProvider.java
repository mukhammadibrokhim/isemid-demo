package uz.uzinfocom.app.platform.auth.application;

import uz.uzinfocom.app.platform.auth.properties.LoginGrantType;
import uz.uzinfocom.app.platform.auth.web.dto.LoginRequest;

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
}
