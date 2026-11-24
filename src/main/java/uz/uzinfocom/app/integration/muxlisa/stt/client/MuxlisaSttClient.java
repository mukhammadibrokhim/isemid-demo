package uz.uzinfocom.app.integration.muxlisa.stt.client;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.muxlisa.common.exception.MuxlisaCircuitOpenException;
import uz.uzinfocom.app.integration.muxlisa.common.exception.MuxlisaIntegrationException;
import uz.uzinfocom.app.integration.muxlisa.common.exception.MuxlisaRequestRejectedException;
import uz.uzinfocom.app.integration.muxlisa.common.properties.MuxlisaProperties;
import uz.uzinfocom.app.integration.muxlisa.common.properties.MuxlisaSettingKeys;
import uz.uzinfocom.app.platform.resilience.CircuitBreakerNames;
import uz.uzinfocom.app.platform.resilience.DynamicCircuitBreakerLookup;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * The one place Muxlisa speech-to-text calls go through: sends the audio as
 * {@code multipart/form-data} with the secret {@code x-api-key}, runs inside the
 * shared {@code muxlisa} circuit breaker and classifies every failure into a
 * {@link MuxlisaIntegrationException} subtype. The audio is never logged or stored.
 */
@Slf4j
@Component
public class MuxlisaSttClient {

    private static final String OPERATION = "muxlisa-stt";
    private static final String API_KEY_HEADER = "x-api-key";

    /** Where the transcript may sit in the JSON answer, tried in order. */
    private static final List<String> CONTAINERS = List.of("result", "data");
    private static final List<String> TEXT_FIELDS = List.of("text", "transcript", "transcription");

    private final RestClient restClient;
    private final MuxlisaProperties properties;
    private final SystemSettingResolver systemSettingResolver;
    private final DynamicCircuitBreakerLookup circuitBreakerLookup;
    private final JsonMapper jsonMapper;
    private final Semaphore concurrency;

    public MuxlisaSttClient(
            @Qualifier("muxlisaRestClient") RestClient restClient,
            MuxlisaProperties properties,
            SystemSettingResolver systemSettingResolver,
            DynamicCircuitBreakerLookup circuitBreakerLookup,
            JsonMapper jsonMapper
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.systemSettingResolver = systemSettingResolver;
        this.circuitBreakerLookup = circuitBreakerLookup;
        this.jsonMapper = jsonMapper;
        this.concurrency = new Semaphore(properties.maxConcurrentRequests());
    }

    /**
     * @param audio       the raw recording, already size-checked by the caller
     * @param contentType the audio MIME type without parameters, e.g. {@code audio/wav}
     * @param filename    file name Muxlisa sees; its extension should match the format
     * @return the recognized text, possibly empty for silence
     */
    public String transcribe(byte[] audio, String contentType, String filename) {
        if (!systemSettingResolver.resolveBoolean(MuxlisaSettingKeys.ENABLED, true)) {
            throw MuxlisaIntegrationException.disabled();
        }

        String apiKey = properties.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Muxlisa API key is not configured (integration.muxlisa.api-key / MUXLISA_API_KEY).");
            throw MuxlisaIntegrationException.notConfigured();
        }

