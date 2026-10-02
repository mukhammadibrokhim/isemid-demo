package uz.uzinfocom.app.orchestration.webhook.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.platform.integrationclient.domain.OutboundHttpMethod;
import uz.uzinfocom.app.platform.integrationclient.domain.OutboundWebhookAuthType;
import uz.uzinfocom.app.platform.resilience.CircuitBreakerNames;
import uz.uzinfocom.app.platform.resilience.DynamicCircuitBreakerLookup;

import java.net.URI;

/**
 * The only class that speaks HTTP for the outbound webhook feature - mirrors
 * {@code LisActClient}: no repository or transaction access, takes a
 * fully-built target/credential/payload and returns the response's HTTP
 * status. Unlike {@code LisActClient} there is one shared RestClient but a
 * per-{@code IntegrationClient} circuit-breaker instance (see
 * {@link DynamicCircuitBreakerLookup#forProvider}) - one slow/broken partner
 * callback must never trip the breaker for every other client's webhook.
 *
 * <p>Returns the response status as-is, including non-2xx - it is the
 * {@code OutboundWebhookDispatchSendOrchestrator}'s job to decide what counts
 * as success and whether to retry, not this class's. Transport-level
 * failures (timeout, connection refused, circuit breaker open) are left to
 * propagate as unchecked exceptions from the caller's underlying
 * {@code RestClientException}/{@code CallNotPermittedException} - the
 * orchestrator's broad catch handles every failure mode uniformly.
 */
@Slf4j
@Component
public class OutboundWebhookClient {

    private final RestClient restClient;
    private final DynamicCircuitBreakerLookup circuitBreakerLookup;

    public OutboundWebhookClient(
            @Qualifier("outboundWebhookRestClient") RestClient restClient,
            DynamicCircuitBreakerLookup circuitBreakerLookup
    ) {
        this.restClient = restClient;
        this.circuitBreakerLookup = circuitBreakerLookup;
    }

    public int send(
            Long integrationClientId,
            String url,
            OutboundHttpMethod method,
            OutboundWebhookAuthType authType,
            String username,
            String headerName,
            String decryptedSecret,
            String payloadJson
    ) {
        URI uri = URI.create(url);

        return circuitBreakerLookup.forProvider(CircuitBreakerNames.OUTBOUND_WEBHOOK, integrationClientId.toString())
                .executeSupplier(() -> requestSpec(method, uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers -> applyAuth(headers, authType, username, headerName, decryptedSecret))
                        .body(payloadJson)
                        .exchange((request, response) -> response.getStatusCode().value()));
    }

    private RestClient.RequestBodySpec requestSpec(OutboundHttpMethod method, URI uri) {
        return switch (method) {
            case POST -> restClient.post().uri(uri);
            case PATCH -> restClient.patch().uri(uri);
        };
    }

    private void applyAuth(
            HttpHeaders headers,
            OutboundWebhookAuthType authType,
            String username,
            String headerName,
            String decryptedSecret
    ) {
        switch (authType) {
            case NONE -> {
                // no credential to attach
            }
            case BASIC_AUTH -> headers.setBasicAuth(username, decryptedSecret);
            case BEARER_TOKEN -> headers.setBearerAuth(decryptedSecret);
            case API_KEY_HEADER -> headers.set(headerName, decryptedSecret);
        }
    }
}
