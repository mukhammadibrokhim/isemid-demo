package uz.uzinfocom.app.platform.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.app.shared.exception.ErrorCode;
import uz.uzinfocom.app.platform.observability.RequestLogErrorContext;
import uz.uzinfocom.app.platform.security.handler.JsonAccessDeniedHandler;
import uz.uzinfocom.app.platform.security.handler.JsonAuthenticationEntryPoint;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;

import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
@RequiredArgsConstructor
public class ErrorResponseFilter extends OncePerRequestFilter {

    private final ErrorResponseWriter errorResponseWriter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

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

            if (rootCause instanceof RejectedExecutionException
                    || rootCause instanceof TaskRejectedException) {
                writeServiceUnavailable(request, response, rootCause);
                return;
            }

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
        authenticationEntryPoint.commence(request, response, exception);
    }

    private void writeAccessDeniedError(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        accessDeniedHandler.handle(request, response, exception);
    }

    private void writeServiceUnavailable(
            HttpServletRequest request,
            HttpServletResponse response,
            Throwable exception
    ) throws IOException {
        RequestLogErrorContext.attach(
                request,
                ErrorCode.ASYNC_EXECUTOR_SATURATED.getCode(),
                "Application async executor rejected the submitted task",
                exception
        );
        errorResponseWriter.write(
                request,
                response,
                HttpStatus.SERVICE_UNAVAILABLE,
                ErrorCode.ASYNC_EXECUTOR_SATURATED
        );
    }

    private void writeInternalError(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) throws IOException {
        RequestLogErrorContext.attach(
                request,
                ErrorCode.INTERNAL_ERROR.getCode(),
                "Unhandled filter-chain exception",
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
}
