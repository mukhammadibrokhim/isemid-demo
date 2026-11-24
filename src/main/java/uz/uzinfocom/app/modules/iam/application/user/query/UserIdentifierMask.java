package uz.uzinfocom.app.modules.iam.application.user.query;

import uz.uzinfocom.app.platform.security.mask.SensitiveIdentifierMasker;

/**
 * Hides a staff member's national identifier (JSHSHIR/PINFL) from callers who have
 * no reason to see it in full: only the first three and last three characters stay
 * readable, e.g. {@code 31234567890123} becomes {@code 312********123}.
 */
public final class UserIdentifierMask {

    private UserIdentifierMask() {
    }

    public static String mask(String identifier) {
        return SensitiveIdentifierMasker.maskValue(identifier);
    }
}
