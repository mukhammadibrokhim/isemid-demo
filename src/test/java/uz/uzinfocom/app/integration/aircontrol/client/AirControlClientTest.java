package uz.uzinfocom.app.integration.aircontrol.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.aircontrol.client.dto.AirControlMeasurement;
import uz.uzinfocom.app.integration.aircontrol.client.dto.AirControlStation;
import uz.uzinfocom.app.integration.aircontrol.common.exception.AirControlIntegrationException;
import uz.uzinfocom.app.integration.aircontrol.common.properties.AirControlProperties;
import uz.uzinfocom.app.platform.resilience.CircuitBreakerNames;
import uz.uzinfocom.app.platform.resilience.TestCircuitBreakerLookups;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AirControlClientTest {

    private static final String STATIONS_URL = "https://meteo.example/api/atmosphere/horiba/";
    private static final String STATION_URL = "https://meteo.example/api/atmosphere/horiba/7";
    private static final String BASIC = "Basic " + Base64.getEncoder().encodeToString("login:secret".getBytes());

    private MockRestServiceServer server;
    private SystemSettingResolver settings;
    private AirControlClient client;

    @BeforeEach
    void setUp() {
        client = clientWith("login", "secret");
    }

    private AirControlClient clientWith(String username, String password) {
        settings = mock(SystemSettingResolver.class);
        when(settings.resolveBoolean(anyString(), anyBoolean())).thenAnswer(call -> call.getArgument(1));
        when(settings.resolveString(anyString(), anyString())).thenAnswer(call -> call.getArgument(1));
        when(settings.resolveLong(anyString(), anyLong())).thenAnswer(call -> call.getArgument(1));

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        AirControlProperties properties = new AirControlProperties(
                "https://meteo.example", "/api/atmosphere/horiba/", "/api/atmosphere/horiba/",
                username, password, Duration.ofSeconds(1), Duration.ofSeconds(5));
        return new AirControlClient(
                builder.build(), properties, settings,
                TestCircuitBreakerLookups.withDefaults(CircuitBreakerNames.AIRCONTROL),
                JsonMapper.builder().build());
    }

    @Test
    void readsStationsAndToleratesNumbersOrStringsAndMissingCoordinates() {
        server.expect(once(), requestTo(STATIONS_URL))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("Authorization", BASIC))
                .andRespond(withSuccess("""
                        [
                          {"id": 1, "name": "Uzhyromet", "latitude": 41.327961, "longitude": 69.294475,
                           "soato_areaid": 1726266, "active": true},
                          {"id": "2", "name": "No coords", "soato_areaid": "1703", "active": false},
                          {"name": "no id - skipped"}
                        ]
                        """, MediaType.APPLICATION_JSON));

        List<AirControlStation> stations = client.fetchStations();

        assertThat(stations).hasSize(2);
        AirControlStation first = stations.get(0);
        assertThat(first.id()).isEqualTo("1");
        assertThat(first.name()).isEqualTo("Uzhyromet");
        assertThat(first.latitude()).isEqualByComparingTo("41.327961");
        assertThat(first.soatoAreaId()).isEqualTo("1726266");
        assertThat(first.active()).isTrue();
        AirControlStation second = stations.get(1);
        assertThat(second.id()).isEqualTo("2");
        assertThat(second.latitude()).isNull();
        assertThat(second.soatoAreaId()).isEqualTo("1703");
        assertThat(second.active()).isFalse();
        server.verify();
    }

    @Test
    void readsLatestMeasurementFromTheDocumentedSample() {
        server.expect(once(), requestTo(STATION_URL))
                .andRespond(withSuccess("""
                        {"stationId": 7, "datetime": "2025-04-17 11:00:00",
                         "data": [
                           {"name": "PM 2.5", "value": 25.884, "unit": "µg/m³"},
                           {"name": "CO", "value": "0.433", "unit": "mg/m³"},
                           {"name": "broken", "value": "n/a", "unit": "mg/m³"}
                         ]}
                        """, MediaType.APPLICATION_JSON));

        Optional<AirControlMeasurement> latest = client.fetchLatest("7");

        assertThat(latest).isPresent();
        AirControlMeasurement measurement = latest.get();
        assertThat(measurement.stationId()).isEqualTo("7");
        assertThat(measurement.measuredAt()).isEqualTo(LocalDateTime.of(2025, 4, 17, 11, 0, 0));
        assertThat(measurement.readings()).hasSize(2);
        assertThat(measurement.readings().get(0).name()).isEqualTo("PM 2.5");
        assertThat(measurement.readings().get(0).value()).isEqualByComparingTo(new BigDecimal("25.884"));
        assertThat(measurement.readings().get(1).value()).isEqualByComparingTo("0.433");
        server.verify();
    }

    @Test
    void unknownStationIsAnEmptyResultNotAnError() {
        server.expect(once(), requestTo(STATION_URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.fetchLatest("7")).isEmpty();
        server.verify();
    }

    @Test
    void measurementWithoutUsableDatetimeIsMalformed() {
        server.expect(once(), requestTo(STATION_URL))
                .andRespond(withSuccess("{\"stationId\":7,\"datetime\":\"garbage\",\"data\":[]}",
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchLatest("7"))
                .isInstanceOf(AirControlIntegrationException.class)
                .hasMessage("aircontrol.error.malformed_response");
    }

    @Test
    void refusedCredentialsAreReportedAsNotConfigured() {
        server.expect(once(), requestTo(STATIONS_URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.fetchStations())
                .isInstanceOf(AirControlIntegrationException.class)
                .hasMessage("aircontrol.error.not_configured");
    }

    @Test
    void serverErrorIsAnUpstreamFailure() {
        server.expect(once(), requestTo(STATIONS_URL)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.fetchStations())
                .isInstanceOf(AirControlIntegrationException.class)
                .hasMessage("aircontrol.error.unavailable");
    }

    @Test
    void missingCredentialsFailBeforeAnyRequestIsSent() {
        AirControlClient unconfigured = clientWith("", null);

        assertThatThrownBy(unconfigured::fetchStations)
                .isInstanceOf(AirControlIntegrationException.class)
                .hasMessage("aircontrol.error.not_configured");
        server.verify();
    }

    @Test
    void refusesWithoutContactingAirControlWhenDisabled() {
        when(settings.resolveBoolean(eq("integration.aircontrol.enabled"), anyBoolean())).thenReturn(false);

        assertThatThrownBy(() -> client.fetchStations())
                .isInstanceOf(AirControlIntegrationException.class)
                .hasMessage("aircontrol.error.disabled");
        server.verify();
    }
}
