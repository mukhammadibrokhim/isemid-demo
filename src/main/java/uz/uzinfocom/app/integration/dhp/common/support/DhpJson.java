package uz.uzinfocom.app.integration.dhp.common.support;

import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Null-safe JsonNode readers shared by the DHP response mappers. */
public final class DhpJson {

    private static final DateTimeFormatter DAY_MONTH_YEAR = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private DhpJson() {
    }

    public static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode() || node.isObject() || node.isArray()) {
            return null;
        }

        String value = node.asString();
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** Value of {@code field} on an object node; {@code null} when absent or not an object. */
    public static String text(JsonNode node, String field) {
        return node == null || !node.isObject() ? null : text(node.get(field));
    }

    public static JsonNode child(JsonNode node, String field) {
        if (node == null || !node.isObject()) {
            return null;
        }

        JsonNode child = node.get(field);
        return child == null || child.isNull() ? null : child;
    }

    /** Lower-cases and drops {@code _}/{@code -} so {@code start_date}, {@code startDate} and {@code StartDate} compare equal. */
    public static String normalizeKey(String key) {
        return key.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
    }

    /** ISO date, ISO date-time (with or without offset) or {@code dd.MM.yyyy}; {@code null} when unparseable. */
    public static LocalDate date(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String value = raw.trim();
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
            // fall through to the other accepted shapes
        }
        try {
            return OffsetDateTime.parse(value).toLocalDate();
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return LocalDateTime.parse(value).toLocalDate();
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return LocalDate.parse(value, DAY_MONTH_YEAR);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    /** Date-time keeping the source's wall-clock time; a bare date becomes start-of-day. */
    public static LocalDateTime dateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String value = raw.trim();
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            // fall through
        }

        LocalDate date = date(value);
        return date == null ? null : date.atStartOfDay();
    }
}
