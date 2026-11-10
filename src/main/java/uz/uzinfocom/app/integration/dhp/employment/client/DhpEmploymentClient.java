package uz.uzinfocom.app.integration.dhp.employment.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.dhp.common.client.DhpHttpClient;
import uz.uzinfocom.app.integration.dhp.common.properties.DhpProperties;

import java.net.URI;
import java.util.Optional;

/** {@code GET <egov>/mol/citizen/employment/by-ni?ni=} - raw JSON, mapping happens in the service layer. */
@Component
@RequiredArgsConstructor
public class DhpEmploymentClient {

    private static final String OPERATION = "DHP_EMPLOYMENT_BY_NI";

    private final DhpHttpClient httpClient;
    private final DhpProperties properties;

    /** @param ni an already validated 14-digit NNUZB */
    public Optional<JsonNode> findByNi(String ni) {
        DhpProperties.Employment employment = properties.employment();

        URI uri = UriComponentsBuilder.fromUriString(employment.baseUrl())
                .path(employment.byNiEndpoint())
                .queryParam("ni", ni)
                .build()
                .toUri();

        return httpClient.getJson(OPERATION, uri);
    }
}
