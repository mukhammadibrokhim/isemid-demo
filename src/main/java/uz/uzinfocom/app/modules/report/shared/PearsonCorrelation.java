package uz.uzinfocom.app.modules.report.shared;

/**
 * Pearson product-moment correlation of two equally long series, with the reading of its size
 * that the reports show next to it. Pure arithmetic - no state, no Spring.
 */
public final class PearsonCorrelation {

    /** Below this many paired points a coefficient is arithmetic, not evidence. */
    public static final int RELIABLE_MIN_PAIRS = 14;

    private static final int MIN_PAIRS = 3;

    private PearsonCorrelation() {
    }

    /** How strong the relationship is, by |r|: below 0.1 none, 0.1 weak, 0.3 moderate, 0.5 strong. */
    public enum Strength {
        NONE, WEAK, MODERATE, STRONG;

        public static Strength of(double coefficient) {
            double size = Math.abs(coefficient);
            if (size >= 0.5) {
                return STRONG;
            }
            if (size >= 0.3) {
                return MODERATE;
            }
            if (size >= 0.1) {
                return WEAK;
            }
            return NONE;
        }
    }

    /**
     * @param coefficient {@code r} in [-1, 1]; {@code null} when it is undefined (fewer than three
     *                    pairs, or one of the series does not vary)
     * @param pairs       how many pairs were used
     */
    public record Result(Double coefficient, int pairs) {

        /** Enough pairs for the coefficient to mean something. */
        public boolean reliable() {
            return coefficient != null && pairs >= RELIABLE_MIN_PAIRS;
        }

        public Strength strength() {
            return coefficient == null ? null : Strength.of(coefficient);
        }
    }

    public static Result compute(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Series must have the same length");
        }
        int n = x.length;
        if (n < MIN_PAIRS) {
            return new Result(null, n);
        }
        double meanX = 0;
        double meanY = 0;
        for (int i = 0; i < n; i++) {
            meanX += x[i];
            meanY += y[i];
        }
        meanX /= n;
        meanY /= n;

        double covariance = 0;
        double varianceX = 0;
        double varianceY = 0;
        for (int i = 0; i < n; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            covariance += dx * dy;
            varianceX += dx * dx;
            varianceY += dy * dy;
        }
        if (varianceX == 0 || varianceY == 0) {
            return new Result(null, n);
        }
        double r = covariance / Math.sqrt(varianceX * varianceY);
        // Floating point can overshoot by an ulp on perfectly (anti)correlated data.
        return new Result(Math.max(-1d, Math.min(1d, r)), n);
    }
}
