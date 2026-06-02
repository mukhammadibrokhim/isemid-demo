package uz.uzinfocom.app.platform.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(20)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        long startedAt = System.nanoTime();
        boolean failed = false;

        try {
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            failed = true;

            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;

            log.error(
                    "HTTP request failed. traceId={}, method={}, path={}, dispatcherType={}, status={}, durationMs={}, remoteAddr={}, message={}",
                    MDC.get(TraceContext.MDC_KEY),
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getDispatcherType(),
                    response.getStatus(),
                    durationMs,
                    request.getRemoteAddr(),
                    exception.getMessage(),
                    exception
            );

            throw exception;
        } finally {
            if (!failed) {
                long durationMs = (System.nanoTime() - startedAt) / 1_000_000;

                log.info(
                        "HTTP request completed. traceId={}, method={}, path={}, dispatcherType={}, status={}, durationMs={}, remoteAddr={}, message={}",
                        MDC.get(TraceContext.MDC_KEY),
                        request.getMethod(),
                        request.getRequestURI(),
                        request.getDispatcherType(),
                        response.getStatus(),
                        durationMs,
                        request.getRemoteAddr(),
                        "OK"
                );
            }
        }
    }
}
