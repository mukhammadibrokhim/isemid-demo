package uz.uzinfocom.app.platform.iam.infrastructure.remote;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.payload.RemoteOrganizationPayload;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.payload.RemotePractitionerPayload;
import uz.uzinfocom.app.platform.resilience.CircuitBreakerNames;
import uz.uzinfocom.app.platform.resilience.DynamicCircuitBreakerLookup;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;

import java.util.UUID;

@Component
public class ProviderIamRemoteClient {

    private final RestClient restClient;
    private final AuthProvidersProperties properties;
    private final DynamicCircuitBreakerLookup circuitBreakerLookup;

    public ProviderIamRemoteClient(
            RestClient restClient,
            AuthProvidersProperties properties,
            DynamicCircuitBreakerLookup circuitBreakerLookup
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.circuitBreakerLookup = circuitBreakerLookup;
    }

    public RemoteOrganizationPayload fetchOrganization(String providerKey, UUID organizationUuid, String rawToken) {
        String url = expandUuidTemplate(
                properties.requireProvider(providerKey).getIam().getOrganizationUrlTemplate(),
                organizationUuid,
                "organization"
        );

        RemoteOrganizationPayload response = executeGet(providerKey, url, rawToken, RemoteOrganizationPayload.class);

        if (response == null) {
            throw new IllegalStateException("Remote organization response is empty: " + organizationUuid);
        }
        return response;
    }

    public RemotePractitionerPayload fetchPractitioner(String providerKey, UUID practitionerUuid, String rawToken) {
        String url = expandUuidTemplate(
                properties.requireProvider(providerKey).getIam().getPractitionerUrlTemplate(),
                practitionerUuid,
                "practitioner"
        );

        RemotePractitionerPayload response = executeGet(providerKey, url, rawToken, RemotePractitionerPayload.class);

        if (response == null) {
            throw new IllegalStateException("Remote practitioner response is empty: " + practitionerUuid);
        }
        return response;
    }

    private <T> T executeGet(String providerKey, String url, String rawToken, Class<T> responseType) {
        try {
            return circuitBreakerLookup
                    .forProvider(CircuitBreakerNames.IAM_REMOTE, providerKey)
                    .executeSupplier(() -> restClient.get()
                            .uri(url)
                            .header(HttpHeaders.AUTHORIZATION, bearer(rawToken))
                            .retrieve()
                            .body(responseType));
        } catch (CallNotPermittedException exception) {
            throw new IllegalStateException(
                    "IAM provider circuit breaker is open: " + providerKey, exception);
        }
    }

    private String expandUuidTemplate(String template, UUID uuid, String resourceName) {
        if (!StringUtils.hasText(template)) {
            throw new IllegalStateException("IAM " + resourceName + " URL template is not configured");
        }
        return template.replace("{uuid}", uuid.toString());
    }

    private String bearer(String rawToken) {
        return "Bearer " + rawToken;
    }
}
