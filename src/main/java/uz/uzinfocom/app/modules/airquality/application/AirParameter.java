package uz.uzinfocom.app.modules.airquality.application;

import java.util.Locale;

/**
 * Normalizes AIR_CONTROL pollutant names into stable codes: the agreement's own sample
 * mixes spacing ({@code "PM 2.5"}, {@code "NOx-2"}), so {@code "PM 2.5"} and
 * {@code "pm2.5"} must land on the same code or the unique key and every series
 * query would split one pollutant in two.
 */
public final class AirParameter {

    private static final int MAX_LENGTH = 30;

    private AirParameter() {
    }

    /** Upper-cased with all whitespace removed ({@code "PM 2.5"} → {@code "PM2.5"}); {@code null} for blank input. */
    public static String normalize(String name) {
        if (name == null) {
            return null;
        }
        String code = name.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if (code.isEmpty()) {
            return null;
        }
        return code.length() > MAX_LENGTH ? code.substring(0, MAX_LENGTH) : code;
    }
}
