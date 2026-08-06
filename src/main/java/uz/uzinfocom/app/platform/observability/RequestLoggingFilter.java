package uz.uzinfocom.app.platform.observability;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import uz.uzinfocom.app.platform.devmonitoring.application.DevErrorLogWriter;
import uz.uzinfocom.app.platform.devmonitoring.application.DevRequestLogWriter;
import uz.uzinfocom.app.platform.http.SensitiveLoggingSanitizer;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String HTTP_LOGGING_ENABLED_KEY = "app.observability.http-logging.enabled";

    private static final Logger HTTP_LOG = LoggerFactory.getLogger("HTTP_REQUEST");

    private final ObservabilityProperties properties;
    private final SensitiveLoggingSanitizer sanitizer;
    private final TraceIdProvider traceIdProvider;
    private final DevErrorLogWriter devErrorLogWriter;
    private final DevRequestLogWriter devRequestLogWriter;
    private final SystemSettingResolver systemSettingResolver;

    public RequestLoggingFilter(
            ObservabilityProperties properties,
            SensitiveLoggingSanitizer sanitizer,
            TraceIdProvider traceIdProvider,
            DevErrorLogWriter devErrorLogWriter,
            DevRequestLogWriter devRequestLogWriter,
            SystemSettingResolver systemSettingResolver
    ) {
        this.properties = properties;
        this.sanitizer = sanitizer;
        this.traceIdProvider = traceIdProvider;
        this.devErrorLogWriter = devErrorLogWriter;
        this.devRequestLogWriter = devRequestLogWriter;
        this.systemSettingResolver = systemSettingResolver;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        ObservabilityProperties.HttpLogging config = properties.getHttpLogging();
        boolean enabled = systemSettingResolver.resolveBoolean(HTTP_LOGGING_ENABLED_KEY, config.isEnabled());
        if (!enabled || request.getDispatcherType() != DispatcherType.REQUEST) {
            return true;
        }
        String path = request.getRequestURI();
        return path != null && config.getExcludedPathPrefixes().stream()
                .anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        long startedAtNanos = System.nanoTime();
        String traceId = traceIdProvider.getOrCreate(request);
        ErrorMessageCaptureResponse responseWrapper = new ErrorMessageCaptureResponse(response);
        Throwable failure = null;

        try {
            filterChain.doFilter(request, responseWrapper);
        } catch (ServletException | IOException | RuntimeException | Error exception) {
            failure = exception;
            throw exception;
        } finally {
            if (request.isAsyncStarted()) {
                registerAsyncListener(request, responseWrapper, traceId, startedAtNanos, failure);
            } else {
                logOnce(request, responseWrapper, traceId, startedAtNanos, failure, null);
            }
        }
    }

    private void registerAsyncListener(
            HttpServletRequest request,
            ErrorMessageCaptureResponse response,
            String traceId,
            long startedAtNanos,
            Throwable initialFailure
    ) {
        AtomicBoolean logged = new AtomicBoolean();
        AsyncListener listener = new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) {
                complete(null, null);
            }

            @Override
            public void onTimeout(AsyncEvent event) {
                complete(new TimeoutException("Asynchronous request timed out"), "timeout");
            }

            @Override
            public void onError(AsyncEvent event) {
                complete(event.getThrowable(), "error");
            }

            @Override
            public void onStartAsync(AsyncEvent event) {
                event.getAsyncContext().addListener(this);
            }

            private void complete(Throwable asyncFailure, String asyncOutcome) {
                if (!logged.compareAndSet(false, true)) {
                    return;
                }
                Throwable failure = asyncFailure != null ? asyncFailure : initialFailure;
                try (TraceContext.Scope ignored = TraceContext.open(traceId)) {
                    logOnce(request, response, traceId, startedAtNanos, failure, asyncOutcome);
                }
            }
        };

        try {
            request.getAsyncContext().addListener(listener);
        } catch (IllegalStateException registrationFailure) {
            if (logged.compareAndSet(false, true)) {
                logOnce(request, response, traceId, startedAtNanos,
                        initialFailure != null ? initialFailure : registrationFailure, "error");
            }
        }
    }

    private void logOnce(
            HttpServletRequest request,
            ErrorMessageCaptureResponse response,
            String traceId,
            long startedAtNanos,
            Throwable directFailure,
            String asyncOutcome
    ) {
        ObservabilityProperties.HttpLogging config = properties.getHttpLogging();
        long durationMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
        int status = response.getStatus();
        boolean slow = durationMs >= config.getSlowRequestThresholdMs();
        boolean success = status < 400 && directFailure == null && asyncOutcome == null;
        boolean serverFailure = status >= 500
                || directFailure instanceof Error
                || (directFailure != null && status < 400)
                || "error".equals(asyncOutcome)
                || "timeout".equals(asyncOutcome);

        persistRequestLog(request, response, traceId, durationMs, status, directFailure, asyncOutcome, config);

        if (success && !slow && !config.isLogSuccessfulRequests()) {
            return;
        }
        if (serverFailure) {
            if (!HTTP_LOG.isErrorEnabled()) {
                return;
            }
        } else if (status >= 400 || slow || directFailure != null) {
            if (!HTTP_LOG.isWarnEnabled()) {
                return;
            }
        } else if (!HTTP_LOG.isInfoEnabled()) {
            return;
        }

        try {
            RequestLogErrorContext.ErrorDetails details = RequestLogErrorContext.get(request).orElse(null);
            Throwable failure = details != null && details.throwable() != null
                    ? details.throwable()
                    : directFailure;
            Throwable rootCause = rootCause(failure);
            String message = details != null ? details.technicalMessage() : response.getErrorMessage();
            if (message == null && failure != null) {
                message = failure.getMessage();
            }

            StringBuilder event = new StringBuilder(512);
            append(event, "event", "http_request");
            append(event, "outcome", outcome(status, failure, asyncOutcome));
            append(event, "traceId", traceId);
            append(event, "method", request.getMethod());
            append(event, "route", sanitize(attribute(request), config.getMaxTextLength()));
            append(event, "path", sanitizer.sanitizePath(request.getRequestURI(), config.isMaskPathIdentifiers(), config.getMaxTextLength()));
            if (config.isIncludeQueryString()) {
                append(event, "query", sanitizer.sanitizeQuery(
                        request.getQueryString(), config.getSensitiveQueryParameters(), config.getMaxTextLength()));
            }
            append(event, "status", status);
            append(event, "durationMs", durationMs);
            append(event, "clientIp", sanitize(request.getRemoteAddr(), 64));
            String principal = RequestPrincipalContext.get(request).orElse(null);
            append(event, "principal", principal == null ? null : sanitize(principal, config.getMaxTextLength()));
            append(event, "organizationId", sanitize(request.getHeader(config.getOrganizationHeader()), config.getMaxTextLength()));
            append(event, "dispatcherType", request.getDispatcherType());
            append(event, "requestContentType", sanitize(request.getContentType(), 100));
            append(event, "responseContentType", sanitize(response.getContentType(), 100));
            append(event, "requestContentLength", request.getContentLengthLong());
            append(event, "responseCommitted", response.isCommitted());
            append(event, "errorCode", details == null ? null : sanitize(details.errorCode(), 100));
            append(event, "exceptionType", failure == null ? null : failure.getClass().getName());
            append(event, "rootCauseType", rootCause == null ? null : rootCause.getClass().getName());
            append(event, "message", sanitize(message, config.getMaxTextLength()));
            if (config.isIncludeUserAgent()) {
                append(event, "userAgent", sanitize(request.getHeader("User-Agent"), config.getMaxUserAgentLength()));
            }

            if (status >= 400) {
                devErrorLogWriter.record(
                        traceId,
                        details == null ? null : details.errorCode(),
                        status,
                        failure == null ? null : failure.getClass().getName(),
                        sanitizer.sanitizePath(request.getRequestURI(), config.isMaskPathIdentifiers(), config.getMaxTextLength()),
                        request.getMethod(),
                        principal,
                        message
                );
            }

            if (serverFailure) {
                if (failure != null) {
                    HTTP_LOG.error(event.toString(), failure);
                } else {
                    HTTP_LOG.error(event.toString());
                }
            } else if (status >= 400 || slow || failure != null) {
                HTTP_LOG.warn(event.toString());
            } else {
                HTTP_LOG.info(event.toString());
            }
        } catch (RuntimeException loggingFailure) {
            HTTP_LOG.warn(
                    "event=http_request_logging_failure traceId={} method={} status={} loggingErrorType={}",
                    traceId,
                    sanitize(request.getMethod(), 16),
                    status,
                    loggingFailure.getClass().getName()
            );
        }
    }

    /**
     * Persists one {@code dev_request_log} row per request, independent of
     * {@code config.isLogSuccessfulRequests()} and the HTTP_REQUEST logger's
     * level - those only govern what lands in the console/file log.
     * "Which resources did this user use" (see {@code GET /v1/dev/requests})
     * needs to stay complete regardless of that verbosity configuration.
     */
    private void persistRequestLog(
            HttpServletRequest request,
            ErrorMessageCaptureResponse response,
            String traceId,
            long durationMs,
            int status,
            Throwable directFailure,
            String asyncOutcome,
            ObservabilityProperties.HttpLogging config
    ) {
        try {
            RequestLogErrorContext.ErrorDetails details = RequestLogErrorContext.get(request).orElse(null);
            Throwable failure = details != null && details.throwable() != null
                    ? details.throwable()
                    : directFailure;
            Throwable rootCause = rootCause(failure);
            String message = details != null ? details.technicalMessage() : response.getErrorMessage();
            if (message == null && failure != null) {
                message = failure.getMessage();
            }
            String principal = RequestPrincipalContext.get(request).orElse(null);

            devRequestLogWriter.record(
                    traceId,
                    sanitize(request.getMethod(), 16),
                    sanitize(attribute(request), config.getMaxTextLength()),
                    sanitizer.sanitizePath(request.getRequestURI(), config.isMaskPathIdentifiers(), config.getMaxTextLength()),
                    sanitizer.sanitizeQuery(request.getQueryString(), config.getSensitiveQueryParameters(), config.getMaxTextLength()),
                    status,
                    outcome(status, failure, asyncOutcome),
                    durationMs,
                    sanitize(request.getRemoteAddr(), 64),
                    principal == null ? null : sanitize(principal, config.getMaxTextLength()),
                    sanitize(request.getHeader(config.getOrganizationHeader()), config.getMaxTextLength()),
                    sanitize(request.getContentType(), 100),
                    sanitize(response.getContentType(), 100),
                    request.getContentLengthLong(),
                    details == null ? null : sanitize(details.errorCode(), 100),
                    failure == null ? null : failure.getClass().getName(),
                    rootCause == null ? null : rootCause.getClass().getName(),
                    sanitize(message, config.getMaxTextLength()),
                    sanitize(request.getHeader("User-Agent"), config.getMaxUserAgentLength())
            );
        } catch (RuntimeException extractionFailure) {
            HTTP_LOG.warn(
                    "event=dev_request_log_extraction_failure traceId={} failureType={}",
                    traceId, extractionFailure.getClass().getName()
            );
        }
    }

    private String outcome(int status, Throwable failure, String asyncOutcome) {
        if (asyncOutcome != null) {
            return asyncOutcome;
        }
        if (failure != null || status >= 500) {
            return "error";
        }
        if (status >= 400) {
            return "rejected";
        }
        return "success";
    }

    private Throwable rootCause(Throwable failure) {
        if (failure == null) {
            return null;
        }
        Throwable current = failure;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private String attribute(HttpServletRequest request) {
        Object value = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return value == null ? null : value.toString();
    }

    private String sanitize(String value, int maxLength) {
        return sanitizer.sanitizeText(value, maxLength);
    }

    private void append(StringBuilder target, String name, Object value) {
        if (value == null) {
            return;
        }
        if (!target.isEmpty()) {
            target.append(' ');
        }
        target.append(name).append('=').append(value);
    }

    private static final class ErrorMessageCaptureResponse extends HttpServletResponseWrapper {

        private String errorMessage;

        private ErrorMessageCaptureResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void sendError(int statusCode, String message) throws IOException {
            errorMessage = message;
            super.sendError(statusCode, message);
        }

        @Override
        public void sendError(int statusCode) throws IOException {
            errorMessage = null;
            super.sendError(statusCode);
        }

        @Override
        public void reset() {
            errorMessage = null;
            super.reset();
        }

        private String getErrorMessage() {
            return errorMessage;
        }
    }
}
