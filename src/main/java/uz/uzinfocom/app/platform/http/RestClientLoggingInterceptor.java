package uz.uzinfocom.app.platform.http;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.observability.TraceContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestClientLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Tashkent");

    private final JsonMapper objectMapper;
    private final SensitiveLoggingSanitizer sanitizer;
    private final PlatformRestClientProperties properties;

    @Nonnull
    @Override
    public ClientHttpResponse intercept(
            @Nonnull HttpRequest request,
            @Nonnull byte[] body,
            @Nonnull ClientHttpRequestExecution execution
    ) throws IOException {

        if (!properties.isLoggingEnabled()) {
            return execution.execute(request, body);
        }

        OffsetDateTime startedAt = OffsetDateTime.now(ZONE_ID);

        try {
            ClientHttpResponse response = execution.execute(request, body);
            byte[] responseBody = readResponseBodySafely(response);

            OffsetDateTime finishedAt = OffsetDateTime.now(ZONE_ID);

            logSuccess(
                    request,
                    body,
                    response,
                    responseBody,
                    startedAt,
                    finishedAt
            );

            return new BufferedClientHttpResponse(response, responseBody);
        } catch (IOException | RuntimeException exception) {
            OffsetDateTime finishedAt = OffsetDateTime.now(ZONE_ID);

            logFailure(
                    request,
                    body,
                    startedAt,
                    finishedAt,
                    exception
            );

            throw exception;
        }
    }

    private void logSuccess(
            HttpRequest request,
            byte[] requestBody,
            ClientHttpResponse response,
            byte[] responseBody,
            OffsetDateTime startedAt,
            OffsetDateTime finishedAt
    ) {
        try {
            Map<String, Object> logMap = new LinkedHashMap<>();

            logMap.put("event", "outbound_http_completed");
            logMap.put("traceId", MDC.get(TraceContext.MDC_KEY));
            logMap.put("requestMethod", request.getMethod().name());
            logMap.put("requestUri", sanitizeUri(request));
            logMap.put("requestHeaders", sanitizer.sanitizeHeaders(request.getHeaders()));
            logMap.put("requestContentLength", requestBody == null ? 0 : requestBody.length);
            logMap.put("requestBody", sanitizer.sanitizeBody(
                    requestBody,
                    request.getHeaders(),
                    properties.isLogRequestBody()
            ));

            logMap.put("responseStatusCode", getStatusCodeSafely(response));
            logMap.put("responseStatusText", getStatusTextSafely(response));
            logMap.put("responseHeaders", sanitizer.sanitizeHeaders(response.getHeaders()));
            logMap.put("responseContentLength", responseBody == null ? 0 : responseBody.length);
            logMap.put("responseBody", sanitizer.sanitizeBody(
                    responseBody,
                    response.getHeaders(),
                    properties.isLogResponseBody()
            ));

            logMap.put("startedAt", startedAt.toString());
            logMap.put("finishedAt", finishedAt.toString());
            logMap.put("durationMs", Duration.between(startedAt, finishedAt).toMillis());

            log.info(objectMapper.writeValueAsString(logMap));
        } catch (Exception logException) {
            log.warn(
                    "Failed to write outbound HTTP success log. uri={}, error={}",
                    request.getURI(),
                    logException.getMessage(),
                    logException
            );
        }
    }

    private void logFailure(
            HttpRequest request,
            byte[] requestBody,
            OffsetDateTime startedAt,
            OffsetDateTime finishedAt,
            Exception exception
    ) {
        try {
            Map<String, Object> logMap = new LinkedHashMap<>();

            logMap.put("event", "outbound_http_failed");
            logMap.put("traceId", MDC.get(TraceContext.MDC_KEY));
            logMap.put("requestMethod", request.getMethod().name());
            logMap.put("requestUri", sanitizeUri(request));
            logMap.put("requestHeaders", sanitizer.sanitizeHeaders(request.getHeaders()));
            logMap.put("requestContentLength", requestBody == null ? 0 : requestBody.length);
            logMap.put("requestBody", sanitizer.sanitizeBody(
                    requestBody,
                    request.getHeaders(),
                    properties.isLogRequestBody()
            ));

            logMap.put("startedAt", startedAt.toString());
            logMap.put("finishedAt", finishedAt.toString());
            logMap.put("durationMs", Duration.between(startedAt, finishedAt).toMillis());
            logMap.put("errorType", exception.getClass().getName());
            logMap.put("errorMessage", exception.getMessage());

            log.error(objectMapper.writeValueAsString(logMap), exception);
        } catch (Exception logException) {
            log.warn(
                    "Failed to write outbound HTTP failure log. uri={}, error={}",
                    request.getURI(),
                    logException.getMessage(),
                    logException
            );
        }
    }

    private byte[] readResponseBodySafely(ClientHttpResponse response) throws IOException {
        InputStream bodyStream = response.getBody();
        return StreamUtils.copyToByteArray(bodyStream);
    }

    private Integer getStatusCodeSafely(ClientHttpResponse response) {
        try {
            return response.getStatusCode().value();
        } catch (IOException exception) {
            return null;
        }
    }

    private String getStatusTextSafely(ClientHttpResponse response) {
        try {
            return response.getStatusText();
        } catch (IOException exception) {
            return null;
        }
    }

    private String sanitizeUri(HttpRequest request) {
        String uri = String.valueOf(request.getURI());

        return uri
                .replaceAll("(?i)(access_token=)[^&]+", "$1****")
                .replaceAll("(?i)(refresh_token=)[^&]+", "$1****")
                .replaceAll("(?i)(token=)[^&]+", "$1****")
                .replaceAll("(?i)(pinfl=)[^&]+", "$1****")
                .replaceAll("(?i)(nnuzb=)[^&]+", "$1****");
    }

    private record BufferedClientHttpResponse(
            ClientHttpResponse delegate,
            byte[] body
    ) implements ClientHttpResponse {

        @Nonnull
        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Nonnull
        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Nonnull
        @Override
        public HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }

        @Nonnull
        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body == null ? new byte[0] : body);
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
