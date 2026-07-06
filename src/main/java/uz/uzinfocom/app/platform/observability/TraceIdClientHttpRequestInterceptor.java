package uz.uzinfocom.app.platform.observability;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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

    private final ObservabilityProperties properties;
    private final TraceIdProvider traceIdProvider;

    @Override
    public @NonNull ClientHttpResponse intercept(
            @NonNull HttpRequest request,
            byte @NonNull [] body,
            @NonNull ClientHttpRequestExecution execution
    ) throws IOException {
        String headerName = properties.getTraceIdHeader();
        String explicitTraceId = request.getHeaders().getFirst(headerName);
        String traceId;

        if (traceIdProvider.isValid(explicitTraceId)) {
            traceId = explicitTraceId;
        } else {
            traceId = traceIdProvider.currentTraceId();
            if (!StringUtils.hasText(traceId)) {
                traceId = traceIdProvider.generateTraceId();
            }
            request.getHeaders().set(headerName, traceId);
        }

        try (TraceContext.Scope ignored = TraceContext.open(traceId)) {
            return execution.execute(request, body);
        }
    }
}
