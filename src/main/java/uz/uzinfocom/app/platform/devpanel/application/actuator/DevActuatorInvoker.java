package uz.uzinfocom.app.platform.devpanel.application.actuator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.InvalidEndpointRequestException;
import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.OperationArgumentResolver;
import org.springframework.boot.actuate.endpoint.ProducibleOperationArgumentResolver;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.boot.actuate.endpoint.jackson.EndpointJsonMapper;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointHttpMethod;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.boot.actuate.endpoint.web.WebOperationRequestPredicate;
import org.springframework.boot.actuate.endpoint.web.WebServerNamespace;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calls Spring Boot Actuator operations in-process, exactly the way
 * {@code AbstractWebMvcEndpointHandlerMapping} does for {@code /v1/actuator/**}: it looks the
 * operation up in the {@link WebEndpointsSupplier}, matches the request path against the
 * operation's path template, builds the argument map (path variables + POST body + query
 * parameters) and invokes it with an {@link InvocationContext}.
 *
 * <p>Going through the discovered web operations (instead of injecting each {@code *Endpoint}
 * bean) means every endpoint listed in {@code management.endpoints.web.exposure.include} - and
 * any added to that list later - shows up in the dev panel with no code change, and the
 * actuator's own parameter conversion, validation and {@code roles}-based masking keep working.
 */
@Component
@RequiredArgsConstructor
public class DevActuatorInvoker {

    /**
     * Never served by the dev panel even if someone adds them to the exposure list: a heap
     * dump is a copy of every secret in memory, {@code shutdown} stops the app, {@code logfile}
     * streams raw logs (the panel already has a file browser with its own access rules).
     */
    static final Set<String> BLOCKED_ENDPOINTS = Set.of("heapdump", "shutdown", "logfile", "jolokia");

    private final ObjectProvider<WebEndpointsSupplier> endpointsSupplier;
    private final ObjectProvider<EndpointJsonMapper> endpointJsonMapper;
    private final JsonMapper applicationJsonMapper;
    private final DevActuatorAccessPolicy accessPolicy;

    private final PathPatternParser patternParser = new PathPatternParser();
    private final Map<String, PathPattern> patterns = new ConcurrentHashMap<>();

    /** Every reachable endpoint with its operations, for the panel's endpoint picker. */
    public List<DevActuatorEndpointResponse> describe() {
        return endpoints().stream()
                .filter(endpoint -> !BLOCKED_ENDPOINTS.contains(endpointId(endpoint)))
                .sorted(Comparator.comparing(DevActuatorInvoker::endpointId))
                .map(this::describe)
                .toList();
    }

    private DevActuatorEndpointResponse describe(ExposableWebEndpoint endpoint) {
        String id = endpointId(endpoint);
        List<DevActuatorEndpointResponse.Operation> operations = endpoint.getOperations().stream()
                .map(operation -> describe(endpoint, id, operation.getRequestPredicate()))
                .sorted(Comparator.comparing(DevActuatorEndpointResponse.Operation::path)
                        .thenComparing(DevActuatorEndpointResponse.Operation::method))
                .toList();
        return new DevActuatorEndpointResponse(
                id,
                accessPolicy.requiredAuthority(id, WebEndpointHttpMethod.GET),
                accessPolicy.isSensitive(id),
                operations
        );
    }

    private DevActuatorEndpointResponse.Operation describe(
            ExposableWebEndpoint endpoint, String id, WebOperationRequestPredicate predicate
    ) {
        // The actuator path starts with the endpoint's root path, which management.endpoints.web.path-mapping
        // can rename; the panel always addresses an endpoint by its id.
        String path = predicate.getPath();
        String suffix = path.startsWith(endpoint.getRootPath()) ? path.substring(endpoint.getRootPath().length()) : "";
        boolean read = predicate.getHttpMethod() == WebEndpointHttpMethod.GET;
        return new DevActuatorEndpointResponse.Operation(
                predicate.getHttpMethod().name(),
                ApiPaths.Dev.ROOT + "/actuator/" + id + suffix,
                read ? ApiPaths.Dev.ROOT + "/actuator-stream/" + id + suffix : null,
                List.copyOf(predicate.getProduces())
        );
    }

    public boolean isAvailable(String endpointId) {
        String normalized = endpointId.toLowerCase(Locale.ROOT);
        return !BLOCKED_ENDPOINTS.contains(normalized)
                && endpoints().stream().anyMatch(endpoint -> endpointId(endpoint).equals(normalized));
    }

    public DevActuatorResult get(
            String endpointId,
            String subPath,
            MultiValueMap<String, String> query,
            List<String> accept,
            SecurityContext securityContext
    ) {
        return invoke(endpointId, subPath, WebEndpointHttpMethod.GET, query, null, accept, securityContext);
    }

    /**
     * @param subPath the part of the URL after the endpoint id (may be empty, leading slash optional)
     * @param body    JSON body of a POST (already parsed), or {@code null}
     * @param accept  the caller's {@code Accept} header values, used to choose between operations that
     *                differ only by content type (JSON vs plain-text thread dump) and to pick a
     *                {@code Producible} argument (prometheus text format)
     * @throws NotFoundException   no such endpoint/operation, or the operation answered "nothing here"
     * @throws DevActuatorException the actuator rejected the arguments
     */
    public DevActuatorResult invoke(
            String endpointId,
            String subPath,
            WebEndpointHttpMethod method,
            MultiValueMap<String, String> query,
            Map<String, String> body,
            List<String> accept,
            SecurityContext securityContext
    ) {
        ExposableWebEndpoint endpoint = findEndpoint(endpointId);
        String relative = trimSlashes(subPath);
        String requestPath = "/" + endpoint.getRootPath() + (relative.isEmpty() ? "" : "/" + relative);
        PathContainer container = PathContainer.parsePath(requestPath);

        List<Candidate> candidates = new ArrayList<>();
        for (WebOperation operation : endpoint.getOperations()) {
            WebOperationRequestPredicate predicate = operation.getRequestPredicate();
            if (predicate.getHttpMethod() != method) {
                continue;
            }
            PathPattern pattern = patterns.computeIfAbsent(predicate.getPath(), path -> patternParser.parse("/" + path));
            PathPattern.PathMatchInfo match = pattern.matchAndExtract(container);
            if (match != null) {
                candidates.add(new Candidate(operation, pattern, match));
            }
        }
        if (candidates.isEmpty()) {
            throw new NotFoundException("dev.actuator.operation-not-found", method.name(), requestPath);
        }

        Candidate chosen = choose(candidates, accept);
        InvocationContext context = new InvocationContext(
                securityContext,
                arguments(chosen, method, query, body),
                OperationArgumentResolver.of(WebServerNamespace.class, () -> WebServerNamespace.SERVER),
                producibleResolver(accept)
        );

        Object result;
        try {
            result = chosen.operation().invoke(context);
        } catch (InvalidEndpointRequestException ex) {
            throw DevActuatorException.badRequest(ex.getReason() != null ? ex.getReason() : ex.getMessage());
        }
        return toResult(method, result);
    }

    private Map<String, Object> arguments(
            Candidate chosen,
            WebEndpointHttpMethod method,
            MultiValueMap<String, String> query,
            Map<String, String> body
    ) {
        Map<String, Object> arguments = new LinkedHashMap<>(chosen.match().getUriVariables());
        String remainingVariable = chosen.operation().getRequestPredicate().getMatchAllRemainingPathSegmentsVariable();
        if (remainingVariable != null) {
            arguments.put(remainingVariable, segments(arguments.get(remainingVariable)));
        }
        if (body != null && method == WebEndpointHttpMethod.POST) {
            arguments.putAll(body);
        }
        if (query != null) {
            query.forEach((name, values) -> arguments.put(name, values.size() == 1 ? values.get(0) : List.copyOf(values)));
        }
        return arguments;
    }

    private static String[] segments(Object remaining) {
        if (remaining == null) {
            return new String[0];
        }
        return java.util.Arrays.stream(remaining.toString().split("/"))
                .filter(segment -> !segment.isEmpty())
                .toArray(String[]::new);
    }

    /**
     * Same as the stock resolver (honours {@code Accept}) but falls back to the producible's default
     * format instead of failing when the caller's {@code Accept} lists nothing the operation can produce -
     * e.g. a panel fetching {@code prometheus} with {@code Accept: application/json}.
     */
    private static OperationArgumentResolver producibleResolver(List<String> accept) {
        ProducibleOperationArgumentResolver negotiated = new ProducibleOperationArgumentResolver(() -> accept);
        ProducibleOperationArgumentResolver defaults = new ProducibleOperationArgumentResolver(List::of);
        return new OperationArgumentResolver() {
            @Override
            public boolean canResolve(Class<?> type) {
                return negotiated.canResolve(type);
            }

            @Override
            public <T> T resolve(Class<T> type) {
                T value = negotiated.resolve(type);
                return value != null ? value : defaults.resolve(type);
            }
        };
    }

    private Candidate choose(List<Candidate> candidates, List<String> accept) {
        candidates.sort(Comparator.comparing(Candidate::pattern, PathPattern.SPECIFICITY_COMPARATOR));
        String bestPattern = candidates.get(0).pattern().getPatternString();
        List<Candidate> best = candidates.stream()
                .filter(candidate -> candidate.pattern().getPatternString().equals(bestPattern))
                .toList();
        return best.size() == 1 ? best.get(0) : negotiate(best, accept);
    }

    /** Operations that share a path and differ by {@code produces} (threaddump JSON vs text). */
    private Candidate negotiate(List<Candidate> candidates, List<String> accept) {
        List<MimeType> wanted = parseAccept(accept);
        for (Candidate candidate : candidates) {
            for (String produced : candidate.operation().getRequestPredicate().getProduces()) {
                MimeType producedType = parseMimeType(produced);
                if (producedType != null && wanted.stream()
                        .anyMatch(type -> !type.isWildcardType() && type.isCompatibleWith(producedType))) {
                    return candidate;
                }
            }
        }
        return candidates.stream()
                .filter(candidate -> {
                    Collection<String> produces = candidate.operation().getRequestPredicate().getProduces();
                    return produces.isEmpty() || produces.stream().anyMatch(type -> type.contains("json"));
                })
                .findFirst()
                .orElse(candidates.get(0));
    }

    private static List<MimeType> parseAccept(List<String> accept) {
        if (accept == null || accept.isEmpty()) {
            return List.of();
        }
        try {
            return MimeTypeUtils.parseMimeTypes(String.join(",", accept));
        } catch (InvalidMimeTypeException ex) {
            return List.of();
        }
    }

    private static MimeType parseMimeType(String value) {
        try {
            return MimeTypeUtils.parseMimeType(value);
        } catch (InvalidMimeTypeException ex) {
            return null;
        }
    }

    private DevActuatorResult toResult(WebEndpointHttpMethod method, Object result) {
        if (result == null) {
            if (method == WebEndpointHttpMethod.GET) {
                throw new NotFoundException("dev.actuator.no-data");
            }
            return new DevActuatorResult(WebEndpointResponse.STATUS_NO_CONTENT, null, null);
        }

        int status = WebEndpointResponse.STATUS_OK;
        String contentType = null;
        Object body = result;
        if (result instanceof WebEndpointResponse<?> response) {
            status = response.getStatus();
            contentType = response.getContentType() != null ? response.getContentType().toString() : null;
            body = response.getBody();
        }

        if (status == WebEndpointResponse.STATUS_NOT_FOUND) {
            throw new NotFoundException("dev.actuator.no-data");
        }
        if (status == WebEndpointResponse.STATUS_BAD_REQUEST) {
            throw DevActuatorException.badRequest("rejected by the actuator");
        }
        return new DevActuatorResult(status, contentType, convert(body));
    }

    private Object convert(Object body) {
        return switch (body) {
            case null -> null;
            case String text -> text;
            case byte[] bytes -> new String(bytes, StandardCharsets.UTF_8);
            case CharSequence text -> text.toString();
            default -> jsonMapper().valueToTree(body);
        };
    }

    private JsonMapper jsonMapper() {
        EndpointJsonMapper endpointMapper = endpointJsonMapper.getIfAvailable();
        return endpointMapper != null ? endpointMapper.get() : applicationJsonMapper;
    }

    private ExposableWebEndpoint findEndpoint(String id) {
        String normalized = id.toLowerCase(Locale.ROOT);
        if (BLOCKED_ENDPOINTS.contains(normalized)) {
            throw new NotFoundException("dev.actuator.endpoint-not-found", id);
        }
        return endpoints().stream()
                .filter(endpoint -> endpointId(endpoint).equals(normalized))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("dev.actuator.endpoint-not-found", id));
    }

    private Collection<ExposableWebEndpoint> endpoints() {
        WebEndpointsSupplier supplier = endpointsSupplier.getIfAvailable();
        return supplier == null ? List.of() : supplier.getEndpoints();
    }

    private static String endpointId(ExposableWebEndpoint endpoint) {
        return endpoint.getEndpointId().toLowerCaseString();
    }

    private static String trimSlashes(String path) {
        if (path == null) {
            return "";
        }
        int start = 0;
        int end = path.length();
        while (start < end && path.charAt(start) == '/') {
            start++;
        }
        while (end > start && path.charAt(end - 1) == '/') {
            end--;
        }
        return path.substring(start, end);
    }

    private record Candidate(WebOperation operation, PathPattern pattern, PathPattern.PathMatchInfo match) {
    }
}
