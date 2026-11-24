package uz.uzinfocom.app.modules.airquality.application;

import java.math.BigDecimal;

/**
 * How a pollutant reading (or a whole station) compares with its norm. Derived from
 * {@code value / limit}: up to half the limit is {@link #GOOD}, up to the limit
 * {@link #MODERATE}, up to twice the limit {@link #EXCEEDED}, above that {@link #SEVERE}.
 * {@link #NO_DATA} when there is no reading, no norm for the pollutant, or a unit that
 * cannot be converted to micrograms per cubic metre.
 */
public enum AirStatus {
    NO_DATA,
    GOOD,
    MODERATE,
    EXCEEDED,
    SEVERE;

    private static final BigDecimal HALF = new BigDecimal("0.5");
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal TWO = new BigDecimal("2");

    public static AirStatus fromRatio(BigDecimal ratio) {
        if (ratio == null) {
            return NO_DATA;
        }
        if (ratio.compareTo(HALF) <= 0) {
            return GOOD;
        }
        if (ratio.compareTo(ONE) <= 0) {
            return MODERATE;
        }
        if (ratio.compareTo(TWO) <= 0) {
            return EXCEEDED;
        }
        return SEVERE;
    }

    /** {@code true} for the two statuses above the norm. */
    public boolean isAboveNorm() {
        return this == EXCEEDED || this == SEVERE;
    }

    /** Higher is worse; {@link #NO_DATA} ranks lowest so it never hides a real reading. */
    public int severity() {
        return ordinal();
    }
}
