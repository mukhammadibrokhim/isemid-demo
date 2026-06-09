package uz.uzinfocom.app.integration.api2.common.support;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class Api2ResponseBodyReader {

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
        try {
            return StreamUtils.copyToByteArray(response.getBody());
        } catch (IOException exception) {
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
