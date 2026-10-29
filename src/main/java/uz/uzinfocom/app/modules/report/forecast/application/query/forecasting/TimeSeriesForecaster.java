package uz.uzinfocom.app.modules.report.forecast.application.query.forecasting;

import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastMethod;

/**
 * Self-contained classical time-series extrapolation — no Spring, no
 * external libraries, deterministic. Given a gap-filled history of
 * non-negative counts it produces an {@code h}-step-ahead point forecast
 * plus a symmetric prediction band derived from the model's own in-sample
 * one-step-ahead error.
 *
 * <p>Smoothing constants are fixed (no per-series maximum-likelihood
 * optimisation): the defaults below are the textbook "reasonable without
 * tuning" values and keep every call O(n). Counts are non-negative, so every
 * produced point and lower bound is clamped at zero.
 *
 * <p>Models, in increasing order of data appetite: {@link
 * ForecastMethod#NAIVE_MEAN} → {@link ForecastMethod#SES} → {@link
 * ForecastMethod#HOLT} → {@link ForecastMethod#HOLT_WINTERS_ADDITIVE}.
 */
public final class TimeSeriesForecaster {

    private static final double ALPHA = 0.3;   // level smoothing
    private static final double BETA = 0.1;    // trend smoothing
    private static final double GAMMA = 0.3;   // seasonal smoothing
    private static final double Z_95 = 1.96;   // ~95% prediction band

    private TimeSeriesForecaster() {
    }

    /**
     * @param history      gap-filled bucket counts, oldest first, length ≥ 1
     * @param horizon      number of future buckets to predict, ≥ 1
     * @param seasonLength buckets per seasonal cycle (7 / 52 / 12); ignored unless a seasonal model runs
     * @param requested    a specific model, or {@link ForecastMethod#AUTO}
     */
    public static Result forecast(double[] history, int horizon, int seasonLength, ForecastMethod requested) {
        ForecastMethod method = requested == ForecastMethod.AUTO
                ? resolveAuto(history.length, seasonLength)
                : requested;

        Fit fit = switch (method) {
            case NAIVE_MEAN, AUTO -> naiveMean(history, horizon);
            case SES -> ses(history, horizon);
            case HOLT -> holt(history, horizon);
            case HOLT_WINTERS_ADDITIVE -> holtWinters(history, horizon, seasonLength);
        };

        double sigma = residualStdDev(fit.oneStepErrors(), history);

        double[] point = new double[horizon];
        double[] lower = new double[horizon];
        double[] upper = new double[horizon];
        for (int h = 1; h <= horizon; h++) {
            double p = Math.max(0.0, fit.future()[h - 1]);
            double band = Z_95 * sigma * Math.sqrt(h);
            point[h - 1] = p;
            lower[h - 1] = Math.max(0.0, p - band);
            upper[h - 1] = p + band;
        }
        return new Result(method, point, lower, upper, sigma);
    }

    /** Point + band for every horizon step, plus the model actually used and its residual σ. */
    public record Result(ForecastMethod method, double[] point, double[] lower, double[] upper, double residualStdDev) {
    }

    private record Fit(double[] future, double[] oneStepErrors) {
    }

    private static ForecastMethod resolveAuto(int n, int seasonLength) {
        if (n < 3) {
            return ForecastMethod.NAIVE_MEAN;
        }
        if (seasonLength >= 2 && n >= 2 * seasonLength) {
            return ForecastMethod.HOLT_WINTERS_ADDITIVE;
        }
        if (n >= 4) {
            return ForecastMethod.HOLT;
        }
        return ForecastMethod.SES;
    }

    private static Fit naiveMean(double[] y, int horizon) {
        int n = y.length;
        double[] errors = new double[Math.max(0, n - 1)];
        double runningSum = y[0];
        for (int i = 1; i < n; i++) {
            double pred = runningSum / i;
            errors[i - 1] = y[i] - pred;
            runningSum += y[i];
        }
        double mean = runningSum / n;
        return new Fit(filled(horizon, mean), errors);
    }

    private static Fit ses(double[] y, int horizon) {
        int n = y.length;
        double level = y[0];
        double[] errors = new double[Math.max(0, n - 1)];
        for (int i = 1; i < n; i++) {
            double err = y[i] - level;
            errors[i - 1] = err;
            level += ALPHA * err;
        }
        return new Fit(filled(horizon, level), errors);
    }

    private static Fit holt(double[] y, int horizon) {
        int n = y.length;
        double level = y[0];
        double trend = n > 1 ? y[1] - y[0] : 0.0;
        double[] errors = new double[Math.max(0, n - 1)];
        for (int i = 1; i < n; i++) {
            double pred = level + trend;
            double err = y[i] - pred;
            errors[i - 1] = err;
            level = pred + ALPHA * err;
            trend = trend + BETA * (ALPHA * err);
        }
        double[] future = new double[horizon];
        for (int h = 1; h <= horizon; h++) {
            future[h - 1] = level + h * trend;
        }
        return new Fit(future, errors);
    }

    private static Fit holtWinters(double[] y, int horizon, int m) {
        int n = y.length;
        if (m < 2 || n < 2 * m) {
            return holt(y, horizon);
        }

        double firstCycleMean = mean(y, 0, m);
        double secondCycleMean = mean(y, m, 2 * m);
        double level = firstCycleMean;
        double trend = (secondCycleMean - firstCycleMean) / m;

        double[] seasonal = new double[m];
        for (int i = 0; i < m; i++) {
            seasonal[i] = y[i] - firstCycleMean;
        }

        double[] errors = new double[n - m];
        for (int t = m; t < n; t++) {
            int s = t % m;
            double pred = level + trend + seasonal[s];
            double err = y[t] - pred;
            errors[t - m] = err;

            double newLevel = ALPHA * (y[t] - seasonal[s]) + (1 - ALPHA) * (level + trend);
            double newTrend = BETA * (newLevel - level) + (1 - BETA) * trend;
            seasonal[s] = GAMMA * (y[t] - newLevel) + (1 - GAMMA) * seasonal[s];
            level = newLevel;
            trend = newTrend;
        }

        double[] future = new double[horizon];
        for (int h = 1; h <= horizon; h++) {
            int s = (n - 1 + h) % m;
            future[h - 1] = level + h * trend + seasonal[s];
        }
        return new Fit(future, errors);
    }

    /**
     * Sample standard deviation of the in-sample one-step errors — the width
     * unit of the prediction band. Falls back to a Poisson-style {@code
     * sqrt(mean count)} when there are too few errors to estimate a spread
     * (and to a floor of 1 so a dead-flat history still yields a visible
     * band).
     */
    private static double residualStdDev(double[] errors, double[] history) {
        if (errors.length >= 2) {
            double mean = mean(errors, 0, errors.length);
            double ss = 0.0;
            for (double e : errors) {
                ss += (e - mean) * (e - mean);
            }
            double sd = Math.sqrt(ss / (errors.length - 1));
            if (sd > 0.0 && !Double.isNaN(sd)) {
                return sd;
            }
        }
        double meanCount = mean(history, 0, history.length);
        return Math.max(1.0, Math.sqrt(Math.max(0.0, meanCount)));
    }

    private static double[] filled(int length, double value) {
        double[] out = new double[length];
        java.util.Arrays.fill(out, value);
        return out;
    }

    private static double mean(double[] values, int fromInclusive, int toExclusive) {
        double sum = 0.0;
        for (int i = fromInclusive; i < toExclusive; i++) {
            sum += values[i];
        }
        return sum / (toExclusive - fromInclusive);
    }
}
