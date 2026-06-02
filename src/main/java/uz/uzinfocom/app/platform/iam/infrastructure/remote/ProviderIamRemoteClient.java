package uz.uzinfocom.app.platform.iam.infrastructure.remote;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.payload.RemoteOrganizationPayload;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.payload.RemotePractitionerPayload;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;

import java.util.UUID;

@Component
public class ProviderIamRemoteClient {

    private final RestClient restClient;
    private final AuthProvidersProperties properties;

    public ProviderIamRemoteClient(
            RestClient restClient,
            AuthProvidersProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public RemoteOrganizationPayload fetchOrganization(String providerKey, UUID organizationUuid, String rawToken) {
        String url = expandUuidTemplate(
                properties.requireProvider(providerKey).getIam().getOrganizationUrlTemplate(),
                organizationUuid,
                "organization"
        );

        RemoteOrganizationPayload response = restClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, bearer(rawToken))
                .retrieve()
                .body(RemoteOrganizationPayload.class);

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

        RemotePractitionerPayload response = restClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, bearer(rawToken))
                .retrieve()
                .body(RemotePractitionerPayload.class);

        if (response == null) {
            throw new IllegalStateException("Remote practitioner response is empty: " + practitionerUuid);
        }
        return response;
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
