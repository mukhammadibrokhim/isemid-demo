package uz.uzinfocom.app.integration.aircontrol.client;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.aircontrol.client.dto.AirControlMeasurement;
import uz.uzinfocom.app.integration.aircontrol.client.dto.AirControlReading;
import uz.uzinfocom.app.integration.aircontrol.client.dto.AirControlStation;
import uz.uzinfocom.app.integration.aircontrol.common.exception.AirControlCircuitOpenException;
import uz.uzinfocom.app.integration.aircontrol.common.exception.AirControlIntegrationException;
import uz.uzinfocom.app.integration.aircontrol.common.properties.AirControlProperties;
import uz.uzinfocom.app.integration.aircontrol.common.properties.AirControlSettingKeys;
import uz.uzinfocom.app.platform.resilience.CircuitBreakerNames;
import uz.uzinfocom.app.platform.resilience.DynamicCircuitBreakerLookup;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * The one place AIR_CONTROL calls go through: GET with HTTP basic auth, inside the
 * shared {@code air-control} circuit breaker, every failure classified into an
 * {@link AirControlIntegrationException}. The agreement's JSON is read tolerantly
 * ({@link JsonNode}, not bound DTOs) because its attribute table and its sample
 * disagree on types: ids/values may arrive as numbers or strings.
 */
@Slf4j
@Component
public class AirControlClient {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestClient restClient;
    private final AirControlProperties properties;
    private final SystemSettingResolver systemSettingResolver;
    private final DynamicCircuitBreakerLookup circuitBreakerLookup;
    private final JsonMapper jsonMapper;

    public AirControlClient(
            @Qualifier("airControlRestClient") RestClient restClient,
            AirControlProperties properties,
            SystemSettingResolver systemSettingResolver,
            DynamicCircuitBreakerLookup circuitBreakerLookup,
            JsonMapper jsonMapper
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.systemSettingResolver = systemSettingResolver;
        this.circuitBreakerLookup = circuitBreakerLookup;
        this.jsonMapper = jsonMapper;
    }

    /** Method 1: every monitoring station. Entries without an id are skipped. */
    public List<AirControlStation> fetchStations() {
        JsonNode root = get(path(AirControlSettingKeys.STATIONS_PATH, properties.stationsPath()))
                .orElseThrow(AirControlIntegrationException::malformed);
        if (!root.isArray()) {
            log.warn("AIR_CONTROL stations answer is not a JSON array. type={}", root.getNodeType());
            throw AirControlIntegrationException.malformed();
        }
        List<AirControlStation> stations = new ArrayList<>();
        for (JsonNode node : root) {
            String id = text(node.path("id"));
            if (id == null) {
                continue;
            }
            stations.add(new AirControlStation(
                    id,
                    text(node.path("name")),
                    decimal(node.path("latitude")),
                    decimal(node.path("longitude")),
                    text(node.path("soato_areaid")),
                    node.path("active").asBoolean(true)
            ));
        }
        return stations;
    }

    /**
     * Method 2: the latest measurement of one station; empty when AIR_CONTROL does not
     * know the station (404).
     */
    public Optional<AirControlMeasurement> fetchLatest(String stationId) {
        String path = path(AirControlSettingKeys.MEASUREMENT_PATH, properties.measurementPath());
        String url = path.endsWith("/") ? path + stationId : path + "/" + stationId;
        return get(url).map(root -> parseMeasurement(stationId, root));
    }

    private AirControlMeasurement parseMeasurement(String requestedId, JsonNode root) {
        JsonNode data = root.path("data");
        LocalDateTime measuredAt = dateTime(text(root.path("datetime")));
        if (measuredAt == null || !data.isArray()) {
            log.warn("AIR_CONTROL measurement has no usable datetime/data. stationId={}", requestedId);
            throw AirControlIntegrationException.malformed();
        }
        List<AirControlReading> readings = new ArrayList<>();
        for (JsonNode item : data) {
            String name = text(item.path("name"));
            BigDecimal value = decimal(item.path("value"));
            if (name == null || value == null) {
                continue;
            }
            readings.add(new AirControlReading(name, value, text(item.path("unit"))));
        }
        String stationId = text(root.path("stationId"));
        return new AirControlMeasurement(stationId != null ? stationId : requestedId, measuredAt, readings);
    }

    private String path(String settingKey, String defaultValue) {
        return systemSettingResolver.resolveString(AirControlSettingKeys.BASE_URL, properties.baseUrl())
                + systemSettingResolver.resolveString(settingKey, defaultValue);
    }

    /** @return the parsed body, or empty for 404 */
    private Optional<JsonNode> get(String url) {
        if (!systemSettingResolver.resolveBoolean(AirControlSettingKeys.ENABLED, true)) {
            throw AirControlIntegrationException.disabled();
        }
        String username = properties.username();
        String password = properties.password();
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.error("AIR_CONTROL credentials are not configured (integration.air-control.username/password).");
            throw AirControlIntegrationException.notConfigured();
        }
        String basic = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));

        try {
            return circuitBreakerLookup.forName(CircuitBreakerNames.AIR_CONTROL)
                    .executeSupplier(() -> send(url, basic));
        } catch (CallNotPermittedException exception) {
            throw new AirControlCircuitOpenException(
                    circuitBreakerLookup.retryAfterSeconds(CircuitBreakerNames.AIR_CONTROL), exception);
        }
    }

    private Optional<JsonNode> send(String url, String basic) {
        try {
            return restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange((request, response) -> handle(response));
        } catch (RestClientException exception) {
            log.warn("AIR_CONTROL request failed before a response was received. errorType={}",
                    exception.getClass().getSimpleName());
            throw AirControlIntegrationException.unavailable(exception);
        }
    }

    private Optional<JsonNode> handle(ClientHttpResponse response) throws IOException {
        HttpStatusCode status = response.getStatusCode();
        int code = status.value();
        if (code == 404) {
            return Optional.empty();
        }
        if (code == 401 || code == 403) {
            log.warn("AIR_CONTROL refused the credentials. status={}", code);
            throw AirControlIntegrationException.notConfigured();
        }
        if (status.isError()) {
            log.warn("AIR_CONTROL returned an error. status={}", code);
            throw AirControlIntegrationException.upstream();
        }
        try (InputStream stream = response.getBody()) {
            byte[] bytes = stream.readAllBytes();
            if (bytes.length == 0) {
                throw AirControlIntegrationException.malformed();
            }
            return Optional.of(jsonMapper.readTree(bytes));
        } catch (JacksonException exception) {
            throw AirControlIntegrationException.malformed();
        }
    }

    private static String text(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asString().trim();
        return value.isEmpty() ? null : value;
    }

    private static BigDecimal decimal(JsonNode node) {
        String value = text(node);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(',', '.'));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static LocalDateTime dateTime(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DATETIME);
        } catch (DateTimeParseException exception) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }
}
