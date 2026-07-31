package uz.uzinfocom.app.platform.auth.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import uz.uzinfocom.app.platform.auth.properties.LoginProvidersProperties.ProviderProperties;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Best-effort calls to a provider's own OAuth2 cleanup endpoints during
 * logout - RFC 7009 token revocation and a separate end-session/logout
 * endpoint, both optional per provider (see {@code revokeUrl}/{@code
 * logoutUrl} on {@link ProviderProperties}). Deliberately never throws: the
 * local token blacklist (see {@code TokenBlacklistService}) is what actually
 * protects this backend immediately, so a network hiccup reaching the
 * provider must not fail the whole logout call and leave the caller's own
 * tokens un-blacklisted.
 */
@Slf4j
final class OAuth2RevocationClient {

    private OAuth2RevocationClient() {
    }

    static void revoke(
            String providerKey,
            RestClient restClient,
            ProviderProperties properties,
            String token,
            String tokenTypeHint
    ) {
        if (!StringUtils.hasText(properties.getRevokeUrl()) || !StringUtils.hasText(token)) {
            return;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("token", token);
        form.add("token_type_hint", tokenTypeHint);
        form.add("client_id", properties.getClientId());

        boolean sendSecretInForm = !properties.isCredentialsInBasicHeader()
                && StringUtils.hasText(properties.getClientSecret());
        if (sendSecretInForm) {
            form.add("client_secret", properties.getClientSecret());
        }

        try {
            RestClient.RequestBodySpec request = restClient.post()
                    .uri(properties.getRevokeUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED);

            if (properties.isCredentialsInBasicHeader() && StringUtils.hasText(properties.getClientSecret())) {
                request = request.header(HttpHeaders.AUTHORIZATION,
                        basicAuthHeader(properties.getClientId(), properties.getClientSecret()));
            }

            request.body(form).retrieve().toBodilessEntity();
        } catch (RestClientException exception) {
            log.warn("Upstream token revocation failed - continuing, the local blacklist already covers "
                            + "this token on this backend. providerKey={}, tokenTypeHint={}, errorType={}",
                    providerKey, tokenTypeHint, exception.getClass().getSimpleName());
        }
    }

    static void endSession(String providerKey, RestClient restClient, ProviderProperties properties) {
        if (!StringUtils.hasText(properties.getLogoutUrl())) {
            return;
        }

        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", properties.getClientId());

            restClient.post()
                    .uri(properties.getLogoutUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            log.warn("Upstream end-session call failed - continuing. providerKey={}, errorType={}",
                    providerKey, exception.getClass().getSimpleName());
        }
    }

    private static String basicAuthHeader(String clientId, String clientSecret) {
        String credentials = clientId + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
