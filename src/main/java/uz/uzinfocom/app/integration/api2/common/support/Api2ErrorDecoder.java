package uz.uzinfocom.app.integration.api2.common.support;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.api2.citizen.exception.CitizenDataNotFoundException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2AuthenticationException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2AuthorizationException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2BadRequestException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;
import uz.uzinfocom.app.integration.api2.common.exception.Api2RateLimitException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2UnavailableException;
import uz.uzinfocom.app.integration.api2.legalentity.exception.LegalEntityNotFoundException;

import java.util.regex.Pattern;

@Component
public class Api2ErrorDecoder {

    private static final int MAX_SAFE_TEXT_LENGTH = 500;
    private static final Pattern BEARER_PATTERN =
            Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9\\-._~+/]+=*");
    private static final Pattern RAW_KEY_VALUE_PATTERN =
            Pattern.compile("(?i)((?:authorization|access_token|refresh_token|id_token|token|cookie|nnuzb|pinfl|document|series|number|tin|birth_date)\\s*[=:]\\s*)([^,\\s&]+)");
    private static final Pattern QUERY_KEY_VALUE_PATTERN =
            Pattern.compile("(?i)([?&](?:nnuzb|pinfl|document|series|number|tin|birth_date|token|access_token|refresh_token)=)([^&\\s]+)");

    public Api2Exception decodeCitizen(String operation, HttpStatusCode statusCode, JsonNode body) {
        Api2UpstreamError upstreamError = upstreamError(statusCode, body);

        if (statusCode.value() == 404) {
            return new CitizenDataNotFoundException(operation, upstreamError);
        }

        return decodeCommon(operation, statusCode, upstreamError);
    }

    public Api2Exception decodeLegalEntity(String operation, HttpStatusCode statusCode, JsonNode body) {
        Api2UpstreamError upstreamError = upstreamError(statusCode, body);

        if (statusCode.value() == 404) {
            return new LegalEntityNotFoundException(operation, upstreamError);
        }

        return decodeCommon(operation, statusCode, upstreamError);
    }

    private Api2Exception decodeCommon(
            String operation,
            HttpStatusCode statusCode,
            Api2UpstreamError upstreamError
    ) {
        return switch (statusCode.value()) {
            case 400 -> new Api2BadRequestException(operation, upstreamError);
            case 401 -> new Api2AuthenticationException(operation, upstreamError);
            case 403 -> new Api2AuthorizationException(operation, upstreamError);
            case 429 -> new Api2RateLimitException(operation, upstreamError);
            default -> new Api2UnavailableException(operation, upstreamError);
        };
    }

    private Api2UpstreamError upstreamError(HttpStatusCode statusCode, JsonNode body) {
        return new Api2UpstreamError(
                statusCode.value(),
                firstText(body, "code", "errorCode", "error"),
                firstText(body, "message", "title"),
                firstText(body, "detail", "description")
        );
    }

    private String firstText(JsonNode node, String... fieldNames) {
        if (node == null) {
            return null;
        }

        for (String fieldName : fieldNames) {
            JsonNode value = node.get(fieldName);
            String text = safeText(value);

            if (StringUtils.hasText(text)) {
                return text;
            }
        }

        JsonNode errorNode = node.get("error");
        if (errorNode != null && errorNode.isObject()) {
            return firstText(errorNode, fieldNames);
        }

        return null;
    }

    private String safeText(JsonNode node) {
        if (node == null || node.isNull() || node.isObject() || node.isArray()) {
            return null;
        }

        String raw = node.asText();
        if (!StringUtils.hasText(raw)) {
            return null;
        }

        String sanitized = BEARER_PATTERN.matcher(raw).replaceAll("Bearer ****");
        sanitized = RAW_KEY_VALUE_PATTERN.matcher(sanitized).replaceAll("$1****");
        sanitized = QUERY_KEY_VALUE_PATTERN.matcher(sanitized).replaceAll("$1****");
        sanitized = sanitized.trim();

        if (!StringUtils.hasText(sanitized)) {
            return null;
        }

        if (sanitized.length() > MAX_SAFE_TEXT_LENGTH) {
            return sanitized.substring(0, MAX_SAFE_TEXT_LENGTH);
        }

        return sanitized;
    }
}
