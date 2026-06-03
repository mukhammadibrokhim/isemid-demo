package uz.uzinfocom.app.platform.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.app.shared.exception.ErrorCode;
import uz.uzinfocom.app.platform.observability.TraceIdProvider;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class ErrorResponseFilter extends OncePerRequestFilter {

    private final ErrorResponseWriter errorResponseWriter;
    private final TraceIdProvider traceIdProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            if (response.isCommitted()) {
                rethrow(exception);
            }

            Throwable rootCause = rootCause(exception);

            if (rootCause instanceof AuthenticationException authenticationException) {
                writeAuthenticationError(request, response, authenticationException);
                return;
            }

            if (rootCause instanceof AccessDeniedException accessDeniedException) {
                writeAccessDeniedError(request, response, accessDeniedException);
                return;
            }

            writeInternalError(request, response, exception);
        }
    }

    private void writeAuthenticationError(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Authentication failed before MVC handling. traceId={}, method={}, path={}, remoteAddr={}, reason={}",
                traceId,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                exception.getMessage()
        );

        errorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
    }

    private void writeAccessDeniedError(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Access denied before MVC handling. traceId={}, method={}, path={}, remoteAddr={}, reason={}",
                traceId,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                exception.getMessage()
        );

        if (isMessageCode(exception.getMessage())) {
            errorResponseWriter.write(
                    request,
                    response,
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN,
                    exception.getMessage()
            );
            return;
        }

        errorResponseWriter.write(request, response, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
    }

    private void writeInternalError(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) throws IOException {
        String traceId = traceIdProvider.getTraceId(request);

        log.error(
                "Unhandled filter-chain exception. traceId={}, method={}, path={}, remoteAddr={}",
                traceId,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                exception
        );

        errorResponseWriter.write(request, response, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
    }

    private Throwable rootCause(Throwable exception) {
        Throwable current = exception;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private void rethrow(Exception exception) throws ServletException, IOException {
        if (exception instanceof IOException ioException) {
            throw ioException;
        }

        if (exception instanceof ServletException servletException) {
            throw servletException;
        }

        throw new ServletException(exception);
    }

    private boolean isMessageCode(String message) {
        return message != null && message.matches("[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+");
    }
}
