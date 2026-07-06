package uz.uzinfocom.app.platform.http;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.observability.ObservabilityProperties;
import uz.uzinfocom.app.platform.observability.TraceContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class RestClientLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger OUTBOUND_LOG = LoggerFactory.getLogger("OUTBOUND_HTTP");

    private final SensitiveLoggingSanitizer sanitizer;
    private final ObservabilityProperties properties;

    @Override
    public @NonNull ClientHttpResponse intercept(
            @NonNull HttpRequest request,
            byte @NonNull [] body,
            @NonNull ClientHttpRequestExecution execution
    ) throws IOException {
        ObservabilityProperties.OutboundHttpLogging config = properties.getOutboundHttpLogging();
        if (!config.isEnabled()) {
            return execution.execute(request, body);
        }

        long startedAtNanos = System.nanoTime();
        try {
            ClientHttpResponse response = execution.execute(request, body);
            ResponseCapture capture = captureResponseBody(response, config);
            long durationMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
            int status = response.getStatusCode().value();
            logCompletion(request, body, response, capture, status, durationMs, config);
            return capture.response();
        } catch (IOException | RuntimeException exception) {
            long durationMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
            logTransportFailure(request, body.length, durationMs, exception, config);
            throw exception;
        }
    }

    private ResponseCapture captureResponseBody(
            ClientHttpResponse response,
            ObservabilityProperties.OutboundHttpLogging config
    ) throws IOException {
        if (!config.isLogResponseBody()) {
            return new ResponseCapture(response, null, false, null);
        }

        HttpHeaders headers = response.getHeaders();
        long contentLength = headers.getContentLength();
        MediaType contentType = headers.getContentType();
        String skipReason = responseBodySkipReason(headers, contentType, contentLength, config);
        if (skipReason != null) {
            return new ResponseCapture(response, null, true, skipReason);
        }

        byte[] responseBody = response.getBody().readNBytes((int) contentLength);
        return new ResponseCapture(
                new BufferedClientHttpResponse(response, responseBody),
                responseBody,
                false,
                null
        );
    }

    private String responseBodySkipReason(
            HttpHeaders headers,
            MediaType contentType,
            long contentLength,
            ObservabilityProperties.OutboundHttpLogging config
    ) {
        if (contentLength < 0) {
            return "unknown_content_length";
        }
        if (contentLength > config.getMaxBodyBytes()) {
            return "content_length_exceeds_limit";
        }
        if (!sanitizer.isAllowedTextContentType(contentType, config.getAllowedTextContentTypes())) {
            return "content_type_not_allowed";
        }
        String disposition = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        if (disposition != null && disposition.toLowerCase(Locale.ROOT).contains("attachment")) {
            return "file_download";
        }
        String transferEncoding = headers.getFirst(HttpHeaders.TRANSFER_ENCODING);
        if (transferEncoding != null && transferEncoding.toLowerCase(Locale.ROOT).contains("chunked")) {
            return "streaming_response";
        }
        return null;
    }

    private void logCompletion(
            HttpRequest request,
            byte[] requestBody,
            ClientHttpResponse response,
            ResponseCapture capture,
            int status,
            long durationMs,
            ObservabilityProperties.OutboundHttpLogging config
    ) {
        boolean slow = durationMs >= config.getSlowRequestThresholdMs();
        LogLevel level;
        if (status >= 500) {
            level = LogLevel.ERROR;
        } else if (status >= 400 || slow) {
            level = LogLevel.WARN;
        } else if (config.isLogSuccessfulRequests()) {
            level = LogLevel.INFO;
        } else {
            level = LogLevel.DEBUG;
        }
        if (!isEnabled(level)) {
            return;
        }

        try {
            StringBuilder event = baseEvent(request, durationMs);
            append(event, "outcome", status >= 400 ? "error" : "success");
            append(event, "status", status);
            append(event, "requestContentType", contentType(request.getHeaders()));
            append(event, "responseContentType", contentType(response.getHeaders()));
            append(event, "requestContentLength", requestBody.length);
            append(event, "responseContentLength", response.getHeaders().getContentLength());

            if (config.isIncludeHeaders()) {
                append(event, "requestHeaders", sanitizer.sanitizeHeaders(request.getHeaders()));
                append(event, "responseHeaders", sanitizer.sanitizeHeaders(response.getHeaders()));
            }
            appendRequestBody(event, requestBody, request.getHeaders(), config);
            if (config.isLogResponseBody()) {
                append(event, "bodySkipped", capture.bodySkipped());
                if (capture.bodySkipped()) {
                    append(event, "bodySkipReason", capture.bodySkipReason());
                    append(event, "responseBodySkipped", true);
                    append(event, "responseBodySkipReason", capture.bodySkipReason());
                } else {
                    append(event, "responseBody", sanitizer.sanitizeBody(
                            capture.body(), response.getHeaders(), config.getMaxBodyBytes()));
                }
            }
            write(level, event.toString());
        } catch (RuntimeException loggingFailure) {
            logFallback(request, loggingFailure);
        }
    }

    private void appendRequestBody(
            StringBuilder event,
            byte[] body,
            HttpHeaders headers,
            ObservabilityProperties.OutboundHttpLogging config
    ) {
        if (!config.isLogRequestBody()) {
            return;
        }
        if (body.length > config.getMaxBodyBytes()) {
            append(event, "requestBodySkipped", true);
            append(event, "requestBodySkipReason", "body_exceeds_limit");
            return;
        }
        if (!sanitizer.isAllowedTextContentType(headers.getContentType(), config.getAllowedTextContentTypes())) {
            append(event, "requestBodySkipped", true);
            append(event, "requestBodySkipReason", "content_type_not_allowed");
            return;
        }
        append(event, "requestBody", sanitizer.sanitizeBody(body, headers, config.getMaxBodyBytes()));
    }

    private void logTransportFailure(
            HttpRequest request,
            long requestContentLength,
            long durationMs,
            Exception exception,
            ObservabilityProperties.OutboundHttpLogging config
    ) {
        if (!OUTBOUND_LOG.isErrorEnabled()) {
            return;
        }
        try {
            StringBuilder event = baseEvent(request, durationMs);
            append(event, "outcome", "transport_error");
            append(event, "requestContentType", contentType(request.getHeaders()));
            append(event, "requestContentLength", requestContentLength);
            append(event, "errorType", exception.getClass().getName());
            append(event, "errorMessage", sanitizer.sanitizeText(exception.getMessage(), config.getMaxBodyBytes()));
            OUTBOUND_LOG.error(event.toString(), exception);
        } catch (RuntimeException loggingFailure) {
            logFallback(request, loggingFailure);
        }
    }

    private StringBuilder baseEvent(HttpRequest request, long durationMs) {
        ObservabilityProperties.HttpLogging inboundConfig = properties.getHttpLogging();
        StringBuilder event = new StringBuilder(384);
        append(event, "event", "outbound_http");
        append(event, "traceId", TraceContext.currentTraceId());
        append(event, "method", request.getMethod());
        append(event, "sanitizedUri", sanitizer.sanitizeUri(
                request.getURI(), inboundConfig.getSensitiveQueryParameters(), inboundConfig.getMaxTextLength()));
        append(event, "targetHost", sanitizer.sanitizeText(request.getURI().getHost(), 255));
        append(event, "durationMs", durationMs);
        return event;
    }

    private void logFallback(HttpRequest request, RuntimeException loggingFailure) {
        String safePath;
        try {
            safePath = sanitizer.sanitizePath(request.getURI().getRawPath(), true, 300);
        } catch (RuntimeException ignored) {
            safePath = "[unavailable]";
        }
        OUTBOUND_LOG.warn(
                "event=outbound_http_logging_failure traceId={} method={} targetHost={} path={} loggingErrorType={}",
                TraceContext.currentTraceId(),
                request.getMethod(),
                sanitizer.sanitizeText(request.getURI().getHost(), 255),
                safePath,
                loggingFailure.getClass().getName()
        );
    }

    private boolean isEnabled(LogLevel level) {
        return switch (level) {
            case DEBUG -> OUTBOUND_LOG.isDebugEnabled();
            case INFO -> OUTBOUND_LOG.isInfoEnabled();
            case WARN -> OUTBOUND_LOG.isWarnEnabled();
            case ERROR -> OUTBOUND_LOG.isErrorEnabled();
        };
    }

    private void write(LogLevel level, String event) {
        switch (level) {
            case DEBUG -> OUTBOUND_LOG.debug(event);
            case INFO -> OUTBOUND_LOG.info(event);
            case WARN -> OUTBOUND_LOG.warn(event);
            case ERROR -> OUTBOUND_LOG.error(event);
        }
    }

    private String contentType(HttpHeaders headers) {
        MediaType contentType = headers.getContentType();
        return contentType == null ? null : contentType.toString();
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

    private enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    private record ResponseCapture(
            ClientHttpResponse response,
            byte[] body,
            boolean bodySkipped,
            String bodySkipReason
    ) {
    }

    private record BufferedClientHttpResponse(
            ClientHttpResponse delegate,
            byte[] body
    ) implements ClientHttpResponse {

        @Override
        public @NonNull org.springframework.http.HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Override
        public @NonNull String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public @NonNull HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }

        @Override
        public @NonNull InputStream getBody() {
            return new ByteArrayInputStream(body);
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
