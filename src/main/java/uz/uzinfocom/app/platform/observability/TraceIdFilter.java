package uz.uzinfocom.app.platform.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class TraceIdFilter extends OncePerRequestFilter {

    private static final Pattern SAFE_TRACE_ID =
            Pattern.compile("^[a-zA-Z0-9._\\-]{16,128}$");

    private final ObservabilityProperties properties;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String traceId = resolveTraceId(request);

        request.setAttribute(TraceContext.REQUEST_ATTRIBUTE, traceId);
        MDC.put(TraceContext.MDC_KEY, traceId);

        response.setHeader(properties.getTraceIdHeader(), traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            response.setHeader(properties.getTraceIdHeader(), traceId);
            MDC.remove(TraceContext.MDC_KEY);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String headerName = properties.getTraceIdHeader();

        String incoming = request.getHeader(headerName);

        if (StringUtils.hasText(incoming)) {
            String trimmed = incoming.trim();

            if (SAFE_TRACE_ID.matcher(trimmed).matches()) {
                return trimmed;
            }
        }

        return UUID.randomUUID().toString();
    }
}