package uz.uzinfocom.app.integration.dhp.immunization.client;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

class DhpImmunizationClientTest {

    private static final String NI = "30101900123456";
    private static final String FHIR = DhpTestProperties.FHIR_BASE;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final DhpHttpClient httpClient = mock(DhpHttpClient.class);

    @Test
    void searchesByPatientIdentifierWithPageSize() {
        DhpImmunizationClient client = client(null);
        when(httpClient.getJson(any(), any())).thenReturn(Optional.of(bundle(null)));

        List<JsonNode> bundles = client.searchByNi(NI);

        assertThat(bundles).hasSize(1);
        assertThat(requestedUris(1)).containsExactly(
                URI.create(FHIR + "/Immunization?patient.identifier=" + NI + "&_count=100"));
    }

    @Test
    void prefixesTheIdentifierSystemWhenConfigured() {
        DhpImmunizationClient client = client("https://dhp.uz/ni");
        when(httpClient.getJson(any(), any())).thenReturn(Optional.of(bundle(null)));

        client.searchByNi(NI);

        assertThat(requestedUris(1).get(0).toString())
                .contains("patient.identifier=https://dhp.uz/ni%7C" + NI);
    }

    @Test
    void followsNextLinksUpToTheLastPage() {
        String page2 = FHIR + "/Immunization?page=2";
        when(httpClient.getJson(any(), any()))
                .thenReturn(Optional.of(bundle(page2)))
                .thenReturn(Optional.of(bundle(null)));

        List<JsonNode> bundles = client(null).searchByNi(NI);

        assertThat(bundles).hasSize(2);
        assertThat(requestedUris(2).get(1)).isEqualTo(URI.create(page2));
    }

    @Test
    void neverFollowsANextLinkToAnotherHost() {
        when(httpClient.getJson(any(), any())).thenReturn(Optional.of(bundle("https://evil.example/steal?token=1")));

        List<JsonNode> bundles = client(null).searchByNi(NI);

        assertThat(bundles).hasSize(1);
        verify(httpClient, times(1)).getJson(any(), any());
    }

    @Test
    void stopsAtTheConfiguredPageLimit() {
        when(httpClient.getJson(any(), any()))
                .thenAnswer(invocation -> Optional.of(bundle(FHIR + "/Immunization?page=next")));

        List<JsonNode> bundles = client(null).searchByNi(NI);

        assertThat(bundles).hasSize(3); // DhpTestProperties.maxPages
    }

    @Test
    void noBundleMeansNoResults() {
        when(httpClient.getJson(any(), any())).thenReturn(Optional.empty());

        assertThat(client(null).searchByNi(NI)).isEmpty();
    }

    private DhpImmunizationClient client(String identifierSystem) {
        return new DhpImmunizationClient(httpClient, DhpTestProperties.with("id", "secret", identifierSystem));
    }

    private JsonNode bundle(String nextUrl) {
        String links = nextUrl == null ? "[]" : "[{\"relation\":\"next\",\"url\":\"" + nextUrl + "\"}]";
        return jsonMapper.readTree("{\"resourceType\":\"Bundle\",\"link\":" + links + ",\"entry\":[]}");
    }

    private List<URI> requestedUris(int expectedCalls) {
        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);
        verify(httpClient, times(expectedCalls)).getJson(any(), captor.capture());
        return captor.getAllValues();
    }
}
