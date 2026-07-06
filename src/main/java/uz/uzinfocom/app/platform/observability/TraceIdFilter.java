package uz.uzinfocom.app.platform.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TraceIdFilter extends OncePerRequestFilter {

    private final ObservabilityProperties properties;
    private final TraceIdProvider traceIdProvider;

    public TraceIdFilter(
            ObservabilityProperties properties,
            TraceIdProvider traceIdProvider
    ) {
        this.properties = properties;
        this.traceIdProvider = traceIdProvider;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String incomingCandidate = request.getDispatcherType() == jakarta.servlet.DispatcherType.REQUEST
                ? request.getHeader(properties.getTraceIdHeader())
                : null;
        String traceId = traceIdProvider.resolveOrCreate(request, incomingCandidate);
        response.setHeader(properties.getTraceIdHeader(), traceId);

        try (TraceContext.Scope ignored = TraceContext.open(traceId)) {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    protected void doFilterNestedErrorDispatch(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        doFilterInternal(request, response, filterChain);
    }
}
