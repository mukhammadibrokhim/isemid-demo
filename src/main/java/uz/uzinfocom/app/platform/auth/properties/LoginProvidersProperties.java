package uz.uzinfocom.app.platform.auth.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Outbound login-proxy configuration: which providers this app can exchange
 * an end user's authorization code (PKCE) for an access token with, and the
 * client_id/client_secret to present while doing so.
 *
 * <p>Deliberately separate from {@code AuthProvidersProperties} (which
 * configures validation of bearer JWTs already issued by SSO/DHP): that side
 * is inbound/resource-server, this side is outbound/login. Provider keys
 * here (e.g. "sso-web", "dhp-web") are intentionally distinct from the
 * inbound side's keys ("sso", "dhp") - see {@code LoginProviderRegistry}'s
 * javadoc for why they must not be conflated.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth.login")
public class LoginProvidersProperties {

    private Map<String, ProviderProperties> providers = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class ProviderProperties {

        private boolean enabled;

        /**
         * Which grant/flow this entry speaks - determines which {@code
         * LoginProviderFactory} {@code LoginProviderRegistry} dispatches it
         * to. Defaults to {@code AUTHORIZATION_CODE}, the only grant in use
         * today, so it doesn't need to be repeated in every provider entry.
         */
        private LoginGrantType grantType = LoginGrantType.AUTHORIZATION_CODE;

        private String tokenUrl;

        private String clientId;

        private String clientSecret;

        /**
         * Some OAuth2 token endpoints expect client_id/client_secret as an
         * HTTP Basic Authorization header instead of form-body parameters.
         * Config-only toggle so a differently-behaving provider needs no
         * code change.
         */
        private boolean credentialsInBasicHeader;

        /**
         * Left blank for a public PKCE client that authenticates with only
         * {@code client_id} + PKCE {@code code_verifier}, no secret at all
         * (e.g. an SPA client registration) - {@code client_secret} is then
         * omitted from the exchange entirely, rather than sent as an empty
         * string.
         */
        private boolean requireClientSecret = true;

        /**
         * Extra static form fields some providers require alongside the
         * standard OAuth2 ones - e.g. SSO's authorization_code exchange also
         * expects {@code claims=organization}. Config-only: a provider
         * needing a field no other provider uses doesn't need a code change,
         * just an entry here.
         */
        private Map<String, String> extraParams = new LinkedHashMap<>();
    }
}
