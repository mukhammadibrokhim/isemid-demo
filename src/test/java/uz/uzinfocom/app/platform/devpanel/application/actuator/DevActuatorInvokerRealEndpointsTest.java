package uz.uzinfocom.app.platform.devpanel.application.actuator;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.jackson.JacksonEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.logging.LoggersEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.management.ThreadDumpEndpointAutoConfiguration;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.boot.actuate.endpoint.jackson.EndpointJsonMapper;
import org.springframework.boot.actuate.endpoint.web.WebEndpointHttpMethod;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointAutoConfiguration;
import org.springframework.boot.health.autoconfigure.registry.HealthContributorRegistryAutoConfiguration;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsEndpointAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs {@link DevActuatorInvoker} against the REAL Spring Boot actuator endpoints (health, metrics,
 * threaddump, loggers, env) wired by their own auto-configurations - the part that cannot be faked,
 * because the whole point is that the dev panel behaves exactly like {@code /v1/actuator/**} would.
 */
class DevActuatorInvokerRealEndpointsTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    EndpointAutoConfiguration.class,
                    WebEndpointAutoConfiguration.class,
                    JacksonEndpointAutoConfiguration.class,
                    HealthContributorRegistryAutoConfiguration.class,
                    HealthEndpointAutoConfiguration.class,
                    MetricsEndpointAutoConfiguration.class,
                    ThreadDumpEndpointAutoConfiguration.class,
                    LoggersEndpointAutoConfiguration.class,
                    EnvironmentEndpointAutoConfiguration.class
            ))
            .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
            .withBean(LoggingSystem.class, () -> LoggingSystem.get(getClass().getClassLoader()))
            .withBean("testHealthIndicator", HealthIndicator.class, () -> () -> Health.up().withDetail("probe", "ok").build())
            .withPropertyValues(
                    "management.endpoints.web.exposure.include=health,metrics,threaddump,loggers,env",
                    "management.endpoint.health.show-details=when-authorized",
                    "management.endpoint.health.roles=DEV_MONITORING",
                    "management.endpoint.env.show-values=when-authorized",
                    "management.endpoint.env.roles=DEV_SUPER_ADMIN",
                    "dev.actuator.test.visible=plain-value"
            );

    private static SecurityContext caller(String... authorities) {
        return DevActuatorSecurityContext.of(
                UsernamePasswordAuthenticationToken.authenticated("tester", "n/a", AuthorityUtils.createAuthorityList(authorities)));
    }

    private static DevActuatorInvoker invokerFor(org.springframework.context.ApplicationContext context) {
        return new DevActuatorInvoker(
                context.getBeanProvider(WebEndpointsSupplier.class),
                context.getBeanProvider(EndpointJsonMapper.class),
                JsonMapper.builder().build(),
                new DevActuatorAccessPolicy()
        );
    }

    private void withInvoker(Consumer<DevActuatorInvoker> test) {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            test.accept(invokerFor(context));
        });
    }

    private static MultiValueMap<String, String> query(String name, String... values) {
        MultiValueMap<String, String> query = new LinkedMultiValueMap<>();
        query.addAll(name, List.of(values));
        return query;
    }

    @Test
    void listsEveryExposedEndpointWithBothThreadDumpFlavours() {
        withInvoker(invoker -> {
            List<DevActuatorEndpointResponse> endpoints = invoker.describe();

            assertThat(endpoints).extracting(DevActuatorEndpointResponse::id)
                    .containsExactly("env", "health", "loggers", "metrics", "threaddump");

            DevActuatorEndpointResponse threadDump = endpoints.stream()
                    .filter(endpoint -> endpoint.id().equals("threaddump")).findFirst().orElseThrow();
            assertThat(threadDump.sensitive()).isTrue();
            assertThat(threadDump.requiredAuthority()).isEqualTo(DevActuatorAccessPolicy.ROLE_ADMIN);
            assertThat(threadDump.operations()).hasSize(2)
                    .allSatisfy(operation -> {
                        assertThat(operation.method()).isEqualTo("GET");
                        assertThat(operation.path()).isEqualTo("/v1/dev/actuator/threaddump");
                        assertThat(operation.streamPath()).isEqualTo("/v1/dev/actuator-stream/threaddump");
                    });

            DevActuatorEndpointResponse loggers = endpoints.stream()
                    .filter(endpoint -> endpoint.id().equals("loggers")).findFirst().orElseThrow();
            assertThat(loggers.operations()).filteredOn(operation -> operation.method().equals("POST"))
                    .singleElement()
                    .satisfies(operation -> {
                        assertThat(operation.path()).isEqualTo("/v1/dev/actuator/loggers/{name}");
                        assertThat(operation.streamPath()).isNull();
                    });
        });
    }

    @Test
    void metricsListNamesAndResolveASingleMetricWithTagFilter() {
        runner.run(context -> {
            MeterRegistry registry = context.getBean(MeterRegistry.class);
            registry.counter("dev.actuator.test.counter", "region", "tashkent").increment(3);
            DevActuatorInvoker invoker = invokerFor(context);

            JsonNode names = (JsonNode) invoker.get("metrics", "", new LinkedMultiValueMap<>(), List.of(), caller("ROLE_DEV_USER")).body();
            assertThat(names.get("names").toString()).contains("dev.actuator.test.counter");

            JsonNode metric = (JsonNode) invoker.get("metrics", "dev.actuator.test.counter",
                    query("tag", "region:tashkent"), List.of(), caller("ROLE_DEV_USER")).body();
            assertThat(metric.get("name").asString()).isEqualTo("dev.actuator.test.counter");
            assertThat(metric.get("measurements").get(0).get("value").asDouble()).isEqualTo(3.0);

            assertThatThrownBy(() -> invoker.get("metrics", "no.such.metric", new LinkedMultiValueMap<>(), List.of(), caller("ROLE_DEV_USER")))
                    .isInstanceOf(NotFoundException.class);
        });
    }

    @Test
    void metricsAvailableTagsAreCountedAndFilterRejectsMalformedTag() {
        runner.run(context -> {
            context.getBean(MeterRegistry.class).counter("dev.actuator.test.tagged", "k", "v").increment();
            DevActuatorInvoker invoker = invokerFor(context);

            assertThatThrownBy(() -> invoker.get("metrics", "dev.actuator.test.tagged",
                    query("tag", "not-a-key-value"), List.of(), caller("ROLE_DEV_USER")))
                    .isInstanceOf(DevActuatorException.class);
        });
    }

    @Test
    void threadDumpIsJsonByDefaultAndPlainTextWhenAsked() {
        withInvoker(invoker -> {
            DevActuatorResult json = invoker.get("threaddump", "", new LinkedMultiValueMap<>(), List.of("*/*"), caller("ROLE_DEV_ADMIN"));
            assertThat(json.body()).isInstanceOf(JsonNode.class);
            assertThat(((JsonNode) json.body()).get("threads").size()).isPositive();

            DevActuatorResult text = invoker.get("threaddump", "", new LinkedMultiValueMap<>(), List.of("text/plain"), caller("ROLE_DEV_ADMIN"));
            assertThat(text.isText()).isTrue();
            assertThat((String) text.body()).contains("main");
        });
    }

    @Test
    void healthShowsComponentDetailsOnlyToADevPanelCaller() {
        withInvoker(invoker -> {
            JsonNode detailed = (JsonNode) invoker.get("health", "", new LinkedMultiValueMap<>(), List.of(),
                    DevActuatorSecurityContext.system()).body();
            assertThat(detailed.get("status").asString()).isEqualTo("UP");
            assertThat(detailed.get("components").get("test").get("details").get("probe").asString()).isEqualTo("ok");

            JsonNode anonymous = (JsonNode) invoker.get("health", "", new LinkedMultiValueMap<>(), List.of(),
                    SecurityContext.NONE).body();
            assertThat(anonymous.get("status").asString()).isEqualTo("UP");
            assertThat(anonymous.has("components")).isFalse();

            JsonNode component = (JsonNode) invoker.get("health", "test", new LinkedMultiValueMap<>(), List.of(),
                    DevActuatorSecurityContext.system()).body();
            assertThat(component.get("status").asString()).isEqualTo("UP");
        });
    }

    @Test
    void loggerLevelCanBeChangedAndReadBack() {
        withInvoker(invoker -> {
            String logger = "dev.actuator.test.logger";

            DevActuatorResult changed = invoker.invoke("loggers", "/" + logger, WebEndpointHttpMethod.POST,
                    new LinkedMultiValueMap<>(), Map.of("configuredLevel", "DEBUG"), List.of(), caller("ROLE_DEV_ADMIN"));
            assertThat(changed.status()).isEqualTo(204);

            JsonNode read = (JsonNode) invoker.get("loggers", logger, new LinkedMultiValueMap<>(), List.of(),
                    caller("ROLE_DEV_USER")).body();
            assertThat(read.get("configuredLevel").asString()).isEqualTo("DEBUG");

            invoker.invoke("loggers", logger, WebEndpointHttpMethod.POST, new LinkedMultiValueMap<>(),
                    new java.util.HashMap<>(java.util.Collections.singletonMap("configuredLevel", null)), List.of(),
                    caller("ROLE_DEV_ADMIN"));
        });
    }

    @Test
    void envValuesAreMaskedUnlessTheCallerIsDevSuperAdmin() {
        withInvoker(invoker -> {
            JsonNode asAdmin = (JsonNode) invoker.get("env", "dev.actuator.test.visible", new LinkedMultiValueMap<>(), List.of(),
                    caller("ROLE_DEV_USER", "ROLE_DEV_ADMIN")).body();
            assertThat(asAdmin.get("property").get("value").asString()).isEqualTo("******");

            JsonNode asSuperAdmin = (JsonNode) invoker.get("env", "dev.actuator.test.visible", new LinkedMultiValueMap<>(), List.of(),
                    caller("ROLE_DEV_USER", "ROLE_DEV_ADMIN", "ROLE_DEV_SUPER_ADMIN")).body();
            assertThat(asSuperAdmin.get("property").get("value").asString()).isEqualTo("plain-value");
        });
    }
}
