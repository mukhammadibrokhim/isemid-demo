package uz.uzinfocom.app.platform.iam.infrastructure.remote.payload;

import tools.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class RemotePayloadSupport {

    private RemotePayloadSupport() {
    }

    public static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }

        if (node.isValueNode()) {
            String value = node.asText();
            return StringUtils.hasText(value) ? value.trim() : null;
        }

        return null;
    }

    public static String firstText(JsonNode node, String... fieldNames) {
        if (node == null || node.isNull() || node.isMissingNode() || !node.isObject()) {
            return null;
        }

        for (String fieldName : fieldNames) {
            JsonNode value = node.get(fieldName);
            String text = text(value);

            if (StringUtils.hasText(text)) {
                return text;
            }
        }

        return null;
    }

    public static List<String> textList(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return List.of();
        }

        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
            String value = text(node);
            return StringUtils.hasText(value) ? List.of(value) : List.of();
        }

        if (!node.isArray()) {
            return List.of();
        }

        List<String> values = new ArrayList<>();

        for (JsonNode item : node) {
            String value = text(item);

            if (StringUtils.hasText(value)) {
                values.add(value);
            }
        }

        return List.copyOf(values);
    }

    public static List<RemoteCodingPayload> extractCodings(JsonNode node) {
        List<RemoteCodingPayload> result = new ArrayList<>();
        walkCodings(node, result);
        return List.copyOf(result);
    }

    private static void walkCodings(JsonNode node, List<RemoteCodingPayload> result) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return;
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                walkCodings(item, result);
            }
            return;
        }

        if (!node.isObject()) {
            return;
        }

        JsonNode coding = node.get("coding");
        if (coding != null) {
            walkCodings(coding, result);
        }

        String code = text(node.get("code"));
        if (StringUtils.hasText(code)) {
            result.add(new RemoteCodingPayload(
                    text(node.get("system")),
                    code,
                    text(node.get("display"))
            ));
        }

        node.forEachEntry((fieldName, child) -> {
            if ("coding".equals(fieldName)
                    || "code".equals(fieldName)
                    || "system".equals(fieldName)
                    || "display".equals(fieldName)) {
                return;
            }

            if (child != null && (child.isObject() || child.isArray())) {
                walkCodings(child, result);
            }
        });
    }

    public static Optional<String> identifierValueByCode(
            List<RemoteIdentifierPayload> identifiers,
            String... codes
    ) {
        if (identifiers == null || identifiers.isEmpty()) {
            return Optional.empty();
        }

        return identifiers.stream()
                .filter(identifier -> identifier.hasTypeCode(codes))
                .map(RemoteIdentifierPayload::valueAsString)
                .filter(StringUtils::hasText)
                .findFirst();
    }

    public static Optional<String> identifierValueBySystemContains(
            List<RemoteIdentifierPayload> identifiers,
            String systemPart
    ) {
        if (identifiers == null || identifiers.isEmpty() || !StringUtils.hasText(systemPart)) {
            return Optional.empty();
        }

        return identifiers.stream()
                .filter(identifier -> identifier.systemContains(systemPart))
                .map(RemoteIdentifierPayload::valueAsString)
                .filter(StringUtils::hasText)
                .findFirst();
    }

    public static boolean equalsAnyIgnoreCase(String value, String... expectedValues) {
        if (!StringUtils.hasText(value) || expectedValues == null) {
            return false;
        }

        for (String expected : expectedValues) {
            if (StringUtils.hasText(expected) && value.equalsIgnoreCase(expected)) {
                return true;
            }
        }

        return false;
    }

    public static boolean containsIgnoreCase(String value, String part) {
        if (!StringUtils.hasText(value) || !StringUtils.hasText(part)) {
            return false;
        }

        return value.toLowerCase(Locale.ROOT).contains(part.toLowerCase(Locale.ROOT));
    }

    public static LocalDate parseDateOnly(String rawDate) {
        if (!StringUtils.hasText(rawDate)) {
            return null;
        }

        String trimmed = rawDate.trim();

        if (trimmed.length() < 10) {
            return null;
        }

        try {
            return LocalDate.parse(trimmed.substring(0, 10));
        } catch (Exception ignored) {
            return null;
        }
    }
}
