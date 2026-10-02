package uz.uzinfocom.app.integration.dhp.common.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import uz.uzinfocom.app.integration.dhp.common.exception.DhpIntegrationException;
import uz.uzinfocom.app.integration.dhp.common.properties.DhpProperties;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Obtains and caches the DHP M2M bearer token ({@code client_credentials}).
 * Credentials go in the form body - confirmed live against
 * {@code playground.dhp.uz/sso/oauth/token}, which answers with a
 * {@code bearer} token valid for about an hour.
 *
 * <p>The token is reused until {@code integration.dhp.token-expiry-skew}
 * before its expiry. A downstream 401 makes the caller {@link
 * #invalidate(String) invalidate} the token it used so the next call fetches
 * a fresh one. Refreshing is serialized so a burst of concurrent lookups
 * triggers one token request, not many.
 */
@Slf4j
@Component
public class DhpAccessTokenProvider {

    private static final String OPERATION = "DHP_TOKEN";
    private static final Duration FALLBACK_LIFETIME = Duration.ofMinutes(5);

    private final RestClient restClient;
    private final DhpProperties properties;
    private final Clock clock;
    private final Object refreshLock = new Object();

    private volatile CachedToken cached;

    @Autowired
    public DhpAccessTokenProvider(@Qualifier("dhpRestClient") RestClient restClient, DhpProperties properties) {
        this(restClient, properties, Clock.systemUTC());
    }

    DhpAccessTokenProvider(RestClient restClient, DhpProperties properties, Clock clock) {
        this.restClient = restClient;
        this.properties = properties;
        this.clock = clock;
    }

    public String getAccessToken() {
        CachedToken current = cached;
        if (current != null && current.usableAt(clock.instant())) {
            return current.value();
        }

        synchronized (refreshLock) {
            current = cached;
            if (current != null && current.usableAt(clock.instant())) {
                return current.value();
            }

            CachedToken fresh = fetch();
            cached = fresh;
            return fresh.value();
        }
    }

    /**
     * Drops the cached token, but only if it is still the one the caller was
     * rejected with - a concurrent refresh may already have replaced it.
     */
    public void invalidate(String rejectedToken) {
        CachedToken current = cached;
        if (current != null && current.value().equals(rejectedToken)) {
            cached = null;
        }
    }

    private CachedToken fetch() {
        if (!StringUtils.hasText(properties.clientId()) || !StringUtils.hasText(properties.clientSecret())) {
            log.error("DHP M2M client is not configured - set integration.dhp.client-id / client-secret "
                    + "(DHP_M2M_CLIENT_ID / DHP_M2M_CLIENT_SECRET).");
            throw DhpIntegrationException.notConfigured(OPERATION);
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());

        TokenResponse response;
        try {
            response = restClient.post()
                    .uri(properties.tokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .onStatus(DhpAccessTokenProvider::isCredentialRejection, (request, res) -> {
                        log.error("DHP token endpoint rejected the M2M client credentials. status={}",
                                res.getStatusCode().value());
                        throw DhpIntegrationException.notConfigured(OPERATION);
                    })
                    .onStatus(HttpStatusCode::isError, (request, res) -> {
                        log.warn("DHP token endpoint failed. status={}", res.getStatusCode().value());
                        throw DhpIntegrationException.upstream(OPERATION);
                    })
                    .body(TokenResponse.class);
        } catch (RestClientException exception) {
            log.warn("DHP token request failed before a response was received. errorType={}",
                    exception.getClass().getSimpleName());
            throw DhpIntegrationException.unavailable(OPERATION, exception);
        }

        if (response == null || !StringUtils.hasText(response.accessToken())) {
            throw DhpIntegrationException.malformed(OPERATION, null);
        }

        Duration lifetime = response.expiresIn() == null || response.expiresIn() <= 0
                ? FALLBACK_LIFETIME
                : Duration.ofSeconds(response.expiresIn());
        Duration skew = properties.tokenExpirySkew();
        // A token shorter-lived than the skew would never be reusable - fall back to half its life.
        Duration usableFor = lifetime.compareTo(skew) > 0 ? lifetime.minus(skew) : lifetime.dividedBy(2);

        return new CachedToken(response.accessToken(), clock.instant().plus(usableFor));
    }

    private static boolean isCredentialRejection(HttpStatusCode status) {
        int value = status.value();
        return value == 400 || value == 401 || value == 403;
    }

    private record CachedToken(String value, Instant usableUntil) {
        boolean usableAt(Instant now) {
            return now.isBefore(usableUntil);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Long expiresIn
    ) {
    }
}
