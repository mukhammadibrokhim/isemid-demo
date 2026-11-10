package uz.uzinfocom.app.platform.devpanel.application.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.InvalidEndpointRequestException;
import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointHttpMethod;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.boot.actuate.endpoint.web.WebOperationRequestPredicate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.shared.exception.ErrorCode;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Path matching, argument building and result mapping of {@link DevActuatorInvoker} with hand-built
 * operations, so each rule is pinned down in isolation (the real-endpoint behaviour is covered by
 * {@link DevActuatorInvokerRealEndpointsTest}).
 */
class DevActuatorInvokerTest {

    private static final SecurityContext CALLER = SecurityContext.NONE;

    private static WebOperation operation(String path, WebEndpointHttpMethod method,
                                          AtomicReference<InvocationContext> seen, Function<InvocationContext, Object> answer) {
        return operation(path, method, List.of(), seen, answer);
    }

    private static WebOperation operation(String path, WebEndpointHttpMethod method, List<String> produces,
                                          AtomicReference<InvocationContext> seen, Function<InvocationContext, Object> answer) {
        WebOperation operation = mock(WebOperation.class);
        when(operation.getRequestPredicate()).thenReturn(new WebOperationRequestPredicate(path, method, List.of(), produces));
        when(operation.invoke(any())).thenAnswer(call -> {
            InvocationContext context = call.getArgument(0);
            if (seen != null) {
                seen.set(context);
            }
            return answer.apply(context);
        });
        return operation;
    }

    private static ExposableWebEndpoint endpoint(String id, WebOperation... operations) {
        ExposableWebEndpoint endpoint = mock(ExposableWebEndpoint.class);
        when(endpoint.getEndpointId()).thenReturn(EndpointId.of(id));
        when(endpoint.getRootPath()).thenReturn(id);
        when(endpoint.getOperations()).thenReturn(List.of(operations));
        return endpoint;
    }

    private static DevActuatorInvoker invokerOf(ExposableWebEndpoint... endpoints) {
        WebEndpointsSupplier supplier = () -> List.of(endpoints);
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        beans.addBean("supplier", supplier);
        return new DevActuatorInvoker(
                beans.getBeanProvider(WebEndpointsSupplier.class),
                new StaticListableBeanFactory().getBeanProvider(org.springframework.boot.actuate.endpoint.jackson.EndpointJsonMapper.class),
                JsonMapper.builder().build(),
                new DevActuatorAccessPolicy()
        );
    }

    @Test
    void buildsArgumentsFromPathVariablesAndQueryParameters() {
        AtomicReference<InvocationContext> seen = new AtomicReference<>();
        DevActuatorInvoker invoker = invokerOf(endpoint("metrics",
                operation("metrics/{requiredMetricName}", WebEndpointHttpMethod.GET, seen, context -> Map.of("ok", true))));

        MultiValueMap<String, String> query = new LinkedMultiValueMap<>();
        query.add("tag", "area:heap");
        query.add("tag", "id:G1");
        query.add("single", "x");

        invoker.get("metrics", "/jvm.memory.used/", query, List.of(), CALLER);

        assertThat(seen.get().getArguments())
                .containsEntry("requiredMetricName", "jvm.memory.used")
                .containsEntry("tag", List.of("area:heap", "id:G1"))
                .containsEntry("single", "x");
    }

    @Test
    void matchAllRemainingVariableArrivesAsSegmentArray() {
        AtomicReference<InvocationContext> seen = new AtomicReference<>();
        DevActuatorInvoker invoker = invokerOf(endpoint("health",
                operation("health/{*path}", WebEndpointHttpMethod.GET, seen, context -> Map.of("status", "UP"))));

        invoker.get("health", "liveness/extra", new LinkedMultiValueMap<>(), List.of(), CALLER);

        assertThat(seen.get().getArguments().get("path")).isEqualTo(new String[]{"liveness", "extra"});
    }

    @Test
    void exactPathBeatsCatchAllWhenBothMatch() {
        AtomicReference<String> chosen = new AtomicReference<>();
        DevActuatorInvoker invoker = invokerOf(endpoint("health",
                operation("health/{*path}", WebEndpointHttpMethod.GET, null, context -> {
                    chosen.set("catch-all");
                    return Map.of();
                }),
                operation("health", WebEndpointHttpMethod.GET, null, context -> {
                    chosen.set("exact");
                    return Map.of();
                })));

        invoker.get("health", "", new LinkedMultiValueMap<>(), List.of(), CALLER);

        assertThat(chosen.get()).isEqualTo("exact");
    }

    @Test
    void postBodyIsMergedIntoArgumentsAndQueryWins() {
        AtomicReference<InvocationContext> seen = new AtomicReference<>();
        DevActuatorInvoker invoker = invokerOf(endpoint("loggers",
                operation("loggers/{name}", WebEndpointHttpMethod.POST, seen, context -> null)));

        MultiValueMap<String, String> query = new LinkedMultiValueMap<>();
        query.add("configuredLevel", "TRACE");

        DevActuatorResult result = invoker.invoke("loggers", "a.b", WebEndpointHttpMethod.POST, query,
                Map.of("configuredLevel", "DEBUG", "extra", "1"), List.of(), CALLER);

        assertThat(seen.get().getArguments())
                .containsEntry("name", "a.b")
                .containsEntry("extra", "1")
                .containsEntry("configuredLevel", "TRACE");
        assertThat(result.status()).isEqualTo(204);
    }

