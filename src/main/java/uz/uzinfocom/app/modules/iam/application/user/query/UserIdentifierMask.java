package uz.uzinfocom.app.modules.iam.application.user.query;

/**
 * Hides a staff member's national identifier (JSHSHIR/PINFL) from callers who have
 * no reason to see it in full: only the last four characters stay readable.
 */
public final class UserIdentifierMask {

    private static final int VISIBLE_TAIL = 4;

    private UserIdentifierMask() {
    }

    public static String mask(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return identifier;
        }
        String trimmed = identifier.trim();
        if (trimmed.length() <= VISIBLE_TAIL) {
            return "*".repeat(trimmed.length());
        }
        return "*".repeat(trimmed.length() - VISIBLE_TAIL) + trimmed.substring(trimmed.length() - VISIBLE_TAIL);
    }
}
