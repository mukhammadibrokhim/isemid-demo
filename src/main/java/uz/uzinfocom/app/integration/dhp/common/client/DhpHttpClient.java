package uz.uzinfocom.app.integration.dhp.common.client;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.dhp.common.auth.DhpAccessTokenProvider;
import uz.uzinfocom.app.integration.dhp.common.exception.DhpAccessDeniedException;
import uz.uzinfocom.app.integration.dhp.common.exception.DhpIntegrationException;
import uz.uzinfocom.app.integration.dhp.common.exception.DhpRequestRejectedException;
import uz.uzinfocom.app.platform.resilience.CircuitBreakerNames;
import uz.uzinfocom.app.platform.resilience.DynamicCircuitBreakerLookup;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

/**
 * The one place DHP GET calls go through: attaches the M2M bearer token, runs
 * inside the shared {@code dhp} circuit breaker, retries once with a fresh
 * token after a 401, and classifies every failure into a {@link
 * DhpIntegrationException} subtype. Callers get the parsed JSON body, or
 * {@link Optional#empty()} for a 404 or an empty 2xx body ("no data").
 */
@Slf4j
@Component
public class DhpHttpClient {

    private static final MediaType FHIR_JSON = MediaType.valueOf("application/fhir+json");

    private final RestClient restClient;
    private final DhpAccessTokenProvider tokenProvider;
    private final DynamicCircuitBreakerLookup circuitBreakerLookup;
    private final JsonMapper jsonMapper;

    public DhpHttpClient(
            @Qualifier("dhpRestClient") RestClient restClient,
            DhpAccessTokenProvider tokenProvider,
            DynamicCircuitBreakerLookup circuitBreakerLookup,
            JsonMapper jsonMapper
    ) {
        this.restClient = restClient;
        this.tokenProvider = tokenProvider;
        this.circuitBreakerLookup = circuitBreakerLookup;
        this.jsonMapper = jsonMapper;
    }

    public Optional<JsonNode> getJson(String operation, URI uri) {
        RawResponse response = send(operation, uri);

        if (response.status() == 401) {
            // The cached token may have been revoked or expired early - drop it and try once more.
            tokenProvider.invalidate(response.tokenUsed());
            response = send(operation, uri);
        }

        if (response.status() == 401) {
            log.warn("DHP rejected a freshly issued M2M token. operation={}", operation);
            throw new DhpAccessDeniedException(operation);
        }

        if (response.status() == 404) {
            return Optional.empty();
        }

        return Optional.ofNullable(response.body());
    }

    private RawResponse send(String operation, URI uri) {
        String token = tokenProvider.getAccessToken();

        try {
            return circuitBreakerLookup.forName(CircuitBreakerNames.DHP).executeSupplier(() -> restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON, FHIR_JSON)
                    .exchange((request, response) -> handle(operation, response, token)));
        } catch (DhpIntegrationException exception) {
            throw exception;
        } catch (CallNotPermittedException exception) {
            throw DhpIntegrationException.unavailable(operation, exception);
        } catch (RestClientException exception) {
            log.warn("DHP request failed before a response was received. operation={}, errorType={}",
                    operation, exception.getClass().getSimpleName());
            throw DhpIntegrationException.unavailable(operation, exception);
        }
    }

    private RawResponse handle(String operation, ClientHttpResponse response, String token) throws IOException {
        int status = response.getStatusCode().value();

        if (status == 401 || status == 404) {
            return new RawResponse(status, null, token);
        }
        if (status == 400 || status == 422) {
            throw new DhpRequestRejectedException(operation);
        }
        if (status == 403) {
            log.warn("DHP denied the M2M client access. operation={}", operation);
            throw new DhpAccessDeniedException(operation);
        }
        if (response.getStatusCode().isError()) {
            log.warn("DHP returned an error. operation={}, status={}", operation, status);
            throw DhpIntegrationException.upstream(operation);
        }

        return new RawResponse(status, readBody(operation, response), token);
    }

    private JsonNode readBody(String operation, ClientHttpResponse response) throws IOException {
        try (InputStream stream = response.getBody()) {
            byte[] bytes = stream.readAllBytes();
            if (bytes.length == 0) {
                return null;
            }
            return jsonMapper.readTree(bytes);
        } catch (JacksonException exception) {
            throw DhpIntegrationException.malformed(operation, exception);
        }
    }

    private record RawResponse(int status, JsonNode body, String tokenUsed) {
    }
}
