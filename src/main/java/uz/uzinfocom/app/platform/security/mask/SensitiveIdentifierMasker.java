package uz.uzinfocom.app.platform.security.mask;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;

import java.util.Locale;
import java.util.Set;

/**
 * Single place that decides whether a personal identifier is shown in full.
 *
 * <p>Identifiers of the types in {@link #SENSITIVE_TYPES} are masked in every API response
 * (first three and last three characters stay readable, e.g. {@code 31234567890123} becomes
 * {@code 312********123}); only {@code isemid_super_admin} sees them in full. Identifiers of any
 * other type are returned untouched.
 */
@Component
@RequiredArgsConstructor
public class SensitiveIdentifierMasker {

    public static final Set<String> SENSITIVE_TYPES = Set.of(
            "PPN", "TAX", "FPPN", "DL", "MD", "BCT", "REO", "CZ", "TPPN", "NNUZB"
    );

    private static final int VISIBLE_HEAD = 3;
    private static final int VISIBLE_TAIL = 3;

    private final AdminAccessGuard adminAccessGuard;

    public static boolean isSensitiveType(String typeCode) {
        return typeCode != null && SENSITIVE_TYPES.contains(typeCode.trim().toUpperCase(Locale.ROOT));
    }

    /** Pure masking of a value, regardless of type or caller. */
    public static String maskValue(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String trimmed = value.trim();
        int length = trimmed.length();
        if (length <= VISIBLE_HEAD + VISIBLE_TAIL) {
            return "*".repeat(length);
        }
        return trimmed.substring(0, VISIBLE_HEAD)
                + "*".repeat(length - VISIBLE_HEAD - VISIBLE_TAIL)
                + trimmed.substring(length - VISIBLE_TAIL);
    }

    /** True when the echoed value is exactly the masked form of the stored one (a client round-trip). */
    public static boolean isMaskedFormOf(String candidate, String stored) {
        return candidate != null && stored != null && candidate.trim().equals(maskValue(stored));
    }

    /** Masks {@code value} when {@code typeCode} is sensitive and the caller is not a super administrator. */
    public String mask(String typeCode, String value) {
        if (!isSensitiveType(typeCode) || adminAccessGuard.isSuperAdmin()) {
            return value;
        }
        return maskValue(value);
    }

    /** Masks a value whose type is fixed (e.g. a dedicated {@code nnuzb} field) for non-super-admins. */
    public String maskForCaller(String value) {
        return adminAccessGuard.isSuperAdmin() ? value : maskValue(value);
    }
}