        if (!concurrency.tryAcquire()) {
            throw MuxlisaIntegrationException.busy();
        }
        try {
            return circuitBreakerLookup.forName(CircuitBreakerNames.MUXLISA)
                    .executeSupplier(() -> send(audio, contentType, filename, apiKey));
        } catch (MuxlisaIntegrationException exception) {
            throw exception;
        } catch (CallNotPermittedException exception) {
            throw new MuxlisaCircuitOpenException(
                    circuitBreakerLookup.retryAfterSeconds(CircuitBreakerNames.MUXLISA), exception);
        } finally {
            concurrency.release();
        }
    }

    private String send(byte[] audio, String contentType, String filename, String apiKey) {
        // Plain multi-value map, not MultipartBodyBuilder: the builder drags in reactive-streams,
        // which this servlet-only app does not have on its classpath.
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.parseMediaType(contentType));
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", new HttpEntity<>(new NamedByteArrayResource(audio, filename), partHeaders));

        String url = systemSettingResolver.resolveString(MuxlisaSettingKeys.BASE_URL, properties.baseUrl())
                + systemSettingResolver.resolveString(MuxlisaSettingKeys.STT_ENDPOINT, properties.sttEndpoint());

        try {
            return restClient.post()
                    .uri(url)
                    .header(API_KEY_HEADER, apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .exchange((request, response) -> handle(response, contentType, audio.length));
        } catch (MuxlisaIntegrationException exception) {
            throw exception;
        } catch (RestClientException exception) {
            log.warn("Muxlisa request failed before a response was received. errorType={}",
                    exception.getClass().getSimpleName());
            throw MuxlisaIntegrationException.unavailable(exception);
        }
    }

    private String handle(ClientHttpResponse response, String contentType, int audioBytes) throws IOException {
        HttpStatusCode status = response.getStatusCode();
        int code = status.value();

        if (code == 400 || code == 413 || code == 415 || code == 422) {
            // Muxlisa's error text ("unsupported format", "too long", ...) is the only way to tell
            // why a recording was refused; it never contains the audio, so it is safe to log.
            String reason = readErrorText(response);
            log.warn("Muxlisa rejected the audio. status={}, contentType={}, audioBytes={}, reason={}",
                    code, contentType, audioBytes, reason);
            throw MuxlisaRequestRejectedException.fromReason(reason);
        }
        if (code == 402) {
            log.error("Muxlisa balance is exhausted - top it up in the Muxlisa cabinet.");
            throw MuxlisaIntegrationException.insufficientBalance();
        }
        if (code == 401 || code == 403) {
            log.warn("Muxlisa refused the API key (or the account has no balance). status={}", code);
            throw MuxlisaIntegrationException.notConfigured();
        }
        if (status.isError()) {
            log.warn("Muxlisa returned an error. status={}", code);
            throw MuxlisaIntegrationException.upstream();
        }

        return extractText(readBody(response));
    }

    private static String readErrorText(ClientHttpResponse response) {
        try (InputStream stream = response.getBody()) {
            String text = new String(stream.readNBytes(500), java.nio.charset.StandardCharsets.UTF_8);
            return text.isBlank() ? "<empty body>" : text;
        } catch (IOException exception) {
            return "<unreadable body>";
        }
    }

    private JsonNode readBody(ClientHttpResponse response) throws IOException {
        try (InputStream stream = response.getBody()) {
            byte[] bytes = stream.readAllBytes();
            if (bytes.length == 0) {
                throw MuxlisaIntegrationException.malformed();
            }
            return jsonMapper.readTree(bytes);
        } catch (JacksonException exception) {
            throw MuxlisaIntegrationException.malformed();
        }
    }

    /** Accepts {@code {"text":..}}, {@code {"result":{"text":..}}}, {@code {"data":{"text":..}}} or a plain-string {@code result}/{@code data}. */
    static String extractText(JsonNode root) {
        String direct = textField(root);
        if (direct != null) {
            return direct;
        }
        for (String container : CONTAINERS) {
            JsonNode node = root.path(container);
            if (node.isString()) {
                return node.asString();
            }
            String nested = textField(node);
            if (nested != null) {
                return nested;
            }
        }
        log.warn("Muxlisa answered 2xx but the body held no transcript field. fields={}", root.propertyNames());
        throw MuxlisaIntegrationException.malformed();
    }

    private static String textField(JsonNode node) {
        for (String field : TEXT_FIELDS) {
            JsonNode value = node.path(field);
            if (value.isString()) {
                return value.asString();
            }
        }
        return null;
    }

    /** A {@link ByteArrayResource} that reports a file name, which is what makes the part a file upload. */
    private static final class NamedByteArrayResource extends ByteArrayResource {

        private final String filename;

        NamedByteArrayResource(byte[] bytes, String filename) {
            super(bytes);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
