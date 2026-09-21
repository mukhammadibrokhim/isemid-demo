package uz.uzinfocom.app.integration.dhp.common.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.dhp.common.auth.DhpAccessTokenProvider;
import uz.uzinfocom.app.integration.dhp.common.exception.DhpAccessDeniedException;
import uz.uzinfocom.app.integration.dhp.common.exception.DhpIntegrationException;
import uz.uzinfocom.app.integration.dhp.common.exception.DhpRequestRejectedException;
import uz.uzinfocom.app.platform.resilience.CircuitBreakerNames;
import uz.uzinfocom.app.platform.resilience.TestCircuitBreakerLookups;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DhpHttpClientTest {

    private static final URI URL = URI.create("https://egov.dhp.example/mol/citizen/employment/by-ni?ni=30101900123456");

    private MockRestServiceServer server;
    private DhpAccessTokenProvider tokenProvider;
    private DhpHttpClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        tokenProvider = mock(DhpAccessTokenProvider.class);
        client = new DhpHttpClient(
                builder.build(),
                tokenProvider,
                TestCircuitBreakerLookups.withDefaults(CircuitBreakerNames.DHP),
                JsonMapper.builder().build()
        );
    }

    @Test
    void sendsBearerTokenAndParsesJsonBody() {
        when(tokenProvider.getAccessToken()).thenReturn("tok-1");
        server.expect(once(), requestTo(URL))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok-1"))
                .andRespond(withSuccess("{\"a\":1}", MediaType.APPLICATION_JSON));

        Optional<JsonNode> body = client.getJson("OP", URL);

        assertThat(body).isPresent();
        assertThat(body.get().get("a").asInt()).isEqualTo(1);
        server.verify();
    }

    @Test
    void parsesFhirJsonContentType() {
        when(tokenProvider.getAccessToken()).thenReturn("tok-1");
        server.expect(once(), requestTo(URL))
                .andRespond(withSuccess("{\"resourceType\":\"Bundle\"}", MediaType.valueOf("application/fhir+json")));

        assertThat(client.getJson("OP", URL).orElseThrow().get("resourceType").asString()).isEqualTo("Bundle");
    }

    @Test
    void notFoundAndEmptyBodyMeanNoData() {
        when(tokenProvider.getAccessToken()).thenReturn("tok-1");
        server.expect(once(), requestTo(URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(once(), requestTo(URL)).andRespond(withSuccess());

        assertThat(client.getJson("OP", URL)).isEmpty();
        assertThat(client.getJson("OP", URL)).isEmpty();
    }

    @Test
    void unauthorizedRefreshesTheTokenAndRetriesOnce() {
        when(tokenProvider.getAccessToken()).thenReturn("stale", "fresh");
        server.expect(once(), requestTo(URL))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer stale"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        server.expect(once(), requestTo(URL))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer fresh"))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        assertThat(client.getJson("OP", URL)).isPresent();

        verify(tokenProvider).invalidate("stale");
        server.verify();
    }

    @Test
    void unauthorizedTwiceIsAccessDenied() {
        when(tokenProvider.getAccessToken()).thenReturn("stale", "fresh");
        server.expect(once(), requestTo(URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        server.expect(once(), requestTo(URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.getJson("OP", URL)).isInstanceOf(DhpAccessDeniedException.class);
    }

    @Test
    void forbiddenIsAccessDeniedWithoutRetry() {
        when(tokenProvider.getAccessToken()).thenReturn("tok-1");
        server.expect(once(), requestTo(URL)).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.getJson("OP", URL))
                .isInstanceOf(DhpAccessDeniedException.class)
                .hasMessage("dhp.error.access_denied");
        server.verify();
    }

    @Test
    void badRequestIsRejected() {
        when(tokenProvider.getAccessToken()).thenReturn("tok-1");
        server.expect(once(), requestTo(URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"bad_request\",\"message\":\"incorrect NI\"}"));

        assertThatThrownBy(() -> client.getJson("OP", URL))
                .isInstanceOf(DhpRequestRejectedException.class)
                .hasMessage("dhp.error.rejected");
    }

    @Test
    void serverErrorIsAnUpstreamFailure() {
        when(tokenProvider.getAccessToken()).thenReturn("tok-1");
        server.expect(once(), requestTo(URL)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.getJson("OP", URL))
                .isExactlyInstanceOf(DhpIntegrationException.class)
                .hasMessage("dhp.error.unavailable");
    }

    @Test
    void nonJsonSuccessBodyIsMalformed() {
        when(tokenProvider.getAccessToken()).thenReturn("tok-1");
        server.expect(once(), requestTo(URL)).andRespond(withSuccess("<html>oops</html>", MediaType.TEXT_HTML));

        assertThatThrownBy(() -> client.getJson("OP", URL))
                .isExactlyInstanceOf(DhpIntegrationException.class)
                .hasMessage("dhp.error.malformed_response");
    }
}
