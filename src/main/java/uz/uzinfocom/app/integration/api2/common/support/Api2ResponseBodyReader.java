package uz.uzinfocom.app.integration.api2.common.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class Api2ResponseBodyReader {

    private static final int MAX_RESPONSE_BODY_BYTES = 1_048_576;

    private final JsonMapper jsonMapper;

    public Api2ResponseBody read(ClientHttpResponse response) {
        MediaType contentType = response.getHeaders().getContentType();
        byte[] body = readBytes(response);

        if (body == null) {
            return new Api2ResponseBody(
                    null,
                    "Upstream response body could not be read.",
                    contentType,
                    true,
                    true
            );
        }

        if (body.length == 0) {
            return new Api2ResponseBody(null, null, contentType, true, false);
        }

        String raw = new String(body, StandardCharsets.UTF_8);
        String safeRawBody = Api2ResponseBodySanitizer.sanitize(raw, contentType);
        JsonNode json = parseJson(raw, contentType);

        return new Api2ResponseBody(json, safeRawBody, contentType, false, false);
    }

    private byte[] readBytes(ClientHttpResponse response) {
        long contentLength = response.getHeaders().getContentLength();
        if (contentLength > MAX_RESPONSE_BODY_BYTES) {
            return null;
        }
        try {
            byte[] body = response.getBody().readNBytes(MAX_RESPONSE_BODY_BYTES + 1);
            return body.length <= MAX_RESPONSE_BODY_BYTES ? body : null;
        } catch (IOException exception) {
            log.warn("Failed to read API2 upstream response body. errorType={}, message={}",
                    exception.getClass().getSimpleName(), exception.getMessage());
            return null;
        }
    }

    private JsonNode parseJson(String raw, MediaType contentType) {
        if (!shouldAttemptJson(raw, contentType)) {
            return null;
        }

        try {
            return jsonMapper.readTree(raw);
        } catch (RuntimeException exception) {
            // Jackson parse-exception messages can embed a fragment of the offending content
            // (citizen/legal-entity payloads here), so only the exception type and length are
            // logged - never exception.getMessage() or the raw body itself.
            log.warn("Failed to parse API2 upstream response body as JSON. errorType={}, bodyLength={}",
                    exception.getClass().getSimpleName(), raw.length());
            return null;
        }
    }

    private boolean shouldAttemptJson(String raw, MediaType contentType) {
        if (isJsonContent(contentType)) {
            return true;
        }

        String trimmed = raw == null ? "" : raw.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }

    private boolean isJsonContent(MediaType contentType) {
        if (contentType == null) {
            return true;
        }

        if (MediaType.APPLICATION_JSON.includes(contentType)) {
            return true;
        }

        return contentType.getSubtype().toLowerCase(Locale.ROOT).endsWith("+json");
    }
}
