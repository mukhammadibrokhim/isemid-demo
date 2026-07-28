package uz.uzinfocom.app.platform.auth.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.auth.application.exception.InvalidLoginCredentialsException;
import uz.uzinfocom.app.platform.auth.application.exception.InvalidLoginRequestException;
import uz.uzinfocom.app.platform.auth.application.exception.LoginProviderMisconfiguredException;
import uz.uzinfocom.app.platform.auth.application.exception.LoginUpstreamException;
import uz.uzinfocom.app.platform.auth.properties.LoginProvidersProperties.ProviderProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

/**
 * The part of a token-endpoint exchange that's identical no matter which
 * OAuth2 grant is used: attach client_id/client_secret (or omit the secret
 * for a public PKCE client), merge any provider-specific extra static
 * fields, POST the form, parse the standard {access_token, refresh_token,
 * token_type, expires_in, scope} JSON shape, and classify every failure
 * mode - upstream 4xx/5xx, network failures, timeouts, and malformed/empty
 * responses - into the right {@code AppException} with the right message.
 * Only the form's grant-specific fields (e.g. {@code code}/{@code
 * code_verifier} for authorization_code) are built by the {@code
 * LoginProvider} implementation itself (see {@link
 * AuthorizationCodeGrantLoginProvider}) - everything else lives here once,
 * ready to be reused by a future grant's provider too.
 */
@Slf4j
final class OAuth2TokenExchangeClient {

    /**
     * Standard OAuth2 {@code error} values that mean the request itself (our
     * client_id/client_secret/grant configuration) was rejected - never the
     * caller's fault. Kept apart from {@code invalid_grant} (an expired or
     * already-used code), which genuinely is.
     */
    private static final Set<String> MISCONFIGURATION_ERRORS = Set.of(
            "invalid_client",
            "unauthorized_client",
            "invalid_request",
            "unsupported_grant_type",
            "invalid_scope"
    );

    private OAuth2TokenExchangeClient() {
    }

    static LoginResult exchange(
            String providerKey,
            RestClient restClient,
            JsonMapper jsonMapper,
            ProviderProperties properties,
            MultiValueMap<String, String> form
    ) {
        attachClientCredentials(form, properties);
        properties.getExtraParams().forEach(form::set);

        RestClient.RequestBodySpec request = restClient.post()
                .uri(properties.getTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON);

        if (properties.isCredentialsInBasicHeader() && StringUtils.hasText(properties.getClientSecret())) {
            request = request.header(HttpHeaders.AUTHORIZATION,
                    basicAuthHeader(properties.getClientId(), properties.getClientSecret()));
        }

        UpstreamTokenResponse response;
        try {
            response = request
                    .body(form)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 400 || status.value() == 401,
                            (req, res) -> handleCredentialOrRequestError(providerKey, res, jsonMapper)
                    )
                    .onStatus(
                            HttpStatusCode::isError,
                            (req, res) -> {
                                log.warn("Login provider returned an unexpected upstream error. "
                                                + "providerKey={}, status={}, upstreamError={}",
                                        providerKey, res.getStatusCode().value(),
                                        upstreamErrorCode(providerKey, res, jsonMapper));
                                throw new LoginUpstreamException(providerKey, res.getStatusCode().value());
                            }
                    )
                    .body(UpstreamTokenResponse.class);
        } catch (RestClientException exception) {
            log.warn("Login provider token exchange failed before a response was received. "
                            + "providerKey={}, errorType={}",
                    providerKey, exception.getClass().getSimpleName());
            throw new LoginUpstreamException(providerKey, exception);
        }

        if (response == null || !StringUtils.hasText(response.accessToken())) {
            throw new LoginUpstreamException(providerKey,
                    new IllegalStateException("Upstream returned no access token"));
        }

