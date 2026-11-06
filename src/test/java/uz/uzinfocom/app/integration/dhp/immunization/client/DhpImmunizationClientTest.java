package uz.uzinfocom.app.integration.dhp.immunization.client;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.dhp.common.DhpTestProperties;
import uz.uzinfocom.app.integration.dhp.common.client.DhpHttpClient;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the two-step search: {@code Patient?identifier=} resolves an id,
 * then {@code Immunization?patient=Patient/<id>} is paged. A direct
 * patient-identifier search on Immunization was tried first but confirmed
 * live against the playground (2026-09-21) to never filter correctly -
 * see {@link DhpImmunizationClient}.
 */
class DhpImmunizationClientTest {

    private static final String NI = "30101900123456";
    private static final String FHIR = DhpTestProperties.FHIR_BASE;
    private static final String PATIENT_ID = "patient-abc-123";

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final DhpHttpClient httpClient = mock(DhpHttpClient.class);

    @Test
    void resolvesThePatientThenSearchesImmunizationByReference() {
        when(httpClient.getJson(any(), any()))
                .thenReturn(Optional.of(patientBundle(PATIENT_ID)))
                .thenReturn(Optional.of(immunizationBundle(null)));

        List<JsonNode> bundles = client(null).searchByNi(NI);

        assertThat(bundles).hasSize(1);
        List<URI> uris = requestedUris(2);
        assertThat(uris.get(0)).isEqualTo(patientLookupUri(NI));
        assertThat(uris.get(1)).isEqualTo(immunizationSearchUri(PATIENT_ID, 100));
    }

    @Test
    void prefixesTheIdentifierSystemOnThePatientLookup() {
        when(httpClient.getJson(any(), any()))
                .thenReturn(Optional.of(patientBundle(PATIENT_ID)))
                .thenReturn(Optional.of(immunizationBundle(null)));

        client("https://dhp.uz/ni").searchByNi(NI);

        assertThat(requestedUris(2).get(0).toString())
                .contains("identifier=https://dhp.uz/ni%7C" + NI);
    }

    @Test
    void noMatchingPatientMeansNoImmunizationCallAndNoResults() {
        when(httpClient.getJson(any(), any())).thenReturn(Optional.of(patientNotFoundBundle()));

        List<JsonNode> bundles = client(null).searchByNi(NI);

        assertThat(bundles).isEmpty();
        verify(httpClient, times(1)).getJson(any(), any());
    }

    @Test
    void noPatientBundleMeansNoResults() {
        when(httpClient.getJson(any(), any())).thenReturn(Optional.empty());

        assertThat(client(null).searchByNi(NI)).isEmpty();
        verify(httpClient, times(1)).getJson(any(), any());
    }

    @Test
    void followsNextLinksUpToTheLastPage() {
        String page2 = FHIR + "/Immunization?page=2";
        when(httpClient.getJson(any(), any()))
                .thenReturn(Optional.of(patientBundle(PATIENT_ID)))
                .thenReturn(Optional.of(immunizationBundle(page2)))
                .thenReturn(Optional.of(immunizationBundle(null)));

        List<JsonNode> bundles = client(null).searchByNi(NI);

        assertThat(bundles).hasSize(2);
        assertThat(requestedUris(3).get(2)).isEqualTo(URI.create(page2));
    }

    @Test
    void neverFollowsANextLinkToAnotherHost() {
        when(httpClient.getJson(any(), any()))
                .thenReturn(Optional.of(patientBundle(PATIENT_ID)))
                .thenReturn(Optional.of(immunizationBundle("https://evil.example/steal?token=1")));

        List<JsonNode> bundles = client(null).searchByNi(NI);

        assertThat(bundles).hasSize(1);
        verify(httpClient, times(2)).getJson(any(), any());
    }

    @Test
    void stopsAtTheConfiguredPageLimit() {
        when(httpClient.getJson(any(), any()))
                .thenReturn(Optional.of(patientBundle(PATIENT_ID)))
                .thenAnswer(invocation -> Optional.of(immunizationBundle(FHIR + "/Immunization?page=next")));

        List<JsonNode> bundles = client(null).searchByNi(NI);

        assertThat(bundles).hasSize(3); // DhpTestProperties.maxPages
    }

    private DhpImmunizationClient client(String identifierSystem) {
        return new DhpImmunizationClient(httpClient, DhpTestProperties.with("id", "secret", identifierSystem));
    }

    private URI patientLookupUri(String ni) {
        return UriComponentsBuilder.fromUriString(FHIR)
                .path("/Patient")
                .queryParam("identifier", ni)
                .build()
                .encode()
                .toUri();
    }

    private URI immunizationSearchUri(String patientId, int pageSize) {
        return UriComponentsBuilder.fromUriString(FHIR)
                .path("/Immunization")
                .queryParam("patient", "Patient/" + patientId)
                .queryParam("_count", pageSize)
                .build()
                .encode()
                .toUri();
    }

    private JsonNode patientBundle(String patientId) {
        return jsonMapper.readTree("""
                {"resourceType":"Bundle","entry":[
                  {"resource":{"resourceType":"Patient","id":"%s"}}
                ]}""".formatted(patientId));
    }

    private JsonNode patientNotFoundBundle() {
        return jsonMapper.readTree("""
                {"resourceType":"Bundle","entry":[
                  {"resource":{"resourceType":"OperationOutcome","issue":[{"code":"not-found"}]}}
                ]}""");
    }

    private JsonNode immunizationBundle(String nextUrl) {
        String links = nextUrl == null ? "[]" : "[{\"relation\":\"next\",\"url\":\"" + nextUrl + "\"}]";
        return jsonMapper.readTree("{\"resourceType\":\"Bundle\",\"link\":" + links + ",\"entry\":[]}");
    }

    private List<URI> requestedUris(int expectedCalls) {
        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);
        verify(httpClient, times(expectedCalls)).getJson(any(), captor.capture());
        return captor.getAllValues();
    }
}
