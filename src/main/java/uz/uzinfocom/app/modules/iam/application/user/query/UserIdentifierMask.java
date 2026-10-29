package uz.uzinfocom.app.modules.iam.application.user.query;

/**
 * Hides a staff member's national identifier (JSHSHIR/PINFL) from callers who have
 * no reason to see it in full: only the first three and last three characters stay
 * readable, e.g. {@code 31234567890123} becomes {@code 312********123}.
 */
public final class UserIdentifierMask {

    private static final int VISIBLE_HEAD = 3;
    private static final int VISIBLE_TAIL = 3;

    private UserIdentifierMask() {
    }

    public static String mask(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return identifier;
        }
        String trimmed = identifier.trim();
        int length = trimmed.length();
        if (length <= VISIBLE_HEAD + VISIBLE_TAIL) {
            return "*".repeat(length);
        }
        return trimmed.substring(0, VISIBLE_HEAD)
                + "*".repeat(length - VISIBLE_HEAD - VISIBLE_TAIL)
                + trimmed.substring(length - VISIBLE_TAIL);
    }
}
