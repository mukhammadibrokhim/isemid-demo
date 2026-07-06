package uz.uzinfocom.app.platform.http;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SensitiveLoggingSanitizer {

    private static final String MASK = "***";
    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization", "proxy-authorization", "cookie", "set-cookie",
            "x-api-key", "api-key", "client-secret", "x-auth-token",
            "access-token", "refresh-token", "x-access-token", "x-refresh-token"
    );
    private static final Set<String> SENSITIVE_FIELDS = new LinkedHashSet<>(Set.of(
            "authorization", "access_token", "refresh_token", "id_token", "token", "bearer",
            "password", "old_password", "new_password", "secret", "client_secret", "api_key",
            "apikey", "x_api_key", "pinfl", "nnuzb", "ni", "ppn", "passport",
            "passport_number", "passport_series", "birth_certificate", "phone", "phone_number",
            "patient_id", "patient_identifier", "medical_record_number", "cookie"
    ));
    private static final Pattern BEARER_PATTERN =
            Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9\\-._~+/]+=*");
    private static final Pattern JSON_KEY_VALUE_PATTERN = Pattern.compile(
            "(?i)(\"(?:authorization|access_token|refresh_token|id_token|token|password|secret|client_secret|api_key|apikey|pinfl|nnuzb|passport|passport_number|passport_series|birth_certificate|phone|phone_number|patient_id|patient_identifier|medical_record_number)\"\\s*:\\s*\")([^\"]*)(\")"
    );
    private static final Pattern RAW_KEY_VALUE_PATTERN = Pattern.compile(
            "(?i)((?:authorization|access_token|refresh_token|id_token|token|password|secret|client_secret|api_key|apikey|pinfl|nnuzb|passport|passport_number|passport_series|birth_certificate|phone|phone_number|patient_id|patient_identifier|medical_record_number)\\s*[=:]\\s*)([^,\\s&]+)"
    );
    private static final Pattern PEM_BLOCK_PATTERN = Pattern.compile(
            "(?s)\\s*-----BEGIN [A-Z0-9 ]+-----.*-----END [A-Z0-9 ]+-----\\s*"
    );
    private static final Pattern UUID_PATH_PATTERN = Pattern.compile(
            "(?i)(?<=/)[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}(?=/|$)"
    );
    private static final Pattern NUMERIC_PATH_PATTERN = Pattern.compile("(?<=/)\\d{6,}(?=/|$)");

    private final JsonMapper jsonMapper;

    public HttpHeaders sanitizeHeaders(HttpHeaders headers) {
        HttpHeaders sanitized = new HttpHeaders();
        if (headers == null || headers.isEmpty()) {
            return sanitized;
        }

        headers.forEach((name, values) -> {
            if (isSensitiveHeader(name)) {
                sanitized.put(name, values.stream().map(ignored -> MASK).toList());
            } else {
                sanitized.put(name, values.stream().map(value -> sanitizeText(value, 500)).toList());
            }
        });
        return sanitized;
    }

    public String sanitizeQuery(String rawQuery, Set<String> sensitiveParameters, int maxLength) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return null;
        }

        StringBuilder result = new StringBuilder(Math.min(rawQuery.length(), maxLength));
        int cursor = 0;
        while (cursor <= rawQuery.length() && result.length() < maxLength) {
            int ampersand = rawQuery.indexOf('&', cursor);
            int end = ampersand >= 0 ? ampersand : rawQuery.length();
            String pair = rawQuery.substring(cursor, end);
            int equals = pair.indexOf('=');
            String name = equals >= 0 ? pair.substring(0, equals) : pair;
            String value = equals >= 0 ? pair.substring(equals + 1) : null;

            if (!result.isEmpty()) {
                result.append('&');
            }
            result.append(sanitizeText(name, maxLength));
            if (equals >= 0) {
                result.append('=');
                result.append(isSensitiveParameter(name, sensitiveParameters)
                        ? MASK
                        : sanitizeText(value, maxLength));
            }

            if (ampersand < 0) {
                break;
            }
            cursor = ampersand + 1;
        }
        return truncate(result.toString(), maxLength);
    }

    public String sanitizePath(String path, boolean maskIdentifiers, int maxLength) {
        String sanitized = sanitizeText(path, maxLength);
        if (sanitized == null || !maskIdentifiers) {
            return sanitized;
        }
        sanitized = UUID_PATH_PATTERN.matcher(sanitized).replaceAll("***");
        sanitized = NUMERIC_PATH_PATTERN.matcher(sanitized).replaceAll("***");
        return truncate(sanitized, maxLength);
    }

    public String sanitizeUri(
            URI uri,
            Set<String> sensitiveParameters,
            int maxLength
    ) {
        if (uri == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        if (uri.getScheme() != null) {
            result.append(sanitizeText(uri.getScheme(), 20)).append("://");
        }
        if (uri.getHost() != null) {
            result.append(sanitizeText(uri.getHost(), 255));
        }
        if (uri.getPort() >= 0) {
            result.append(':').append(uri.getPort());
        }
        String path = sanitizePath(uri.getRawPath(), false, maxLength);
        if (path != null) {
            result.append(path);
        }
        String query = sanitizeQuery(uri.getRawQuery(), sensitiveParameters, maxLength);
        if (query != null) {
            result.append('?').append(query);
        }
        return truncate(result.toString(), maxLength);
    }

    public String sanitizeText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String masked = maskSecrets(value);
        StringBuilder result = new StringBuilder(Math.min(masked.length(), maxLength));
        for (int index = 0; index < masked.length() && result.length() < maxLength; index++) {
            char character = masked.charAt(index);
            if (character == '\r' || character == '\n' || character == '\t'
                    || Character.isISOControl(character)) {
                result.append(' ');
            } else if (character == '"') {
                result.append('\'');
            } else {
                result.append(character);
            }
        }
        return truncate(result.toString().trim(), maxLength);
    }

    public String sanitizeBody(byte[] body, HttpHeaders headers, int maxLength) {
        if (body == null || body.length == 0) {
            return null;
        }
        String raw = new String(body, StandardCharsets.UTF_8);
        if (PEM_BLOCK_PATTERN.matcher(raw).matches()) {
            return "[sensitive key material omitted]";
        }

        MediaType contentType = headers == null ? null : headers.getContentType();
        if (isJsonContent(contentType)) {
            try {
                return truncate(jsonMapper.writeValueAsString(sanitizeJson(jsonMapper.readTree(raw))), maxLength);
            } catch (Exception ignored) {
                // The precompiled text masks below are the safe fallback for malformed JSON.
            }
        }

        return sanitizeText(raw, maxLength);
    }

    public boolean isAllowedTextContentType(MediaType contentType, List<String> allowedTypes) {
        if (contentType == null || isExplicitlyUnsafe(contentType)) {
            return false;
        }
        for (String configured : allowedTypes) {
            try {
                if (MediaType.parseMediaType(configured).includes(contentType)) {
                    return true;
                }
            } catch (IllegalArgumentException ignored) {
                // Invalid configuration is ignored; it never broadens body logging.
            }
        }
        return false;
    }

    private JsonNode sanitizeJson(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node.deepCopy();
            for (Map.Entry<String, JsonNode> entry : new ArrayList<>(objectNode.properties())) {
                if (SENSITIVE_FIELDS.contains(normalize(entry.getKey()))) {
                    objectNode.put(entry.getKey(), MASK);
                } else {
                    objectNode.set(entry.getKey(), sanitizeJson(entry.getValue()));
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

    private boolean isSensitiveHeader(String name) {
        String normalized = normalize(name);
        return SENSITIVE_HEADERS.contains(normalized)
                || normalized.contains("access-token")
                || normalized.contains("refresh-token");
    }

    private boolean isSensitiveParameter(String rawName, Set<String> configured) {
        String decoded;
        try {
            decoded = URLDecoder.decode(rawName, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            decoded = rawName;
        }
        String normalized = normalize(decoded);
        if (SENSITIVE_FIELDS.contains(normalized)) {
            return true;
        }
        return configured != null && configured.stream().map(this::normalize).anyMatch(normalized::equals);
    }

    private boolean isExplicitlyUnsafe(MediaType contentType) {
        String type = normalize(contentType.getType());
        String subtype = normalize(contentType.getSubtype());
        return "multipart".equals(type)
                || "image".equals(type)
                || "audio".equals(type)
                || "video".equals(type)
                || subtype.contains("octet-stream")
                || subtype.contains("pdf")
                || subtype.contains("protobuf")
                || subtype.contains("zip")
                || subtype.contains("gzip")
                || subtype.contains("tar")
                || subtype.contains("rar");
    }

    private boolean isJsonContent(MediaType contentType) {
        return contentType != null && (MediaType.APPLICATION_JSON.includes(contentType)
                || normalize(contentType.getSubtype()).endsWith("+json"));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String maskSecrets(String value) {
        String sanitized = BEARER_PATTERN.matcher(value).replaceAll("Bearer " + MASK);
        sanitized = JSON_KEY_VALUE_PATTERN.matcher(sanitized).replaceAll("$1" + MASK + "$3");
        return RAW_KEY_VALUE_PATTERN.matcher(sanitized).replaceAll("$1" + MASK);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }
}