        return new LoginResult(
                response.accessToken(),
                response.refreshToken(),
                StringUtils.hasText(response.tokenType()) ? response.tokenType() : "Bearer",
                response.expiresIn(),
                response.scope()
        );
    }

    /**
     * A 400/401 means either "your code was wrong" (OAuth2 {@code
     * invalid_grant}, or any error we can't recognize - kept as the safe
     * default so an upstream we don't fully understand never silently gets
     * classified as "our bug") or "our own client configuration is wrong"
     * ({@link #MISCONFIGURATION_ERRORS}) - which gets its own exception type
     * specifically so it's never confused with a routine bad login in
     * logs/monitoring.
     */
    private static void handleCredentialOrRequestError(
            String providerKey,
            ClientHttpResponse response,
            JsonMapper jsonMapper
    ) {
        String upstreamError = upstreamErrorCode(providerKey, response, jsonMapper);

        if (upstreamError != null && MISCONFIGURATION_ERRORS.contains(upstreamError)) {
            log.warn("Login provider rejected our own client credentials or request shape - check "
                            + "app.auth.login.providers.{}.* configuration. providerKey={}, upstreamError={}",
                    providerKey, providerKey, upstreamError);
            throw new LoginProviderMisconfiguredException(providerKey);
        }

        throw new InvalidLoginCredentialsException(providerKey);
    }

    /**
     * Best-effort read of the standard OAuth2 error body ({@code {"error":
     * "..."}}); {@code null} if the body is empty, isn't JSON, or has no
     * {@code error} field - callers treat {@code null} as "unclassifiable,
     * assume the safe default" rather than failing.
     */
    private static String upstreamErrorCode(String providerKey, ClientHttpResponse response, JsonMapper jsonMapper) {
        try {
            byte[] body = response.getBody().readAllBytes();

            if (body.length == 0) {
                return null;
            }

            JsonNode node = jsonMapper.readTree(new String(body, StandardCharsets.UTF_8));
            JsonNode errorNode = node.get("error");

            return errorNode == null || errorNode.isNull() ? null : errorNode.asText();
        } catch (IOException | RuntimeException exception) {
            log.debug("Could not parse upstream error body as an OAuth2 error - falling back to the safe "
                            + "default classification. providerKey={}, errorType={}",
                    providerKey, exception.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * {@code client_id} always goes in the form body - even when
     * credentials are sent as a Basic header, most token endpoints still
     * expect/accept it there too, and it's not secret. {@code
     * client_secret} is added to the form body only when it's not being
     * sent via Basic header instead; omitted entirely (not sent as an empty
     * string) when {@code requireClientSecret=false} and none is
     * configured - a public PKCE client (e.g. an SPA client registration)
     * authenticates with {@code code_verifier} alone.
     */
    private static void attachClientCredentials(MultiValueMap<String, String> form, ProviderProperties properties) {
        form.add("client_id", properties.getClientId());

        boolean sendSecretInForm = !properties.isCredentialsInBasicHeader();
        boolean haveSecret = StringUtils.hasText(properties.getClientSecret());

        if (properties.isRequireClientSecret() && !haveSecret) {
            throw new IllegalStateException(
                    "Login provider requires a client secret but none is configured: check "
                            + "app.auth.login.providers.*.client-secret or set require-client-secret=false "
                            + "for a public PKCE client");
        }

        if (sendSecretInForm && haveSecret) {
            form.add("client_secret", properties.getClientSecret());
        }
    }

    /**
     * Shared by every {@code LoginProvider} implementation to validate the
     * one field it needs out of the generic {@code LoginRequest}, so each
     * grant-specific provider doesn't repeat the same blank-check-and-throw.
     */
    static String requireField(String value, String messageCode) {
        if (!StringUtils.hasText(value)) {
            throw new InvalidLoginRequestException(messageCode);
        }
        return value;
    }

    private static String basicAuthHeader(String clientId, String clientSecret) {
        String credentials = clientId + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record UpstreamTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Long expiresIn,
            String scope
    ) {
    }
}
