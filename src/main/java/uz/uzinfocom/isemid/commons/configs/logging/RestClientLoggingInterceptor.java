package uz.uzinfocom.isemid.commons.configs.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestClientLoggingInterceptor implements ClientHttpRequestInterceptor {
  private final ObjectMapper objectMapper;

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {

    String parsedBody = body.length > 0
        ? new String(body, StandardCharsets.UTF_8)
        : "(empty)";

    ClientHttpResponse response = execution.execute(request, body);

    String responseBody = StreamUtils.copyToString(
        response.getBody(), StandardCharsets.UTF_8);

    HttpStatus httpStatus = HttpStatus.resolve(
        response.getStatusCode().value());

    Map<String, String> logMap = new LinkedHashMap<>();
    logMap.put("uri",             request.getURI().toString());
    logMap.put("method",          request.getMethod().name());
    logMap.put("requestHeaders",  request.getHeaders().toString());
    logMap.put("requestBody",     parsedBody);
    logMap.put("statusCode",      String.valueOf(response.getStatusCode().value()));
    logMap.put("statusMessage",   httpStatus != null
        ? httpStatus.getReasonPhrase() : "Unknown");
    logMap.put("responseHeaders", response.getHeaders().toString());
    logMap.put("responseBody",    responseBody);

    log.info(objectMapper.writeValueAsString(logMap));

    return response;
  }
}
