package uz.uzinfocom.app.integration.muxlisa.stt.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.muxlisa.common.exception.MuxlisaIntegrationException;
import uz.uzinfocom.app.integration.muxlisa.common.exception.MuxlisaRequestRejectedException;
import uz.uzinfocom.app.integration.muxlisa.common.properties.MuxlisaProperties;
import uz.uzinfocom.app.platform.resilience.CircuitBreakerNames;
import uz.uzinfocom.app.platform.resilience.TestCircuitBreakerLookups;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MuxlisaSttClientTest {

    private static final String URL = "https://muxlisa.example/api/v2/stt";
    private static final byte[] AUDIO = {1, 2, 3, 4};

    private MockRestServiceServer server;
    private MuxlisaSttClient client;
    private SystemSettingResolver settings;

    @BeforeEach
    void setUp() {
        client = clientWithKey("secret-key");
    }

    private MuxlisaSttClient clientWithKey(String apiKey) {
        settings = defaultsOnly();
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        MuxlisaProperties properties = new MuxlisaProperties(
                "https://muxlisa.example", "/api/v2/stt", apiKey,
                Duration.ofSeconds(1), Duration.ofSeconds(5), DataSize.ofMegabytes(5), 2);
        return new MuxlisaSttClient(
                builder.build(),
                properties,
                settings,
                TestCircuitBreakerLookups.withDefaults(CircuitBreakerNames.MUXLISA),
                JsonMapper.builder().build()
        );
    }

    /** A resolver with no overrides: every lookup answers with the caller's default. */
    private static SystemSettingResolver defaultsOnly() {
        SystemSettingResolver resolver = mock(SystemSettingResolver.class);
        when(resolver.resolveBoolean(anyString(), anyBoolean())).thenAnswer(call -> call.getArgument(1));
        when(resolver.resolveString(anyString(), anyString())).thenAnswer(call -> call.getArgument(1));
        when(resolver.resolveLong(anyString(), anyLong())).thenAnswer(call -> call.getArgument(1));
        return resolver;
    }

    @Test
    void usesBaseUrlAndEndpointFromRuntimeSettings() {
        when(settings.resolveString(org.mockito.ArgumentMatchers.eq("integration.muxlisa.base-url"), anyString()))
                .thenReturn("https://other.example");
        server.expect(once(), requestTo("https://other.example/api/v2/stt"))
                .andRespond(withSuccess("{\"text\":\"ok\"}", MediaType.APPLICATION_JSON));

        assertThat(client.transcribe(AUDIO, "audio/wav", "audio.wav")).isEqualTo("ok");
        server.verify();
    }

    @Test
    void refusesWithoutContactingMuxlisaWhenDisabled() {
        when(settings.resolveBoolean(org.mockito.ArgumentMatchers.eq("integration.muxlisa.enabled"), anyBoolean()))
                .thenReturn(false);

        assertThatThrownBy(() -> client.transcribe(AUDIO, "audio/wav", "audio.wav"))
                .isInstanceOf(MuxlisaIntegrationException.class)
                .hasMessage("muxlisa.error.disabled");
        server.verify();
    }

    @Test
    void sendsKeyAsMultipartAndReadsTopLevelText() {
        server.expect(once(), requestTo(URL))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("x-api-key", "secret-key"))
                .andExpect(header("Content-Type", org.hamcrest.Matchers.startsWith("multipart/form-data")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"audio\"")))
                .andRespond(withSuccess("{\"text\":\"salom dunyo\"}", MediaType.APPLICATION_JSON));

        assertThat(client.transcribe(AUDIO, "audio/wav", "audio.wav")).isEqualTo("salom dunyo");
        server.verify();
    }

    @Test
    void readsNestedResultText() {
        server.expect(once(), requestTo(URL))
                .andRespond(withSuccess("{\"status\":\"ok\",\"result\":{\"text\":\"nested\"}}", MediaType.APPLICATION_JSON));

        assertThat(client.transcribe(AUDIO, "audio/wav", "audio.wav")).isEqualTo("nested");
    }

    @Test
    void emptyTextIsNotAnError() {
        server.expect(once(), requestTo(URL))
                .andRespond(withSuccess("{\"text\":\"\"}", MediaType.APPLICATION_JSON));

        assertThat(client.transcribe(AUDIO, "audio/wav", "audio.wav")).isEmpty();
    }

    @Test
    void bodyWithoutTranscriptIsMalformed() {
        server.expect(once(), requestTo(URL))
                .andRespond(withSuccess("{\"foo\":1}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.transcribe(AUDIO, "audio/wav", "audio.wav"))
                .isInstanceOf(MuxlisaIntegrationException.class)
                .hasMessage("muxlisa.error.malformed_response");
    }

    @Test
    void badAudioIsClassifiedAsRejectedRequest() {
        server.expect(once(), requestTo(URL)).andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> client.transcribe(AUDIO, "audio/wav", "audio.wav"))
                .isInstanceOf(MuxlisaRequestRejectedException.class);
    }

    @Test
    void unrecognizedAudioTypeGetsASpecificMessage() {
        server.expect(once(), requestTo(URL)).andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"detail\":\"Invalid audio type2: video/webm. Supported types: {'audio/wav'}\"}"));

        assertThatThrownBy(() -> client.transcribe(AUDIO, "audio/webm", "audio.webm"))
                .isInstanceOf(MuxlisaRequestRejectedException.class)
                .hasMessage("muxlisa.error.invalid_audio_type");
    }

    @Test
    void refusedKeyIsReportedAsNotConfigured() {
        server.expect(once(), requestTo(URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.transcribe(AUDIO, "audio/wav", "audio.wav"))
                .isInstanceOf(MuxlisaIntegrationException.class)
                .hasMessage("muxlisa.error.not_configured");
    }

    @Test
    void exhaustedBalanceIsReportedDistinctly() {
        server.expect(once(), requestTo(URL)).andRespond(withStatus(HttpStatus.PAYMENT_REQUIRED));

        assertThatThrownBy(() -> client.transcribe(AUDIO, "audio/wav", "audio.wav"))
                .isInstanceOf(MuxlisaIntegrationException.class)
                .hasMessage("muxlisa.error.insufficient_balance");
    }

    @Test
    void serverErrorIsUpstreamFailure() {
        server.expect(once(), requestTo(URL)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> client.transcribe(AUDIO, "audio/wav", "audio.wav"))
                .isInstanceOf(MuxlisaIntegrationException.class)
                .hasMessage("muxlisa.error.unavailable");
    }

    @Test
    void missingKeyFailsBeforeAnyHttpCall() {
        MuxlisaSttClient keyless = clientWithKey(" ");

        assertThatThrownBy(() -> keyless.transcribe(AUDIO, "audio/wav", "audio.wav"))
                .isInstanceOf(MuxlisaIntegrationException.class)
                .hasMessage("muxlisa.error.not_configured");
        server.verify();
    }
}
