package uz.uzinfocom.app.platform.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(20)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String ERROR_CODE_ATTR = RequestLoggingFilter.class.getName() + ".ERROR_CODE";
    public static final String ERROR_MESSAGE_ATTR = RequestLoggingFilter.class.getName() + ".ERROR_MESSAGE";

    private static final int MAX_MESSAGE_LENGTH = 500;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        long startedAt = System.nanoTime();
        StatusCaptureResponseWrapper wrappedResponse = new StatusCaptureResponseWrapper(response);

        Exception thrownException = null;

        try {
            filterChain.doFilter(request, wrappedResponse);
        } catch (Exception exception) {
            thrownException = exception;

            request.setAttribute(ERROR_MESSAGE_ATTR, extractExceptionMessage(exception));

            if (!wrappedResponse.isCommitted() && wrappedResponse.getStatus() < 400) {
                wrappedResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

            throw exception;
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            int status = resolveStatus(wrappedResponse, thrownException);
            String message = resolveMessage(request, wrappedResponse, status, thrownException);
            Object errorCode = request.getAttribute(ERROR_CODE_ATTR);

            logRequest(
                    request,
                    status,
                    durationMs,
                    errorCode,
                    message,
                    thrownException
            );
        }
    }

    private void logRequest(
            HttpServletRequest request,
            int status,
            long durationMs,
            Object errorCode,
            String message,
            Exception thrownException
    ) {
        String traceId = MDC.get(TraceContext.MDC_KEY);

        if (status >= 500) {
            if (thrownException != null) {
                log.error(
                        "HTTP request failed. traceId={}, method={}, path={}, query={}, dispatcherType={}, status={}, durationMs={}, remoteAddr={}, errorCode={}, message={}",
                        traceId,
                        request.getMethod(),
                        request.getRequestURI(),
                        request.getQueryString(),
                        request.getDispatcherType(),
                        status,
                        durationMs,
                        request.getRemoteAddr(),
                        errorCode,
                        message,
                        thrownException
                );
            } else {
                log.error(
                        "HTTP request failed. traceId={}, method={}, path={}, query={}, dispatcherType={}, status={}, durationMs={}, remoteAddr={}, errorCode={}, message={}",
                        traceId,
                        request.getMethod(),
                        request.getRequestURI(),
                        request.getQueryString(),
                        request.getDispatcherType(),
                        status,
                        durationMs,
                        request.getRemoteAddr(),
                        errorCode,
                        message
                );
            }

            return;
        }

        if (status >= 400) {
            log.warn(
                    "HTTP request rejected. traceId={}, method={}, path={}, query={}, dispatcherType={}, status={}, durationMs={}, remoteAddr={}, errorCode={}, message={}",
                    traceId,
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getQueryString(),
                    request.getDispatcherType(),
                    status,
                    durationMs,
                    request.getRemoteAddr(),
                    errorCode,
                    message
            );

            return;
        }

        log.info(
                "HTTP request completed. traceId={}, method={}, path={}, query={}, dispatcherType={}, status={}, durationMs={}, remoteAddr={}, message={}",
                traceId,
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getDispatcherType(),
                status,
                durationMs,
                request.getRemoteAddr(),
                message
        );
    }

    private int resolveStatus(
            StatusCaptureResponseWrapper response,
            Exception thrownException
    ) {
        int status = response.getStatus();

        if (thrownException == null) {
            return status;
        }

        if (status >= 400) {
            return status;
        }

        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(
            HttpServletRequest request,
            StatusCaptureResponseWrapper response,
            int status,
            Exception thrownException
    ) {
        Object attributeMessage = request.getAttribute(ERROR_MESSAGE_ATTR);

        if (attributeMessage != null) {
            return sanitize(attributeMessage.toString());
        }

        String responseErrorMessage = response.getErrorMessage();

        if (responseErrorMessage != null && !responseErrorMessage.isBlank()) {
            return sanitize(responseErrorMessage);
        }

        String exceptionMessage = extractExceptionMessage(thrownException);

        if (exceptionMessage != null && !exceptionMessage.isBlank()) {
            return sanitize(exceptionMessage);
        }

        HttpStatus httpStatus = HttpStatus.resolve(status);

        if (httpStatus != null) {
            if (status >= 400) {
                return httpStatus.getReasonPhrase();
            }

            return "OK";
        }

        return "HTTP " + status;
    }

    private String extractExceptionMessage(Exception exception) {
        if (exception == null) {
            return null;
        }

        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }

        Throwable cause = exception.getCause();

        while (cause != null) {
            if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
                return cause.getMessage();
            }

            cause = cause.getCause();
        }

        return exception.getClass().getSimpleName();
    }

    private String sanitize(String message) {
        if (message == null || message.isBlank()) {
            return "No message";
        }

        String sanitized = message
                .replace('\n', ' ')
                .replace('\r', ' ')
                .trim();

        if (sanitized.length() > MAX_MESSAGE_LENGTH) {
            return sanitized.substring(0, MAX_MESSAGE_LENGTH) + "...";
        }

        return sanitized;
    }

    private static final class StatusCaptureResponseWrapper extends HttpServletResponseWrapper {

        private int httpStatus = HttpServletResponse.SC_OK;
        private String errorMessage;

        private StatusCaptureResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            this.httpStatus = sc;
            super.setStatus(sc);
        }

        @Override
        public void sendError(int sc) throws IOException {
            this.httpStatus = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.httpStatus = sc;
            this.errorMessage = msg;
            super.sendError(sc, msg);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            this.httpStatus = HttpServletResponse.SC_FOUND;
            super.sendRedirect(location);
        }

        @Override
        public int getStatus() {
            return this.httpStatus;
        }

        private String getErrorMessage() {
            return errorMessage;
        }
    }
}