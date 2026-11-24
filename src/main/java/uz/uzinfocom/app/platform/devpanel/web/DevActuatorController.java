package uz.uzinfocom.app.platform.devpanel.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.web.WebEndpointHttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uz.uzinfocom.app.platform.devpanel.application.actuator.DevActuatorAccessPolicy;
import uz.uzinfocom.app.platform.devpanel.application.actuator.DevActuatorEndpointResponse;
import uz.uzinfocom.app.platform.devpanel.application.actuator.DevActuatorException;
import uz.uzinfocom.app.platform.devpanel.application.actuator.DevActuatorInvoker;
import uz.uzinfocom.app.platform.devpanel.application.actuator.DevActuatorResult;
import uz.uzinfocom.app.platform.devpanel.application.actuator.DevActuatorSecurityContext;
import uz.uzinfocom.app.platform.devpanel.application.actuator.DevActuatorStreamService;
import uz.uzinfocom.app.platform.devpanel.application.stream.DevSseBroadcaster;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Tag(
        name = "Dev Panel - Actuator",
        description = "Spring Boot Actuator behind the dev-panel login. Every endpoint in "
                + "management.endpoints.web.exposure.include (health, info, metrics, prometheus, loggers, env, "
                + "configprops, beans, conditions, mappings, scheduledtasks, threaddump, liquibase) is reachable "
                + "under /v1/dev/actuator/{id}[/{rest}] with the actuator's own sub-paths and query parameters "
                + "(e.g. /actuator/metrics/jvm.memory.used?tag=area:heap, /actuator/loggers/uz.uzinfocom.app). "
                + "GET /v1/dev/actuator lists what is available, the access tier each needs and the SSE path of "
                + "every readable operation.\n\n"
                + "Access: reads of health/info/metrics/prometheus/loggers/scheduledtasks/liquibase need any dev "
                + "account (ROLE_DEV_USER); env, configprops, beans, conditions, mappings, threaddump and every "
                + "write (POST/DELETE, e.g. changing a logger level) need ROLE_DEV_ADMIN. Property values in "
                + "env/configprops stay masked unless the caller is ROLE_DEV_SUPER_ADMIN; health details need any "
                + "dev account. heapdump, shutdown and logfile are never served.\n\n"
                + "Live data: GET /v1/dev/actuator-stream/{id}[/{rest}] re-reads the same operation every "
                + "intervalMs (default 3000, 1000-60000; at least 5000 for heavy endpoints such as threaddump) and "
                + "pushes an 'actuator' SSE event {endpoint, path, status, timestamp, body}; onlyChanges=true "
                + "suppresses repeats. A failing sample is reported as an 'actuator-error' event and the feed "
                + "continues. At most 20 feeds are open at once. Dashboard-wide live data (system, http, runtime, "
                + "health, error, logger) arrives on the single multiplexed GET /v1/dev/metrics/stream."
)
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevActuatorController {

    private static final Set<String> STREAM_CONTROL_PARAMS = Set.of("intervalMs", "onlyChanges");

    private final DevActuatorInvoker invoker;
    private final DevActuatorStreamService streamService;
    private final DevActuatorAccessPolicy accessPolicy;
    private final DevSseBroadcaster sseBroadcaster;
    private final MessageResolver messageResolver;

    @Operation(summary = "List the actuator endpoints reachable through the dev panel")
    @GetMapping(ApiPaths.Dev.ACTUATOR)
    public ApiResponse<List<DevActuatorEndpointResponse>> endpoints() {
        return ApiResponse.success(messageResolver.resolve("common.success"), invoker.describe());
    }

    @Operation(
            summary = "Read an actuator endpoint",
            description = "Forwards to the actuator operation with the same sub-path and query parameters. JSON "
                    + "results come back wrapped in the usual ApiResponse; text results (prometheus, or "
                    + "threaddump with 'Accept: text/plain') come back as the raw text."
    )
    @GetMapping({ApiPaths.Dev.ACTUATOR_ENDPOINT, ApiPaths.Dev.ACTUATOR_ENDPOINT_PATH})
    public ResponseEntity<?> read(
            @Parameter(description = "Actuator endpoint id, e.g. metrics.", required = true)
            @PathVariable String id,
            @Parameter(description = "Rest of the actuator path, e.g. jvm.memory.used.")
            @PathVariable(required = false) String rest,
            @RequestParam MultiValueMap<String, String> params,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) List<String> accept,
            Authentication authentication
    ) {
        return execute(id, rest, WebEndpointHttpMethod.GET, params, null, accept, authentication);
    }

    @Operation(
            summary = "Write to an actuator endpoint",
            description = "E.g. POST /actuator/loggers/uz.uzinfocom.app with {\"configuredLevel\":\"DEBUG\"} "
                    + "(null resets). A changed logger level is also pushed as a 'logger' event on the metrics stream."
    )
    @PostMapping({ApiPaths.Dev.ACTUATOR_ENDPOINT, ApiPaths.Dev.ACTUATOR_ENDPOINT_PATH})
    public ResponseEntity<?> write(
            @PathVariable String id,
            @PathVariable(required = false) String rest,
            @RequestParam MultiValueMap<String, String> params,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) List<String> accept,
            Authentication authentication
    ) {
        ResponseEntity<?> response = execute(id, rest, WebEndpointHttpMethod.POST, params, body, accept, authentication);
        log.info("Dev actuator write POST /{}{} by {}", id, rest == null ? "" : rest, authentication.getName());
        if ("loggers".equalsIgnoreCase(id) && rest != null && !rest.isBlank()) {
            sseBroadcaster.broadcast("logger", new LoggerChangedEvent(
                    rest.replaceAll("^/+", ""),
                    body == null ? null : body.get("configuredLevel"),
                    authentication.getName()
            ));
        }
        return response;
    }

    @Operation(summary = "Delete via an actuator endpoint")
    @DeleteMapping({ApiPaths.Dev.ACTUATOR_ENDPOINT, ApiPaths.Dev.ACTUATOR_ENDPOINT_PATH})
    public ResponseEntity<?> delete(
            @PathVariable String id,
            @PathVariable(required = false) String rest,
            @RequestParam MultiValueMap<String, String> params,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) List<String> accept,
            Authentication authentication
    ) {
        ResponseEntity<?> response = execute(id, rest, WebEndpointHttpMethod.DELETE, params, null, accept, authentication);
        log.info("Dev actuator write DELETE /{}{} by {}", id, rest == null ? "" : rest, authentication.getName());
        return response;
    }

    @Operation(
            summary = "Live feed of an actuator operation (SSE)",
            description = "Server-Sent Events feed that re-reads the same operation as GET /actuator/{id}[/{rest}] "
                    + "every intervalMs and pushes it as an 'actuator' event. Query parameters other than "
                    + "intervalMs/onlyChanges are forwarded to the operation (e.g. "
                    + "/actuator-stream/metrics/jvm.memory.used?tag=area:heap&intervalMs=2000). Lives until the "
                    + "client disconnects or an hour passes. Swagger UI's 'Try it out' cannot show it - connect "
                    + "with EventSource or curl -N."
    )
    @GetMapping({ApiPaths.Dev.ACTUATOR_STREAM_ENDPOINT, ApiPaths.Dev.ACTUATOR_STREAM_ENDPOINT_PATH})
    public SseEmitter stream(
            @PathVariable String id,
            @PathVariable(required = false) String rest,
            @RequestParam MultiValueMap<String, String> params,
            Authentication authentication
    ) {
        accessPolicy.assertAllowed(authentication, id.toLowerCase(Locale.ROOT), WebEndpointHttpMethod.GET);

        Long intervalMs = null;
        String rawInterval = params.getFirst("intervalMs");
        if (rawInterval != null && !rawInterval.isBlank()) {
            try {
                intervalMs = Long.parseLong(rawInterval.trim());
            } catch (NumberFormatException ex) {
                throw DevActuatorException.badRequest("intervalMs must be a number of milliseconds");
            }
        }
        boolean onlyChanges = Boolean.parseBoolean(params.getFirst("onlyChanges"));

        MultiValueMap<String, String> forwarded = new LinkedMultiValueMap<>();
        params.forEach((name, values) -> {
            if (!STREAM_CONTROL_PARAMS.contains(name)) {
                forwarded.put(name, values);
            }
        });

        return streamService.open(id, rest, forwarded, intervalMs, onlyChanges, DevActuatorSecurityContext.of(authentication));
    }

    private ResponseEntity<?> execute(
            String id,
            String rest,
            WebEndpointHttpMethod method,
            MultiValueMap<String, String> params,
            Map<String, String> body,
            List<String> accept,
            Authentication authentication
    ) {
        accessPolicy.assertAllowed(authentication, id.toLowerCase(Locale.ROOT), method);

        DevActuatorResult result = invoker.invoke(
                id, rest, method, params, body, accept, DevActuatorSecurityContext.of(authentication));

        if (result.isText()) {
            return ResponseEntity.ok().contentType(textType(result.contentType())).body(result.body());
        }
        return ResponseEntity.ok(ApiResponse.success(messageResolver.resolve("common.success"), result.body()));
    }

    private static MediaType textType(String contentType) {
        if (contentType == null) {
            return MediaType.TEXT_PLAIN;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (RuntimeException ex) {
            return MediaType.TEXT_PLAIN;
        }
    }

    /** Payload of the {@code logger} event on the multiplexed metrics stream. */
    public record LoggerChangedEvent(String name, String configuredLevel, String changedBy) {
    }
}