    @Test
    void emptyGetResultAndNotFoundResponseBecomeNotFound() {
        DevActuatorInvoker invoker = invokerOf(
                endpoint("metrics", operation("metrics/{n}", WebEndpointHttpMethod.GET, null, context -> null)),
                endpoint("loggers", operation("loggers/{n}", WebEndpointHttpMethod.GET, null,
                        context -> new WebEndpointResponse<>(WebEndpointResponse.STATUS_NOT_FOUND))));

        assertThatThrownBy(() -> invoker.get("metrics", "x", new LinkedMultiValueMap<>(), List.of(), CALLER))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> invoker.get("loggers", "x", new LinkedMultiValueMap<>(), List.of(), CALLER))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void downHealthStatusIsReturnedNotThrown() {
        DevActuatorInvoker invoker = invokerOf(endpoint("health", operation("health", WebEndpointHttpMethod.GET, null,
                context -> new WebEndpointResponse<>(Map.of("status", "DOWN"), WebEndpointResponse.STATUS_SERVICE_UNAVAILABLE))));

        DevActuatorResult result = invoker.get("health", "", new LinkedMultiValueMap<>(), List.of(), CALLER);

        assertThat(result.status()).isEqualTo(503);
        assertThat(result.body().toString()).contains("DOWN");
    }

    @Test
    void textAndByteBodiesComeBackAsStrings() {
        DevActuatorInvoker invoker = invokerOf(endpoint("prometheus", operation("prometheus", WebEndpointHttpMethod.GET, null,
                context -> new WebEndpointResponse<>("metric_total 1\n".getBytes(java.nio.charset.StandardCharsets.UTF_8)))));

        DevActuatorResult result = invoker.get("prometheus", "", new LinkedMultiValueMap<>(), List.of(), CALLER);

        assertThat(result.isText()).isTrue();
        assertThat(result.body()).isEqualTo("metric_total 1\n");
    }

    @Test
    void actuatorRejectionBecomesBadRequest() {
        DevActuatorInvoker invoker = invokerOf(endpoint("metrics", operation("metrics/{n}", WebEndpointHttpMethod.GET, null, context -> {
            throw new InvalidEndpointRequestException("bad tag", "Each tag parameter must be in the form key:value");
        })));

        assertThatThrownBy(() -> invoker.get("metrics", "x", new LinkedMultiValueMap<>(), List.of(), CALLER))
                .isInstanceOfSatisfying(DevActuatorException.class, ex -> {
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
                    assertThat(ex.getArgs()).containsExactly("Each tag parameter must be in the form key:value");
                });
    }

    @Test
    void unknownEndpointOrOperationIsNotFound() {
        DevActuatorInvoker invoker = invokerOf(endpoint("metrics",
                operation("metrics/{n}", WebEndpointHttpMethod.GET, null, context -> Map.of())));

        assertThatThrownBy(() -> invoker.get("nope", "", new LinkedMultiValueMap<>(), List.of(), CALLER))
                .isInstanceOf(NotFoundException.class);
        // metrics exists but has no operation at the bare path, and none for POST
        assertThatThrownBy(() -> invoker.get("metrics", "", new LinkedMultiValueMap<>(), List.of(), CALLER))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> invoker.invoke("metrics", "x", WebEndpointHttpMethod.POST,
                new LinkedMultiValueMap<>(), null, List.of(), CALLER))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void dangerousEndpointsAreRefusedEvenWhenTheActuatorExposesThem() {
        DevActuatorInvoker invoker = invokerOf(
                endpoint("heapdump", operation("heapdump", WebEndpointHttpMethod.GET, null, context -> Map.of())),
                endpoint("shutdown", operation("shutdown", WebEndpointHttpMethod.POST, null, context -> Map.of())),
                endpoint("health", operation("health", WebEndpointHttpMethod.GET, null, context -> Map.of())));

        assertThatThrownBy(() -> invoker.get("HeapDump", "", new LinkedMultiValueMap<>(), List.of(), CALLER))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> invoker.invoke("shutdown", "", WebEndpointHttpMethod.POST,
                new LinkedMultiValueMap<>(), null, List.of(), CALLER))
                .isInstanceOf(NotFoundException.class);
        assertThat(invoker.isAvailable("heapdump")).isFalse();
        assertThat(invoker.isAvailable("health")).isTrue();
        assertThat(invoker.describe()).extracting(DevActuatorEndpointResponse::id).containsExactly("health");
    }

    @Test
    void describeUsesEndpointIdEvenWhenRootPathIsRemapped() {
        WebOperation statsOperation = operation("stats/{requiredMetricName}", WebEndpointHttpMethod.GET, null, context -> Map.of());
        ExposableWebEndpoint remapped = mock(ExposableWebEndpoint.class);
        when(remapped.getEndpointId()).thenReturn(EndpointId.of("metrics"));
        when(remapped.getRootPath()).thenReturn("stats");
        when(remapped.getOperations()).thenReturn(List.of(statsOperation));
        DevActuatorInvoker invoker = invokerOf(remapped);

        DevActuatorEndpointResponse.Operation described = invoker.describe().get(0).operations().get(0);

        assertThat(described.path()).isEqualTo("/v1/dev/actuator/metrics/{requiredMetricName}");
        assertThat(described.streamPath()).isEqualTo("/v1/dev/actuator-stream/metrics/{requiredMetricName}");
        // and the remapped root still resolves when the panel addresses it by id
        assertThat(invoker.get("metrics", "jvm.memory.used", new LinkedMultiValueMap<>(), List.of(), CALLER).status()).isEqualTo(200);
    }
}
