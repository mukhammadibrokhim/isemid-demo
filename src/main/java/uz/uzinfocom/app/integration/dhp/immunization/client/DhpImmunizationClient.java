package uz.uzinfocom.app.integration.dhp.immunization.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.dhp.common.client.DhpHttpClient;
import uz.uzinfocom.app.integration.dhp.common.properties.DhpProperties;
import uz.uzinfocom.app.integration.dhp.common.support.DhpJson;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * FHIR search {@code GET <fhir>/Immunization?<patient-param>=<ni>&_count=N},
 * following the Bundle's {@code next} links. Returns the raw Bundles;
 * mapping happens in the service layer.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DhpImmunizationClient {

    private static final String OPERATION = "DHP_IMMUNIZATION_SEARCH";

    private final DhpHttpClient httpClient;
    private final DhpProperties properties;

    /** @param ni an already validated 14-digit NNUZB */
    public List<JsonNode> searchByNi(String ni) {
        DhpProperties.Fhir fhir = properties.fhir();

        String searchValue = StringUtils.hasText(fhir.identifierSystem())
                ? fhir.identifierSystem().trim() + "|" + ni
                : ni;

        URI next = UriComponentsBuilder.fromUriString(fhir.baseUrl())
                .path(fhir.immunizationEndpoint())
                .queryParam(fhir.patientSearchParam(), searchValue)
                .queryParam("_count", fhir.pageSize())
                .build()
                .encode()
                .toUri();

        List<JsonNode> bundles = new ArrayList<>();

        for (int page = 0; page < fhir.maxPages() && next != null; page++) {
            Optional<JsonNode> bundle = httpClient.getJson(OPERATION, next);
            if (bundle.isEmpty()) {
                next = null;
                break;
            }

            bundles.add(bundle.get());
            next = nextPage(bundle.get(), fhir.baseUrl());
        }

        if (next != null) {
            log.warn("DHP Immunization search hit the page limit ({}) - later pages were not fetched.",
                    fhir.maxPages());
        }

        return bundles;
    }

    /**
     * The Bundle's {@code link[relation=next].url}, only if it stays on the
     * configured FHIR host - a next-link is server-supplied, and following one
     * to another host would send our bearer token there.
     */
    private URI nextPage(JsonNode bundle, String fhirBaseUrl) {
        JsonNode links = DhpJson.child(bundle, "link");
        if (links == null || !links.isArray()) {
            return null;
        }

        for (JsonNode link : links) {
            if (!"next".equals(DhpJson.text(link, "relation"))) {
                continue;
            }

            String url = DhpJson.text(link, "url");
            if (url == null) {
                return null;
            }
            if (!isUnder(url, fhirBaseUrl)) {
                log.warn("Ignoring DHP Immunization next-link outside the configured FHIR base URL.");
                return null;
            }

            try {
                return URI.create(url);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }

        return null;
    }

    private static boolean isUnder(String url, String baseUrl) {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        return url.startsWith(base);
    }
}
