package uz.uzinfocom.app.platform.reference.application.common;

import uz.uzinfocom.app.shared.exception.ConflictException;

import java.util.Locale;

public final class ReferenceCodeNormalizer {

    private ReferenceCodeNormalizer() {
    }

    public static String normalizeCode(String code) {
        return normalize(code, "reference.code.required");
    }

    public static String normalizeParentCode(String parentCode) {
        return normalize(parentCode, "reference.parent_code.required");
    }

    private static String normalize(String value, String requiredMessageCode) {
        if (value == null) {
            throw new ConflictException(requiredMessageCode);
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);

        if (normalized.isBlank()) {
            throw new ConflictException(requiredMessageCode);
        }

        return normalized;
    }
}
