package uz.uzinfocom.app.platform.security.claims;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JwtClaimUtils {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
    );

    private JwtClaimUtils() {
    }

    public static String stringValue(Object value) {
        if (value == null) {
            return null;
        }

        String text = String.valueOf(value).trim();

        return StringUtils.hasText(text) ? text : null;
    }

    public static String stringFromMap(Map<?, ?> map, String key) {
        if (map == null || !StringUtils.hasText(key)) {
            return null;
        }

        return stringValue(map.get(key));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> requiredMap(
            Map<String, Object> source,
            String key
    ) {
        Object value = source.get(key);

        if (!(value instanceof Map<?, ?> map)) {
            throw new InsufficientAuthenticationException(
                    "Required JWT claim is missing or invalid: " + key
            );
        }

        return (Map<String, Object>) map;
    }

    public static List<?> listValue(Object value) {
        return switch (value) {
            case null -> List.of();
            case List<?> list -> list;
            case Collection<?> collection -> new ArrayList<>(collection);
            default -> List.of(value);
        };

    }

    public static UUID requiredUuid(
            Object value,
            String errorMessage
    ) {
        return uuidFromAny(value)
                .orElseThrow(() -> new InsufficientAuthenticationException(errorMessage));
    }

    public static Optional<UUID> uuidFromAny(Object value) {
        switch (value) {
            case null -> {
                return Optional.empty();
            }
            case UUID uuid -> {
                return Optional.of(uuid);
            }
            case Map<?, ?> map -> {
                Object direct = firstMapValue(
                        map,
                        "uuid",
                        "id",
                        "value",
                        "reference",
                        "ref"
                );

                return uuidFromAny(direct);
            }
            case Collection<?> collection -> {
                for (Object item : collection) {
                    Optional<UUID> uuid = uuidFromAny(item);

                    if (uuid.isPresent()) {
                        return uuid;
                    }
                }

                return Optional.empty();
            }
            default -> {
            }
        }

        String text = String.valueOf(value).trim();

        if (!StringUtils.hasText(text)) {
            return Optional.empty();
        }

        Matcher matcher = UUID_PATTERN.matcher(text);

        if (!matcher.find()) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(matcher.group()));
        } catch (IllegalArgumentException malformedUuid) {
            return Optional.empty();
        }
    }

    public static Object firstMapValue(Map<?, ?> map, String... keys) {
        if (map == null || keys == null) {
            return null;
        }

        for (String key : keys) {
            Object value = map.get(key);

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    public static String normalizeRoleName(String roleName) {
        if (!StringUtils.hasText(roleName)) {
            return null;
        }

        String normalized = roleName.trim();

        if (normalized.regionMatches(true, 0, "ROLE_", 0, 5)) {
            normalized = normalized.substring(5);
        }

        return normalized.toUpperCase(Locale.ROOT);
    }
}