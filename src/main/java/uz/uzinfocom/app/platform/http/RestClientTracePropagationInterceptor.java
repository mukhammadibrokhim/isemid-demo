package uz.uzinfocom.app.platform.http;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.observability.ObservabilityProperties;
import uz.uzinfocom.app.platform.observability.TraceContext;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestClientTracePropagationInterceptor implements ClientHttpRequestInterceptor {

    private final ObservabilityProperties observabilityProperties;

    @Nonnull
    @Override
    public ClientHttpResponse intercept(
            @Nonnull HttpRequest request,
            @Nonnull byte[] body,
            @Nonnull ClientHttpRequestExecution execution
    ) throws IOException {

        String traceId = MDC.get(TraceContext.MDC_KEY);
        String headerName = observabilityProperties.getTraceIdHeader();

        if (StringUtils.hasText(traceId) && !request.getHeaders().containsHeader(headerName)) {
            request.getHeaders().set(headerName, traceId);
        }

        return execution.execute(request, body);
    }
}
