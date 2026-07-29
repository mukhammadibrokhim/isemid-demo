package uz.uzinfocom.app.platform.auth.application;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.auth.properties.LoginGrantType;
import uz.uzinfocom.app.platform.auth.properties.LoginProvidersProperties.ProviderProperties;
import uz.uzinfocom.app.platform.auth.web.dto.LoginRequest;
import uz.uzinfocom.app.platform.resilience.DynamicCircuitBreakerLookup;

/**
 * OAuth2 "authorization_code" (+ PKCE) grant login: exchanges a {@code code}
 * obtained from the provider's own login-page redirect for an access token,
 * the same exchange DHP requires. One instance handles one configured
 * provider (see {@link LoginProviderRegistry}); adding another provider that
 * speaks the same grant is a config-only change.
 *
 * <p>Also handles {@code refresh_token} grant exchanges for the same
 * provider - it's the same client_id/client_secret/tokenUrl, only {@code
 * grant_type} differs, so no separate class is needed.</p>
 */
public class AuthorizationCodeGrantLoginProvider implements LoginProvider {

    private static final String GRANT_TYPE = "authorization_code";
    private static final String REFRESH_GRANT_TYPE = "refresh_token";

    private final String providerKey;
    private final ProviderProperties properties;
    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final DynamicCircuitBreakerLookup circuitBreakerLookup;

    public AuthorizationCodeGrantLoginProvider(
            String providerKey,
            ProviderProperties properties,
            RestClient restClient,
            JsonMapper jsonMapper,
            DynamicCircuitBreakerLookup circuitBreakerLookup
    ) {
        this.providerKey = providerKey;
        this.properties = properties;
        this.restClient = restClient;
        this.jsonMapper = jsonMapper;
        this.circuitBreakerLookup = circuitBreakerLookup;
    }

    @Override
    public String providerKey() {
        return providerKey;
    }

    @Override
    public LoginGrantType grantType() {
        return LoginGrantType.AUTHORIZATION_CODE;
    }

    @Override
    public LoginResult login(LoginRequest request) {
        String code = OAuth2TokenExchangeClient.requireField(request.code(), "auth.login.code.required");
        String codeVerifier = OAuth2TokenExchangeClient.requireField(
                request.codeVerifier(), "auth.login.code-verifier.required");
        String redirectUri = OAuth2TokenExchangeClient.requireField(
                request.redirectUri(), "auth.login.redirect-uri.required");

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", GRANT_TYPE);
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("code_verifier", codeVerifier);

        return OAuth2TokenExchangeClient.exchange(
                providerKey, restClient, jsonMapper, properties, form, circuitBreakerLookup);
    }

    @Override
    public LoginResult refresh(String refreshToken) {
        String token = OAuth2TokenExchangeClient.requireField(refreshToken, "auth.login.refresh-token.required");

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", REFRESH_GRANT_TYPE);
        form.add("refresh_token", token);

        return OAuth2TokenExchangeClient.exchange(
                providerKey, restClient, jsonMapper, properties, form, circuitBreakerLookup);
    }
}
