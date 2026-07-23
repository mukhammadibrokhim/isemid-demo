package uz.uzinfocom.app.integration.lis.common.support;

/**
 * Trims and length-caps anything LIS sends back before it reaches a log line,
 * an API response, or the {@code lisInfo.lastError} column — upstream text is
 * untrusted input and an error body can be an entire HTML page.
 *
 * <p>Mirrors the intent of {@code Api2ResponseBodySanitizer}, minus the
 * content-type handling: here it is only ever applied to short error strings.
 */
public final class LisResponseSanitizer {

    private static final int MAX_LENGTH = 500;

    private LisResponseSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }

        String collapsed = value.replaceAll("\\s+", " ").trim();
        if (collapsed.isEmpty()) {
            return null;
        }

        return collapsed.length() <= MAX_LENGTH
                ? collapsed
                : collapsed.substring(0, MAX_LENGTH) + "…";
    }
}
