package uz.uzinfocom.app.platform.ssoproxy.properties;

/**
 * Which OAuth2 grant a configured login provider speaks. Determines which
 * {@code LoginProviderFactory} picks up a given {@code
 * app.auth.login.providers.<key>.*} entry - each provider entry belongs to
 * exactly one grant/flow.
 *
 * <p>Only {@code AUTHORIZATION_CODE} is in active use today (`sso-web`,
 * `dhp-web`) - the legacy password grant (username/password posted directly
 * to our backend) was removed once redirect-based login covered every
 * provider. The enum/factory extension point is kept regardless, so a
 * future grant (e.g. {@code client_credentials}) still only needs a new
 * value here plus a matching {@code LoginProviderFactory} - no changes to
 * {@code LoginProviderRegistry}, {@code LoginService}, or {@code
 * LoginController}.</p>
 */
public enum LoginGrantType {

    /**
     * Authorization Code + PKCE grant: the user authenticates on the
     * provider's own login page (browser redirect), the provider redirects
     * back with a {@code code}, and the frontend hands that code (plus the
     * PKCE {@code code_verifier} and the {@code redirect_uri} it used) to us
     * for exchange. Used by `sso-web` and `dhp-web`.
     */
    AUTHORIZATION_CODE
}
