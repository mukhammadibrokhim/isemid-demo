package uz.uzinfocom.app.integration.dhp.common.support;

import uz.uzinfocom.app.integration.dhp.common.exception.DhpInvalidNiException;

import java.util.regex.Pattern;

public final class DhpNiValidator {

    private static final Pattern NI_PATTERN = Pattern.compile("\\d{14}");

    private DhpNiValidator() {
    }

    /** Returns the trimmed NNUZB (PINFL); throws {@link DhpInvalidNiException} unless it is exactly 14 digits. */
    public static String validate(String rawNi) {
        String ni = rawNi == null ? null : rawNi.trim();

        if (ni == null || !NI_PATTERN.matcher(ni).matches()) {
            throw new DhpInvalidNiException();
        }

        return ni;
    }
}
