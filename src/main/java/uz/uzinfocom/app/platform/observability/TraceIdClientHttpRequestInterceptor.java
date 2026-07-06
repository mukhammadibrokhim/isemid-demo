package uz.uzinfocom.app.platform.observability;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TraceIdClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TraceIdClientHttpRequestInterceptor.class);

    private final ObservabilityProperties properties;
    private final TraceIdProvider traceIdProvider;

    @Override
    public @NonNull ClientHttpResponse intercept(
            @NonNull HttpRequest request,
            byte @NonNull [] body,
            @NonNull ClientHttpRequestExecution execution
    ) throws IOException {
        String headerName = properties.getTraceIdHeader();
        String currentTraceId = traceIdProvider.currentTraceId();
        String explicitTraceId = request.getHeaders().getFirst(headerName);
        String traceId;

        if (StringUtils.hasText(currentTraceId)) {
            traceId = currentTraceId;
            if (traceIdProvider.isValid(explicitTraceId) && !traceId.equals(explicitTraceId)) {
                LOGGER.debug("Outbound trace header was replaced by the active trace context");
            }
        } else {
            String normalizedExplicitTraceId = traceIdProvider.normalize(explicitTraceId);
            traceId = normalizedExplicitTraceId != null
                    ? normalizedExplicitTraceId
                    : traceIdProvider.generateTraceId();
        }

        request.getHeaders().set(headerName, traceId);
        try (TraceContext.Scope ignored = TraceContext.open(traceId)) {
            return execution.execute(request, body);
        }
    }
}
