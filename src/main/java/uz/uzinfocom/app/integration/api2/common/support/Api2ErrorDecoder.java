package uz.uzinfocom.app.integration.api2.common.support;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.api2.api.dto.FieldValidationError;
import uz.uzinfocom.app.integration.api2.citizen.exception.CitizenDataNotFoundException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2AuthenticationException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2AuthorizationException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2BadGatewayException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2BadRequestException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2ConflictException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;
import uz.uzinfocom.app.integration.api2.common.exception.Api2RateLimitException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2TimeoutException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2UnavailableException;
import uz.uzinfocom.app.integration.api2.legalentity.exception.LegalEntityNotFoundException;

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class Api2ErrorDecoder {

    private static final Pattern SAFE_FIELD_NAME =
            Pattern.compile("^[A-Za-z0-9_.\\-\\[\\]]{1,64}$");

    public Api2Exception decodeCitizen(
            String operation,
            HttpStatusCode statusCode,
            Api2ResponseBody body
    ) {
        return decodeHttpError(operation, Api2Domain.CITIZEN, statusCode.value(), body);
    }

    public Api2Exception decodeLegalEntity(
            String operation,
            HttpStatusCode statusCode,
            Api2ResponseBody body
    ) {
        return decodeHttpError(operation, Api2Domain.LEGAL_ENTITY, statusCode.value(), body);
    }

    public Api2Exception decodeCitizenPayloadFailure(
            String operation,
            HttpStatusCode statusCode,
            Api2ResponseBody body
    ) {
        return decodePayloadFailure(operation, Api2Domain.CITIZEN, statusCode.value(), body);
    }

    public Api2Exception decodeLegalEntityPayloadFailure(
            String operation,
            HttpStatusCode statusCode,
            Api2ResponseBody body
    ) {
        return decodePayloadFailure(operation, Api2Domain.LEGAL_ENTITY, statusCode.value(), body);
    }

    public Api2Exception decodeTransport(String operation, RestClientException exception) {
        if (containsTimeout(exception)) {
            return new Api2TimeoutException(operation, exception);
        }

        return new Api2UnavailableException(operation, exception);
    }

    public boolean isFailurePayload(Api2ResponseBody body) {
        JsonNode json = body == null ? null : body.json();
        if (json == null || !json.isObject()) {
            return false;
        }

        if (isFailureValue(json.get("status"))) {
            return true;
        }

        if (isFailureValue(json.get("result"))) {
            return true;
        }

        JsonNode data = json.get("data");
        return (data == null || data.isNull())
                && isFailureValue(json.get("comments"));
    }

    private Api2Exception decodeHttpError(
            String operation,
            Api2Domain domain,
            int upstreamStatus,
            Api2ResponseBody body
    ) {
        Api2UpstreamError upstreamError = upstreamError(upstreamStatus, body);
        return decodeByStatus(operation, domain, upstreamStatus, upstreamError, body);
    }

    private Api2Exception decodePayloadFailure(
            String operation,
            Api2Domain domain,
            int httpStatus,
            Api2ResponseBody body
    ) {
        int upstreamStatus = payloadStatus(body, httpStatus);
        Api2UpstreamError upstreamError = upstreamError(upstreamStatus, body);

        if (upstreamStatus < 200 || upstreamStatus >= 300) {
            return decodeByStatus(operation, domain, upstreamStatus, upstreamError, body);
        }

        if (isNotFoundSignal(upstreamError)) {
            return notFound(operation, domain, upstreamError);
        }

        if (isClearlyRequestError(upstreamError)) {
            return new Api2BadRequestException(operation, upstreamError, fieldErrors(body));
        }

        if (isDomainConflict(upstreamError)) {
            return new Api2ConflictException(operation, upstreamError);
        }

        if (containsAny(upstreamError, "rate limit", "too many")) {
            return new Api2RateLimitException(operation, upstreamError);
        }

        if (containsAny(upstreamError, "timeout", "timed out")) {
            return new Api2TimeoutException(operation, upstreamError);
        }

        if (containsAny(upstreamError, "unauthor", "authentication", "credential", "token")) {
            return new Api2AuthenticationException(operation, statusOnly(upstreamError));
        }

        if (containsAny(upstreamError, "forbidden", "denied", "access")) {
            return new Api2AuthorizationException(operation, upstreamError);
        }

        return new Api2BadGatewayException(operation, upstreamError);
    }

    private Api2Exception decodeByStatus(
            String operation,
            Api2Domain domain,
            int upstreamStatus,
            Api2UpstreamError upstreamError,
            Api2ResponseBody body
    ) {
        return switch (upstreamStatus) {
            case 400, 422 -> new Api2BadRequestException(operation, upstreamError, fieldErrors(body));
            case 401 -> new Api2AuthenticationException(operation, statusOnly(upstreamError));
            case 403 -> new Api2AuthorizationException(operation, upstreamError);
            case 404 -> notFound(operation, domain, upstreamError);
            case 408, 504 -> new Api2TimeoutException(operation, upstreamError);
            case 409 -> isDomainConflict(upstreamError)
                    ? new Api2ConflictException(operation, upstreamError)
                    : new Api2BadGatewayException(operation, upstreamError);
            case 429 -> new Api2RateLimitException(operation, upstreamError);
            case 500, 501, 502, 503 -> new Api2BadGatewayException(operation, upstreamError);
            default -> decodeUnknownStatus(operation, upstreamStatus, upstreamError, body);
        };
    }

    private Api2Exception decodeUnknownStatus(
            String operation,
            int upstreamStatus,
            Api2UpstreamError upstreamError,
            Api2ResponseBody body
    ) {
        if (upstreamStatus >= 400 && upstreamStatus < 500 && isClearlyRequestError(upstreamError)) {
            return new Api2BadRequestException(operation, upstreamError, fieldErrors(body));
        }

        if (upstreamStatus >= 500) {
            return new Api2BadGatewayException(operation, upstreamError);
        }

        return new Api2BadGatewayException(operation, upstreamError);
    }

    private Api2Exception notFound(
            String operation,
            Api2Domain domain,
            Api2UpstreamError upstreamError
    ) {
        return domain == Api2Domain.CITIZEN
                ? new CitizenDataNotFoundException(operation, upstreamError)
                : new LegalEntityNotFoundException(operation, upstreamError);
    }

    private Api2UpstreamError upstreamError(int upstreamStatus, Api2ResponseBody body) {
        JsonNode json = body == null ? null : body.json();
        String detail = firstText(json, "detail", "description", "comments");

        if (!StringUtils.hasText(detail) && body != null && !body.hasJson()) {
            detail = body.safeRawBody();
        }

        return new Api2UpstreamError(
                upstreamStatus,
                firstText(json, "code", "errorCode", "error", "result"),
                firstText(json, "message", "title", "comments", "result"),
                detail
        );
    }

    private Api2UpstreamError statusOnly(Api2UpstreamError upstreamError) {
        return new Api2UpstreamError(upstreamError.status(), null, null, null);
    }

    private int payloadStatus(Api2ResponseBody body, int fallbackStatus) {
        JsonNode status = body == null || body.json() == null ? null : body.json().get("status");

        if (status == null || status.isNull()) {
            return fallbackStatus;
        }

        if (status.isNumber()) {
            int value = status.asInt();
            return value >= 100 && value <= 599 ? value : fallbackStatus;
        }

        if (status.isTextual()) {
            try {
                int value = Integer.parseInt(status.asText().trim());
                return value >= 100 && value <= 599 ? value : fallbackStatus;
            } catch (NumberFormatException ignored) {
                return fallbackStatus;
            }
        }

        return fallbackStatus;
    }

    private List<FieldValidationError> fieldErrors(Api2ResponseBody body) {
        JsonNode json = body == null ? null : body.json();
        if (json == null || !json.isObject()) {
            return List.of();
        }

        List<FieldValidationError> errors = new ArrayList<>();
        collectFieldErrors(json.get("fieldErrors"), errors);
        collectFieldErrors(json.get("validationErrors"), errors);
        collectFieldErrors(json.get("violations"), errors);
        collectFieldErrors(json.get("errors"), errors);
        collectFieldErrors(json.get("fields"), errors);

        return List.copyOf(errors);
    }

    private void collectFieldErrors(JsonNode node, List<FieldValidationError> errors) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                collectFieldErrors(item, errors);
            }
            return;
        }

        if (!node.isObject()) {
            return;
        }

        String field = firstText(node, "field", "name", "path", "property", "parameter");
        if (StringUtils.hasText(field)) {
            errors.add(new FieldValidationError(safeField(field), "validation.invalid_value"));
            return;
        }

        for (var entry : node.properties()) {
            if (entry.getValue() == null || entry.getValue().isNull()) {
                continue;
            }

            if (!entry.getValue().isObject() && !entry.getValue().isArray()) {
                errors.add(new FieldValidationError(safeField(entry.getKey()), "validation.invalid_value"));
            }
        }
    }

    private String safeField(String rawField) {
        String field = rawField == null ? "" : rawField.trim();
        if (!SAFE_FIELD_NAME.matcher(field).matches()) {
            return "request";
        }

        return field;
    }

    private String firstText(JsonNode node, String... fieldNames) {
        if (node == null || node.isNull()) {
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

        return Api2ResponseBodySanitizer.sanitize(node.asText(), null);
    }

    private boolean isFailureValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return false;
        }

        if (node.isBoolean()) {
            return !node.asBoolean();
        }

        if (node.isNumber()) {
            int value = node.asInt();
            return value == 0 || value < 200 || value >= 400;
        }

        if (!node.isTextual()) {
            return false;
        }

        String value = normalize(node.asText());
        return value.equals("0")
                || value.equals("false")
                || value.equals("fail")
                || value.equals("failed")
                || value.equals("failure")
                || value.equals("error")
                || value.equals("incorrect")
                || value.equals("invalid")
                || value.equals("not_found")
                || value.equals("not found")
                || value.contains("not found")
                || value.contains("validation")
                || value.contains("invalid");
    }

    private boolean isClearlyRequestError(Api2UpstreamError upstreamError) {
        return containsAny(
                upstreamError,
                "bad request",
                "validation",
                "invalid",
                "incorrect",
                "required",
                "unprocessable"
        );
    }

    private boolean isNotFoundSignal(Api2UpstreamError upstreamError) {
        return containsAny(upstreamError, "not_found", "not found", "notfound");
    }

    private boolean isDomainConflict(Api2UpstreamError upstreamError) {
        return containsAny(upstreamError, "conflict", "duplicate", "already exists");
    }

    private boolean containsAny(Api2UpstreamError upstreamError, String... needles) {
        String value = normalize(String.join(
                " ",
                nullToEmpty(upstreamError.code()),
                nullToEmpty(upstreamError.message()),
                nullToEmpty(upstreamError.detail())
        ));

        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean containsTimeout(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }

            String className = current.getClass().getName().toLowerCase(Locale.ROOT);
            String message = current.getMessage() == null
                    ? ""
                    : current.getMessage().toLowerCase(Locale.ROOT);

            if (className.contains("timeout")
                    || message.contains("timed out")
                    || message.contains("timeout")) {
                return true;
            }

            if (current instanceof InterruptedIOException && message.contains("timed out")) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    private enum Api2Domain {
        CITIZEN,
        LEGAL_ENTITY
    }
}
