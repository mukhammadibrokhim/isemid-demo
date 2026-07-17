package uz.uzinfocom.isemid.features.forecast.service;

import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.forecast.models.dto.HoltWintersResponse;
import uz.uzinfocom.isemid.features.forecast.service.Form058MonthlyAggregationService.MonthSeriesPoint;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class HoltWintersCalculator {

    public HoltWintersResponse compute(List<MonthSeriesPoint> monthly,
                                       double alpha, double beta, double gamma,
                                       int season, int horizon, boolean weekly) {
        validate(alpha, beta, gamma, season, horizon);

        if (monthly == null || monthly.size() < 2) {
            return new HoltWintersResponse(alpha, beta, gamma, season,
                    List.of(), List.of(), new HoltWintersResponse.Metrics(0.0, "Ёмон"));
        }

        int n = monthly.size();

        // Fewer than two full seasons: seasonal indices can't be estimated
        // reliably, and the recursion has too few steps to move away from
        // its crude initial values — the forecast ends up echoing the
        // historical seasonal shape instead of extrapolating it. Fall back
        // to plain Holt (level + trend only) instead.
        if (n < 2 * season) {
            return computeNonSeasonal(monthly, alpha, beta, gamma, season, horizon, weekly);
        }

        double[] y = new double[n];
        for (int i = 0; i < n; i++) y[i] = monthly.get(i).y();

        // ── Initialization ────────────────────────────────────────────────────
        // L_m  = avg(y_1..y_m)
        double sumFirst = 0;
        for (int i = 0; i < season; i++) sumFirst += y[i];
        double L = sumFirst / season;

        // T = (avg second season − avg first season) / m  [n >= 2*season is guaranteed here]
        double sumSecond = 0;
        for (int i = season; i < 2 * season; i++) sumSecond += y[i];
        double T = (sumSecond / season - L) / season;

        // S_t = y_t − L  for t = 1..m
        double[] S = new double[n];
        for (int i = 0; i < season; i++) S[i] = y[i] - L;

        // ── History: first season (initialization only) ───────────────────────
        List<HoltWintersResponse.Point> history = new ArrayList<>(n);
        for (int i = 0; i < season; i++) {
            // Initial L and T are shown at the last period of the first season
            Double initLevel = (i == season - 1) ? L : null;
            Double initTrend = (i == season - 1) ? T : null;
            history.add(new HoltWintersResponse.Point(
                    monthly.get(i).monthNo(),
                    monthly.get(i).from(),
                    monthly.get(i).to(),
                    monthly.get(i).y(),
                    null, null,
                    initLevel, initTrend,
                    S[i]
            ));
        }

        // ── Recursive Holt-Winters (additive) ────────────────────────────────
        double mapeSum = 0;
        int mCount = 0;

        for (int i = season; i < n; i++) {
            double yi = y[i];

            // F_t = L_{t-1} + T_{t-1} + S_{t-m}
            double forecast = L + T + S[i - season];

            // L_t = α*(y_t − S_{t-m}) + (1−α)*(L_{t-1} + T_{t-1})
            double newL = alpha * (yi - S[i - season]) + (1.0 - alpha) * (L + T);

            // T_t = β*(L_t − L_{t-1}) + (1−β)*T_{t-1}
            double newT = beta * (newL - L) + (1.0 - beta) * T;

            // S_t = γ*(y_t − L_t) + (1−γ)*S_{t-m}
            S[i] = gamma * (yi - newL) + (1.0 - gamma) * S[i - season];

            L = newL;
            T = newT;

            double error = yi - forecast;
            mapeSum += (yi != 0) ? Math.abs(error / yi) * 100.0 : 0.0;
            mCount++;

            history.add(new HoltWintersResponse.Point(
                    monthly.get(i).monthNo(),
                    monthly.get(i).from(),
                    monthly.get(i).to(),
                    monthly.get(i).y(),
                    forecast, error,
                    L, T, S[i]
            ));
        }

        double mape = (mCount == 0) ? 0.0 : mapeSum / mCount;

        // ── Future: F_{n+h} = L_n + h*T_n + S[n − m + ((h−1) % m)] ─────────
        List<HoltWintersResponse.FuturePoint> future = new ArrayList<>();
        if (horizon > 0) {
            LocalDate lastTo = monthly.get(n - 1).to();
            for (int h = 1; h <= horizon; h++) {
                Period p = nextPeriod(lastTo, h, weekly);
                int sIdx = n - season + ((h - 1) % season);

                // Point forecast clamped at 0 — case counts cannot be negative.
                double point = Math.max(0.0, L + h * T + S[sIdx]);

                future.add(new HoltWintersResponse.FuturePoint(h, p.from(), p.to(), point));
            }
        }

        return new HoltWintersResponse(alpha, beta, gamma, season, history, future,
                new HoltWintersResponse.Metrics(mape, quality(mape)));
    }

    // ── Plain Holt (level + trend, no seasonal component) ──────────────────
    // Used when there isn't enough data for a reliable seasonal estimate
    // (see the n < 2*season check in compute()). Needs only two points to
    // seed level/trend, so it can still plot short date ranges.
    private HoltWintersResponse computeNonSeasonal(List<MonthSeriesPoint> monthly,
                                                    double alpha, double beta, double gamma,
                                                    int season, int horizon, boolean weekly) {
        int n = monthly.size();
        double[] y = new double[n];
        for (int i = 0; i < n; i++) y[i] = monthly.get(i).y();

        double L = y[0];
        double T = y[1] - y[0];

        List<HoltWintersResponse.Point> history = new ArrayList<>(n);
        history.add(new HoltWintersResponse.Point(
                monthly.get(0).monthNo(), monthly.get(0).from(), monthly.get(0).to(),
                monthly.get(0).y(), null, null, L, T, null));

        double mapeSum = 0;
        int mCount = 0;

        for (int i = 1; i < n; i++) {
            double yi = y[i];

            // F_t = L_{t-1} + T_{t-1}
            double forecast = L + T;

            double newL = alpha * yi + (1.0 - alpha) * (L + T);
            double newT = beta * (newL - L) + (1.0 - beta) * T;
            L = newL;
            T = newT;

            double error = yi - forecast;
            mapeSum += (yi != 0) ? Math.abs(error / yi) * 100.0 : 0.0;
            mCount++;

            history.add(new HoltWintersResponse.Point(
                    monthly.get(i).monthNo(), monthly.get(i).from(), monthly.get(i).to(),
                    monthly.get(i).y(), forecast, error, L, T, null));
        }

        double mape = (mCount == 0) ? 0.0 : mapeSum / mCount;

        List<HoltWintersResponse.FuturePoint> future = new ArrayList<>();
        if (horizon > 0) {
            LocalDate lastTo = monthly.get(n - 1).to();
            for (int h = 1; h <= horizon; h++) {
                Period p = nextPeriod(lastTo, h, weekly);
                double point = Math.max(0.0, L + h * T);
                future.add(new HoltWintersResponse.FuturePoint(h, p.from(), p.to(), point));
            }
        }

        return new HoltWintersResponse(alpha, beta, gamma, season, history, future,
                new HoltWintersResponse.Metrics(mape, quality(mape)));
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

    private static void validate(double alpha, double beta, double gamma, int season, int horizon) {
        if (!(alpha > 0.0 && alpha < 1.0)) throw new IllegalArgumentException("alpha must be in (0,1)");
        if (!(beta  > 0.0 && beta  < 1.0)) throw new IllegalArgumentException("beta must be in (0,1)");
        if (!(gamma > 0.0 && gamma < 1.0)) throw new IllegalArgumentException("gamma must be in (0,1)");
        if (season < 2)                    throw new IllegalArgumentException("season must be >= 2");
        if (horizon < 0 || horizon > 120)  throw new IllegalArgumentException("horizon must be in [0..120]");
    }
}
