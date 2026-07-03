package uz.uzinfocom.isemid.features.forecast.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.forecast.models.dto.HoltWintersResponse;
import uz.uzinfocom.isemid.features.forecast.service.Form058MonthlyAggregationService.MonthSeriesPoint;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HoltWintersCalculator {

    private final MessageUtil messageUtil;

    public HoltWintersResponse compute(List<MonthSeriesPoint> monthly,
                                       double alpha, double beta, double gamma,
                                       int season, int horizon, boolean weekly) {
        validate(alpha, beta, gamma, season, horizon);

        int n = (monthly == null) ? 0 : monthly.size();

        // Additive Holt-Winters needs two full seasons to initialize
        // (L/T from the first two seasons' means, S from the first season) —
        // anything short of that can't be fit reliably, so this is a client
        // error rather than something to silently degrade around.
        if (n < 2 * season) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    messageUtil.get("error.forecast.insufficient_data", n, 2 * season));
        }

        double[] y = new double[n];
        for (int i = 0; i < n; i++) y[i] = monthly.get(i).y();

        SeasonalFit fit = fitSeasonal(y, alpha, beta, gamma, season);

        List<HoltWintersResponse.Point> history = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Double forecast = Double.isNaN(fit.fitted()[i]) ? null : fit.fitted()[i];
            Double error = (forecast == null) ? null : y[i] - forecast;
            Double level = Double.isNaN(fit.level()[i]) ? null : fit.level()[i];
            Double trend = Double.isNaN(fit.trend()[i]) ? null : fit.trend()[i];
            history.add(new HoltWintersResponse.Point(
                    monthly.get(i).monthNo(),
                    monthly.get(i).from(),
                    monthly.get(i).to(),
                    monthly.get(i).y(),
                    forecast, error,
                    level, trend,
                    fit.seasonal()[i]
            ));
        }

        List<HoltWintersResponse.FuturePoint> future = new ArrayList<>();
        if (horizon > 0) {
            LocalDate lastTo = monthly.get(n - 1).to();
            double[] forecastValues = fit.forecast(horizon, season);
            for (int h = 1; h <= horizon; h++) {
                Period p = nextPeriod(lastTo, h, weekly);
                future.add(new HoltWintersResponse.FuturePoint(h, p.from(), p.to(), forecastValues[h - 1]));
            }
        }

        return new HoltWintersResponse(alpha, beta, gamma, season, history, future,
                new HoltWintersResponse.Metrics(fit.mape(), quality(fit.mape())));
    }

    // ── Core additive Holt-Winters recursion ────────────────────────────────
    // Extracted so it can be exercised directly with exact double-precision
    // series in tests (see HoltWintersExcelReferenceTest) — compute() only
    // ever sees whole case counts (long, from a DB COUNT()), but the
    // algorithm itself is unit-independent, and validating against an
    // externally-audited reference to +/-0.01 requires exact inputs rather
    // than values rounded through the production long-count path.
    // Package-private: same behavior as before, just factored out of compute().
    // Precondition (enforced by compute(), not re-checked here): y.length >= 2*season.
    SeasonalFit fitSeasonal(double[] y, double alpha, double beta, double gamma, int season) {
        int n = y.length;

        // ── Initialization ────────────────────────────────────────────────────
        // L_m  = avg(y_1..y_m)
        double sumFirst = 0;
        for (int i = 0; i < season; i++) sumFirst += y[i];
        double L = sumFirst / season;

        // T = (avg second season − avg first season) / m  [caller guarantees n >= 2*season]
        double sumSecond = 0;
        for (int i = season; i < 2 * season; i++) sumSecond += y[i];
        double T = (sumSecond / season - L) / season;

        // S_t = y_t − L  for t = 1..m
        double[] S = new double[n];
        for (int i = 0; i < season; i++) S[i] = y[i] - L;

        double[] fitted = new double[n];
        double[] level = new double[n];
        double[] trend = new double[n];
        for (int i = 0; i < season - 1; i++) {
            fitted[i] = Double.NaN;
            level[i] = Double.NaN;
            trend[i] = Double.NaN;
        }
        // Initial L and T are shown at the last period of the first season
        fitted[season - 1] = Double.NaN;
        level[season - 1] = L;
        trend[season - 1] = T;

        // ── Recursive Holt-Winters (additive) ────────────────────────────────
        double mapeSum = 0;
        int mCount = 0;

        for (int i = season; i < n; i++) {
            double yi = y[i];

            // F_t = L_{t-1} + T_{t-1} + S_{t-m}
            double forecast = L + T + S[i - season];
            fitted[i] = forecast;

            // L_t = α*(y_t − S_{t-m}) + (1−α)*(L_{t-1} + T_{t-1})
            double newL = alpha * (yi - S[i - season]) + (1.0 - alpha) * (L + T);

            // T_t = β*(L_t − L_{t-1}) + (1−β)*T_{t-1}
            double newT = beta * (newL - L) + (1.0 - beta) * T;

            // S_t = γ*(y_t − L_t) + (1−γ)*S_{t-m}
            S[i] = gamma * (yi - newL) + (1.0 - gamma) * S[i - season];

            L = newL;
            T = newT;
            level[i] = L;
            trend[i] = T;

            // Zero-actual points are skipped entirely (not counted as a
            // 0%-error point) — case counts can legitimately be zero, and
            // dividing by zero isn't just undefined, it would silently
            // understate the true error rate if treated as "no error".
            if (yi != 0) {
                double error = yi - forecast;
                mapeSum += Math.abs(error / yi) * 100.0;
                mCount++;
            }
        }

        double mape = (mCount == 0) ? 0.0 : mapeSum / mCount;
        return new SeasonalFit(fitted, level, trend, S, mape);
    }

    // fitted/level/trend hold NaN for the warm-up first season (except level
    // and trend at the last index, which carry their initial values).
    // seasonal holds S_t for every index, updated in place through t=n.
    record SeasonalFit(double[] fitted, double[] level, double[] trend, double[] seasonal, double mape) {

        // Yhat_{n+h} = L_n + h*T_n + S_{n+h-m}, i.e. the seasonal index for
        // the same calendar month h periods ago — matches nextPeriod's
        // month/week cadence, not an offset month.
        double[] forecast(int horizon, int season) {
            int n = seasonal.length;
            int last = level.length - 1;
            double lastLevel = level[last];
            double lastTrend = trend[last];
            double[] out = new double[horizon];
            for (int h = 1; h <= horizon; h++) {
                int sIdx = n - season + ((h - 1) % season);
                // Point forecast clamped at 0 — case counts cannot be negative.
                out[h - 1] = Math.max(0.0, lastLevel + h * lastTrend + seasonal[sIdx]);
            }
            return out;
        }
    }

    private record Period(LocalDate from, LocalDate to) {}

    // Future bucket boundaries must follow the same cadence as the historical
    // buckets (WeekBucketService / MonthBucketService) — otherwise weekly
    // series get monthly-spaced future dates, which desyncs the forecast
    // line from the chart's x-axis and makes it look like most periods
    // are missing.
    private static Period nextPeriod(LocalDate lastTo, int h, boolean weekly) {
        if (weekly) {
            LocalDate from = lastTo.plusDays(7L * (h - 1) + 1);
            LocalDate to = from.plusDays(6);
            return new Period(from, to);
        }
        YearMonth fm = YearMonth.from(lastTo).plusMonths(h);
        return new Period(fm.atDay(1), fm.atEndOfMonth());
    }

    private static String quality(double mape) {
        if (mape < 10) return "Аъло";
        if (mape < 20) return "Яхши";
        if (mape < 50) return "Қониқарли";
        return "Ёмон";
    }

    private void validate(double alpha, double beta, double gamma, int season, int horizon) {
        if (!(alpha > 0.0 && alpha < 1.0))
            throw new ApiException(HttpStatus.BAD_REQUEST, messageUtil.get("error.forecast.alpha_range"));
        if (!(beta > 0.0 && beta < 1.0))
            throw new ApiException(HttpStatus.BAD_REQUEST, messageUtil.get("error.forecast.beta_range"));
        if (!(gamma > 0.0 && gamma < 1.0))
            throw new ApiException(HttpStatus.BAD_REQUEST, messageUtil.get("error.forecast.gamma_range"));
        if (season < 2)
            throw new ApiException(HttpStatus.BAD_REQUEST, messageUtil.get("error.forecast.season_range"));
        if (horizon < 0 || horizon > 120)
            throw new ApiException(HttpStatus.BAD_REQUEST, messageUtil.get("error.forecast.horizon_range"));
    }
}
