package uz.uzinfocom.app.platform.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.observability.RequestLogErrorContext;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String TECHNICAL_MESSAGE =
            "Authentication is required or the supplied credentials are invalid";

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException authenticationException
    ) throws IOException {

        /*
         * Authentication exceptions raised inside the Spring Security filter chain
         * do not reach the MVC exception handler.
         *
         * Store the exception in the request context so RequestLoggingFilter
         * can produce the final HTTP request log with the same trace ID.
         */
        RequestLogErrorContext.attach(
                request,
                ErrorCode.UNAUTHORIZED.getCode(),
                TECHNICAL_MESSAGE,
                authenticationException
        );

        /*
         * Do not attempt to rewrite a response that has already been committed.
         * The attached error context remains available for the final request log.
         */
        if (response.isCommitted()) {
            return;
        }

        /*
         * ErrorResponseWriter must reuse the trace ID already assigned
         * to the request by TraceIdFilter.
         *
         * A new trace ID must never be generated in this handler.
         */
        errorResponseWriter.write(
                request,
                response,
                HttpStatus.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED
        );
    }
}