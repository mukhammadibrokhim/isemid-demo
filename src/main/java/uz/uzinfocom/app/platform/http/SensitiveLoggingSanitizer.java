package uz.uzinfocom.app.platform.http;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SensitiveLoggingSanitizer {

    private static final String MASK = "****";

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            HttpHeaders.AUTHORIZATION.toLowerCase(Locale.ROOT),
            HttpHeaders.COOKIE.toLowerCase(Locale.ROOT),
            HttpHeaders.SET_COOKIE.toLowerCase(Locale.ROOT),
            "proxy-authorization",
            "x-auth-token",
            "x-api-key",
            "api-key"
    );

    private static final Set<String> SENSITIVE_FIELDS = new LinkedHashSet<>(Set.of(
            "authorization",
            "access_token",
            "refresh_token",
            "id_token",
            "token",
            "bearer",
            "password",
            "old_password",
            "new_password",
            "secret",
            "client_secret",
            "api_key",
            "x_api_key",
            "ppn",
            "pinfl",
            "nnuzb",
            "passport",
            "passport_number",
            "passport_series",
            "phone",
            "email"
    ));

    private static final Pattern BEARER_PATTERN =
            Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9\\-._~+/]+=*");

    private static final Pattern JSON_KEY_VALUE_PATTERN =
            Pattern.compile("(?i)(\"(?:authorization|access_token|refresh_token|id_token|token|password|secret|client_secret|api_key|ppn|nnuzb|pinfl|passport|passport_number|passport_series|phone|email)\"\\s*:\\s*\")([^\"]*)(\")");

    private static final Pattern RAW_KEY_VALUE_PATTERN =
            Pattern.compile("(?i)((?:authorization|access_token|refresh_token|id_token|token|password|secret|client_secret|api_key|ppn|nnuzb|pinfl|passport|passport_number|passport_series|phone|email)\\s*[=:]\\s*)([^,\\s&]+)");

    private static final Pattern PEM_BLOCK_PATTERN =
            Pattern.compile("(?s)^\\s*-----BEGIN [A-Z0-9 ]+-----.*-----END [A-Z0-9 ]+-----\\s*$");

    private final JsonMapper jsonMapper;
    private final PlatformRestClientProperties properties;

    public HttpHeaders sanitizeHeaders(HttpHeaders headers) {
        HttpHeaders sanitized = new HttpHeaders();

        if (headers == null || headers.isEmpty()) {
            return sanitized;
        }

        headers.forEach((name, values) -> {
            String normalized = normalize(name);

            if (SENSITIVE_HEADERS.contains(normalized)) {
                if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                    sanitized.put(name, values.stream()
                            .map(this::maskAuthorizationHeader)
                            .toList());
                } else {
                    sanitized.put(name, values.stream()
                            .map(value -> MASK)
                            .toList());
                }
            } else {
                sanitized.put(name, new ArrayList<>(values));
            }
        });

        return sanitized;
    }

    public Object sanitizeBody(
            byte[] body,
            HttpHeaders headers,
            boolean logBody
    ) {
        if (!logBody) {
            return "[body logging disabled]";
        }

        if (body == null || body.length == 0) {
            return null;
        }

        int maxLoggedBodySizeBytes = properties.getMaxLoggedBodySizeBytes();

        if (body.length > maxLoggedBodySizeBytes) {
            return "[body omitted: " + body.length + " bytes exceeds limit "
                    + maxLoggedBodySizeBytes + " bytes]";
        }

        MediaType contentType = headers == null ? null : headers.getContentType();

        if (!isTextLikeContent(contentType)) {
            return "[binary body omitted: contentType=" + contentType + ", size=" + body.length + " bytes]";
        }

        String raw = new String(body, StandardCharsets.UTF_8);

        if (isPemBlock(raw)) {
            return "[sensitive PEM/key body omitted: " + body.length + " bytes]";
        }

        if (isJsonContent(contentType)) {
            try {
                JsonNode jsonNode = jsonMapper.readTree(raw);
                return sanitizeJson(jsonNode);
            } catch (Exception ignored) {
                return sanitizeRawText(raw);
            }
        }

        return sanitizeRawText(raw);
    }

    private JsonNode sanitizeJson(JsonNode node) {
        if (node == null) {
            return null;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node.deepCopy();

            for (Map.Entry<String, JsonNode> entry : new ArrayList<>(objectNode.properties())) {
                String fieldName = entry.getKey();
                JsonNode value = entry.getValue();

                if (isSensitiveField(fieldName)) {
                    objectNode.put(fieldName, MASK);
                } else {
                    objectNode.set(fieldName, sanitizeJson(value));
                }
            }

            return objectNode;
        }

        if (node.isArray()) {
            ArrayNode arrayNode = jsonMapper.createArrayNode();

            for (JsonNode item : node) {
                arrayNode.add(sanitizeJson(item));
            }

            return arrayNode;
        }

        return node;
    }

    private Object sanitizeRawText(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }

        String sanitized = BEARER_PATTERN.matcher(raw).replaceAll("Bearer " + MASK);
        sanitized = JSON_KEY_VALUE_PATTERN.matcher(sanitized).replaceAll("$1" + MASK + "$3");
        sanitized = RAW_KEY_VALUE_PATTERN.matcher(sanitized).replaceAll("$1" + MASK);

        return sanitized;
    }

    private boolean isPemBlock(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }

        String trimmed = raw.trim();

        return PEM_BLOCK_PATTERN.matcher(trimmed).matches()
                || trimmed.startsWith("-----BEGIN PUBLIC KEY-----")
                || trimmed.startsWith("-----BEGIN PRIVATE KEY-----")
                || trimmed.startsWith("-----BEGIN RSA PRIVATE KEY-----")
                || trimmed.startsWith("-----BEGIN EC PRIVATE KEY-----")
                || trimmed.startsWith("-----BEGIN ENCRYPTED PRIVATE KEY-----")
                || trimmed.startsWith("-----BEGIN CERTIFICATE-----")
                || trimmed.startsWith("-----BEGIN CERTIFICATE REQUEST-----")
                || trimmed.startsWith("-----BEGIN NEW CERTIFICATE REQUEST-----")
                || trimmed.startsWith("-----BEGIN X509 CERTIFICATE-----");
    }

    private String maskAuthorizationHeader(String value) {
        if (value == null || value.isBlank()) {
            return MASK;
        }

        if (value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return "Bearer " + MASK;
        }

        return MASK;
    }

    private boolean isSensitiveField(String fieldName) {
        return SENSITIVE_FIELDS.contains(normalize(fieldName));
    }

    private boolean isJsonContent(MediaType contentType) {
        if (contentType == null) {
            return false;
        }

        if (MediaType.APPLICATION_JSON.includes(contentType)) {
            return true;
        }

        return contentType.getSubtype().toLowerCase(Locale.ROOT).endsWith("+json");
    }

    private boolean isTextLikeContent(MediaType contentType) {
        if (contentType == null) {
            return true;
        }

        if (isJsonContent(contentType)) {
            return true;
        }

        if (MediaType.TEXT_PLAIN.includes(contentType)
                || MediaType.TEXT_HTML.includes(contentType)
                || MediaType.APPLICATION_XML.includes(contentType)
                || MediaType.TEXT_XML.includes(contentType)
                || MediaType.APPLICATION_FORM_URLENCODED.includes(contentType)) {
            return true;
        }

        String type = contentType.getType().toLowerCase(Locale.ROOT);
        String subtype = contentType.getSubtype().toLowerCase(Locale.ROOT);

        return "text".equals(type)
                || subtype.endsWith("+xml")
                || subtype.contains("json")
                || subtype.contains("text");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}