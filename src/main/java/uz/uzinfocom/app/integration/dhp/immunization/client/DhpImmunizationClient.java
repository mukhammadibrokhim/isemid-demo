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
 * Two-step FHIR search: {@code GET <fhir>/Patient?identifier=<ni>} resolves
 * the citizen's Patient id, then {@code GET <fhir>/Immunization?patient=
 * Patient/<id>&_count=N} fetches their records, following the Bundle's
 * {@code next} links. A direct reference search is used rather than a
 * patient-identifier search on Immunization itself because DHP's FHIR server
 * does not support the latter - confirmed live against the playground
 * 2026-09-21 (see {@link DhpProperties.Fhir#patientEndpoint()}). Returns the
 * raw Immunization Bundles; mapping happens in the service layer.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DhpImmunizationClient {

    private static final String PATIENT_LOOKUP_OPERATION = "DHP_IMMUNIZATION_PATIENT_LOOKUP";
    private static final String SEARCH_OPERATION = "DHP_IMMUNIZATION_SEARCH";
    private static final String PATIENT_RESOURCE_TYPE = "Patient";

    private final DhpHttpClient httpClient;
    private final DhpProperties properties;

    /** @param ni an already validated 14-digit NNUZB */
    public List<JsonNode> searchByNi(String ni) {
        DhpProperties.Fhir fhir = properties.fhir();

        Optional<String> patientId = resolvePatientId(ni, fhir);
        if (patientId.isEmpty()) {
            return List.of();
        }

        URI next = UriComponentsBuilder.fromUriString(fhir.baseUrl())
                .path(fhir.immunizationEndpoint())
                .queryParam("patient", PATIENT_RESOURCE_TYPE + "/" + patientId.get())
                .queryParam("_count", fhir.pageSize())
                .build()
                .encode()
                .toUri();

        List<JsonNode> bundles = new ArrayList<>();

        for (int page = 0; page < fhir.maxPages() && next != null; page++) {
            Optional<JsonNode> bundle = httpClient.getJson(SEARCH_OPERATION, next);
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

    /** The citizen's Patient id resolved by identifier search; empty when DHP has no matching Patient. */
    private Optional<String> resolvePatientId(String ni, DhpProperties.Fhir fhir) {
        String searchValue = StringUtils.hasText(fhir.identifierSystem())
                ? fhir.identifierSystem().trim() + "|" + ni
                : ni;

        URI uri = UriComponentsBuilder.fromUriString(fhir.baseUrl())
                .path(fhir.patientEndpoint())
                .queryParam("identifier", searchValue)
                .build()
                .encode()
                .toUri();

        Optional<JsonNode> bundle = httpClient.getJson(PATIENT_LOOKUP_OPERATION, uri);
        if (bundle.isEmpty()) {
            return Optional.empty();
        }

        JsonNode entries = DhpJson.child(bundle.get(), "entry");
        if (entries == null || !entries.isArray()) {
            return Optional.empty();
        }

        for (JsonNode entry : entries) {
            JsonNode resource = DhpJson.child(entry, "resource");
            if (resource == null || !PATIENT_RESOURCE_TYPE.equals(DhpJson.text(resource, "resourceType"))) {
                continue;
            }
            String id = DhpJson.text(resource, "id");
            if (id != null) {
                return Optional.of(id);
            }
        }

        return Optional.empty();
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
