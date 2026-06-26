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
                                       int season, int horizon) {
        validate(alpha, beta, gamma, season, horizon);

        if (monthly == null || monthly.size() < season) {
            return new HoltWintersResponse(alpha, beta, gamma, season,
                    List.of(), List.of(), new HoltWintersResponse.Metrics(0.0, "Ёмон"));
        }

        int n = monthly.size();
        double[] y = new double[n];
        for (int i = 0; i < n; i++) y[i] = monthly.get(i).y();

        // ── Initialization ────────────────────────────────────────────────────
        // L_m  = avg(y_1..y_m)
        double sumFirst = 0;
        for (int i = 0; i < season; i++) sumFirst += y[i];
        double L = sumFirst / season;

        // T = (avg second season − avg first season) / m  [requires ≥ 2 seasons]
        double T = 0;
        if (n >= 2 * season) {
            double sumSecond = 0;
            for (int i = season; i < 2 * season; i++) sumSecond += y[i];
            T = (sumSecond / season - L) / season;
        }

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
            YearMonth lastMonth = YearMonth.from(monthly.get(n - 1).to());
            for (int h = 1; h <= horizon; h++) {
                YearMonth fm = lastMonth.plusMonths(h);
                LocalDate from = fm.atDay(1);
                LocalDate to   = fm.atEndOfMonth();
                int sIdx = n - season + ((h - 1) % season);
                future.add(new HoltWintersResponse.FuturePoint(h, from, to, L + h * T + S[sIdx]));
            }
        }

        return new HoltWintersResponse(alpha, beta, gamma, season, history, future,
                new HoltWintersResponse.Metrics(mape, quality(mape)));
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
