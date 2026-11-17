package uz.uzinfocom.app.integration.dhp.citizen.client;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.dhp.citizen.DhpCitizenTestData;
import uz.uzinfocom.app.integration.dhp.common.DhpTestProperties;
import uz.uzinfocom.app.integration.dhp.common.client.DhpHttpClient;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DhpCitizenClientTest {

    private final DhpHttpClient httpClient = mock(DhpHttpClient.class);
    private final DhpCitizenClient client =
            new DhpCitizenClient(httpClient, DhpTestProperties.configured(), DhpCitizenTestData.JSON);

    @Test
    void postsAFhirParametersBodyToPatientPopulate() {
        when(httpClient.postJson(any(), any(), any())).thenReturn(Optional.of(DhpCitizenTestData.patient()));

        Optional<JsonNode> patient = client.populate("NI", DhpCitizenTestData.NI);

        assertThat(patient).isPresent();

        ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<JsonNode> body = ArgumentCaptor.forClass(JsonNode.class);
        verify(httpClient).postJson(eq("DHP_CITIZEN_POPULATE"), uri.capture(), body.capture());

        assertThat(uri.getValue().toString()).isEqualTo(DhpTestProperties.FHIR_BASE + "/Patient/$populate");

        JsonNode parameters = body.getValue();
        assertThat(parameters.get("resourceType").asString()).isEqualTo("Parameters");
        assertThat(parameters.get("parameter")).hasSize(3);
        assertThat(parameters.get("parameter").get(0).get("name").asString()).isEqualTo("system");
        assertThat(parameters.get("parameter").get(0).get("valueUri").asString())
                .isEqualTo("http://terminology.hl7.org/CodeSystem/v2-0203");
        assertThat(parameters.get("parameter").get(1).get("name").asString()).isEqualTo("code");
        assertThat(parameters.get("parameter").get(1).get("valueCode").asString()).isEqualTo("NI");
        assertThat(parameters.get("parameter").get(2).get("name").asString()).isEqualTo("value");
        assertThat(parameters.get("parameter").get(2).get("valueString").asString()).isEqualTo(DhpCitizenTestData.NI);
    }

    @Test
    void anOperationOutcomeBodyMeansNothingFound() {
        when(httpClient.postJson(any(), any(), any())).thenReturn(Optional.of(
                DhpCitizenTestData.JSON.readTree("{\"resourceType\":\"OperationOutcome\"}")));

        assertThat(client.populate("NI", DhpCitizenTestData.NI)).isEmpty();
    }

    @Test
    void anEmptyResponseMeansNothingFound() {
        when(httpClient.postJson(any(), any(), any())).thenReturn(Optional.empty());

        assertThat(client.populate("PPN", "AD3131830")).isEmpty();
    }
}
