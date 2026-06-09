package uz.uzinfocom.app.integration.api2.common.support;

import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

public final class Api2ResponseBodySanitizer {

    private static final int MAX_SAFE_TEXT_LENGTH = 500;
    private static final String MASK = "****";

    private static final Pattern BEARER_PATTERN =
            Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9\\-._~+/]+=*");
    private static final Pattern JSON_KEY_VALUE_PATTERN =
            Pattern.compile("(?i)(\"(?:authorization|access_token|refresh_token|id_token|token|cookie|password|secret|client_secret|api_key|ppn|pinfl|nnuzb|passport|passport_number|passport_series|document|series|number|tin|birth_date)\"\\s*:\\s*\")([^\"]*)(\")");
    private static final Pattern RAW_KEY_VALUE_PATTERN =
            Pattern.compile("(?i)((?:authorization|access_token|refresh_token|id_token|token|cookie|password|secret|client_secret|api_key|ppn|pinfl|nnuzb|passport|passport_number|passport_series|document|series|number|tin|birth_date)\\s*[=:]\\s*)([^,\\s&]+)");
    private static final Pattern QUERY_KEY_VALUE_PATTERN =
            Pattern.compile("(?i)([?&](?:authorization|access_token|refresh_token|id_token|token|cookie|ppn|pinfl|nnuzb|passport|document|series|number|tin|birth_date)=)([^&\\s]+)");
    private static final Pattern LONG_IDENTIFIER_PATTERN =
            Pattern.compile("\\b\\d{9,}\\b");
    private static final Pattern CONTROL_WHITESPACE_PATTERN =
            Pattern.compile("\\s+");

    private Api2ResponseBodySanitizer() {
    }

    public static String sanitize(String raw, MediaType contentType) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }

        if (isHtml(contentType, raw)) {
            return "[html response omitted]";
        }

        String sanitized = BEARER_PATTERN.matcher(raw).replaceAll("Bearer " + MASK);
        sanitized = JSON_KEY_VALUE_PATTERN.matcher(sanitized).replaceAll("$1" + MASK + "$3");
        sanitized = RAW_KEY_VALUE_PATTERN.matcher(sanitized).replaceAll("$1" + MASK);
        sanitized = QUERY_KEY_VALUE_PATTERN.matcher(sanitized).replaceAll("$1" + MASK);
        sanitized = LONG_IDENTIFIER_PATTERN.matcher(sanitized).replaceAll(MASK);
        sanitized = CONTROL_WHITESPACE_PATTERN.matcher(sanitized).replaceAll(" ").trim();

        if (!StringUtils.hasText(sanitized)) {
            return null;
        }

        if (sanitized.length() > MAX_SAFE_TEXT_LENGTH) {
            return sanitized.substring(0, MAX_SAFE_TEXT_LENGTH);
        }

        return sanitized;
    }

    private static boolean isHtml(MediaType contentType, String raw) {
        if (contentType != null && MediaType.TEXT_HTML.includes(contentType)) {
            return true;
        }

        String trimmed = raw.trim().toLowerCase(Locale.ROOT);
        return trimmed.startsWith("<!doctype html")
                || trimmed.startsWith("<html")
                || trimmed.startsWith("<body");
    }
}
