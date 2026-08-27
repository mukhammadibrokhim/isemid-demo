package uz.uzinfocom.app.integration.lis.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.integration.api2.common.auth.CurrentBearerTokenProvider;
import uz.uzinfocom.app.integration.lis.client.dto.LisActPushRequest;
import uz.uzinfocom.app.integration.lis.common.exception.LisUnavailableException;
import uz.uzinfocom.app.integration.lis.common.properties.LisProperties;
import uz.uzinfocom.app.integration.lis.common.support.LisErrorDecoder;
import uz.uzinfocom.app.integration.lis.common.support.LisUrlFactory;
import uz.uzinfocom.app.platform.resilience.CircuitBreakerNames;
import uz.uzinfocom.app.platform.resilience.TestCircuitBreakerLookups;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LisActClientCircuitBreakerTest {

    private static final String BASE_URL = "https://lis.example";

    private final LisProperties properties = new LisProperties(
            BASE_URL,
            "https://callback.example",
            "test-api-key",
            Duration.ofMillis(100),
            Duration.ofMillis(100),
            new LisProperties.Endpoints(
                    "/create-act/{labId}",
                    "/act-code",
                    "/sesorgs",
                    "/departments",
                    "/reference-dictionaries",
                    "/professions",
                    "/research-types",
                    "/categories",
                    "/item-types")
    );

    @Test
    void openCircuitBreakerFailsFastWithoutCallingLis() {
        CircuitBreakerRegistry openRegistry = CircuitBreakerRegistry.of(
                Map.of(CircuitBreakerNames.LIS, CircuitBreakerConfig.ofDefaults()));
        openRegistry.circuitBreaker(CircuitBreakerNames.LIS, CircuitBreakerNames.LIS).transitionToOpenState();

        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        // No expectation is registered: if the breaker failed to
        // short-circuit, MockRestServiceServer itself would fail this test
        // with an "unexpected request" error before this assertion runs.
        MockRestServiceServer.bindTo(builder).build();

        LisActClient client = new LisActClient(
                builder.build(),
                new LisUrlFactory(properties),
                new LisErrorDecoder(),
                new CurrentBearerTokenProvider(),
                TestCircuitBreakerLookups.withDefaults(openRegistry)
        );

        assertThatThrownBy(() -> client.createAct(
                1L, 100L, false, LisActPushRequest.builder().build(), UUID.randomUUID()))
                .isInstanceOf(LisUnavailableException.class);
    }
}
