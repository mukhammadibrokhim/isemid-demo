package uz.uzinfocom.isemid.commons.configs.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestClientLoggingInterceptor implements ClientHttpRequestInterceptor {

  private static final Pattern PASSWORD_PATTERN =
      Pattern.compile("(password|passwd|pwd)[\"']?\\s*[:=]\\s*[\"']?([^\"',}\\]]+)",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern TOKEN_PATTERN =
      Pattern.compile("(token|authorization|bearer)[\"']?\\s*[:=]\\s*[\"']?([^\"',}\\]]+)",
          Pattern.CASE_INSENSITIVE);
  private final ObjectMapper objectMapper;

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {

    String parsedBody = body.length > 0
        ? new String(body, StandardCharsets.UTF_8)
        : "(empty)";

    ClientHttpResponse response = execution.execute(request, body);

    // Reading the response stream for logging drains (and, with chunked/pooled
    // connections, auto-closes) the underlying stream. Buffer it into memory first so
    // downstream message converters can still read the body afterwards.
    byte[] responseBytes = StreamUtils.copyToByteArray(response.getBody());
    String responseBody = new String(responseBytes, StandardCharsets.UTF_8);

    HttpStatus httpStatus = HttpStatus.resolve(
        response.getStatusCode().value());

    Map<String, String> logMap = new LinkedHashMap<>();
    logMap.put("uri",             request.getURI().toString());
    logMap.put("method",          request.getMethod().name());
    logMap.put("requestHeaders",  maskSensitiveData(request.getHeaders().toString()));
    logMap.put("requestBody",     parsedBody);
    logMap.put("statusCode",      String.valueOf(response.getStatusCode().value()));
    logMap.put("statusMessage",   httpStatus != null
        ? httpStatus.getReasonPhrase() : "Unknown");
    logMap.put("responseHeaders", response.getHeaders().toString());
    logMap.put("responseBody",    responseBody);

    log.info(objectMapper.writeValueAsString(logMap));

    return new BufferedClientHttpResponse(response, responseBytes);
  }

  private record BufferedClientHttpResponse(
      ClientHttpResponse delegate, byte[] body) implements ClientHttpResponse {

    @Override
    public InputStream getBody() {
      return new ByteArrayInputStream(body);
    }

    @Override
    public HttpHeaders getHeaders() {
      return delegate.getHeaders();
    }

    @Override
    public HttpStatusCode getStatusCode() throws IOException {
      return delegate.getStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
      return delegate.getStatusText();
    }

    @Override
    public void close() {
      delegate.close();
    }
  }

  private String maskSensitiveData(String message) {
    if (message == null) {
      return null;
    }

    String masked = message;

    // Маскировка паролей
    masked = PASSWORD_PATTERN.matcher(masked)
        .replaceAll("$1: ********");

    // Маскировка токенов
    masked = TOKEN_PATTERN.matcher(masked)
        .replaceAll("$1: ********");

    return masked;
  }
}
