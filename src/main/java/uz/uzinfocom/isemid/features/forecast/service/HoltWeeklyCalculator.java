package uz.uzinfocom.isemid.features.forecast.service;

import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.forecast.models.dto.HoltWeeklyResponse;
import uz.uzinfocom.isemid.features.forecast.service.Form058MonthlyAggregationService.MonthSeriesPoint;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class HoltWeeklyCalculator {

    public HoltWeeklyResponse compute(List<MonthSeriesPoint> monthly,
                                      double alpha, double beta, double gamma,
                                      int season, int horizon) {
        validate(alpha, beta, gamma, season, horizon);

        if (monthly == null || monthly.size() < season) {
            return new HoltWeeklyResponse(alpha, beta, gamma, season,
                    List.of(), List.of(), new HoltWeeklyResponse.Metrics(0.0, "Ёмон"));
        }

        int n = monthly.size();
        double[] y = new double[n];
        for (int i = 0; i < n; i++) y[i] = monthly.get(i).y();

        // ── Initialization ──────────────────────────────────────────────────────
        // Initial level: average of first season
        double sumFirst = 0;
        for (int i = 0; i < season; i++) sumFirst += y[i];
        double L = sumFirst / season;

        // Initial trend: (avg second season − avg first season) / season
        double T = 0;
        if (n >= 2 * season) {
            double sumSecond = 0;
            for (int i = season; i < 2 * season; i++) sumSecond += y[i];
            T = (sumSecond / season - L) / season;
        }

        // Initial seasonal indices: S_t = y_t − L  for t = 1..season
        double[] S = new double[n];
        for (int i = 0; i < season; i++) S[i] = y[i] - L;

        // ── Build history (first season — no forecast) ───────────────────────────
        List<HoltWeeklyResponse.Point> history = new ArrayList<>(n);
        for (int i = 0; i < season; i++) {
            // Show initial L/T only at the last period of the first season (mirrors Excel)
            Double initLevel = (i == season - 1) ? L : null;
            Double initTrend = (i == season - 1) ? T : null;
            history.add(new HoltWeeklyResponse.Point(
                    monthly.get(i).monthNo(),
                    monthly.get(i).from(),
                    monthly.get(i).to(),
                    monthly.get(i).y(),
                    null, null,
                    initLevel, initTrend,
                    S[i]
            ));
        }

        // ── Recursive Holt-Winters (additive) ───────────────────────────────────
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
            double ape = (yi != 0) ? Math.abs(error / yi) * 100.0 : 0.0;
            mapeSum += ape;
            mCount++;

            history.add(new HoltWeeklyResponse.Point(
                    monthly.get(i).monthNo(),
                    monthly.get(i).from(),
                    monthly.get(i).to(),
                    monthly.get(i).y(),
                    forecast,
                    error,
                    L, T,
                    S[i]
            ));
        }

        double mape = (mCount == 0) ? 0.0 : mapeSum / mCount;

        // ── Future forecast ──────────────────────────────────────────────────────
        // F_{n+h} = L_n + h*T_n + S[n − season + ((h−1) % season)]
        List<HoltWeeklyResponse.FuturePoint> future = new ArrayList<>();
        if (horizon > 0) {
            YearMonth lastMonth = YearMonth.from(monthly.get(n - 1).to());
            for (int h = 1; h <= horizon; h++) {
                YearMonth fm = lastMonth.plusMonths(h);
                LocalDate from = fm.atDay(1);
                LocalDate to = fm.atEndOfMonth();
                int sIdx = n - season + ((h - 1) % season);
                future.add(new HoltWeeklyResponse.FuturePoint(h, from, to, L + h * T + S[sIdx]));
            }
        }

        return new HoltWeeklyResponse(alpha, beta, gamma, season, history, future,
                new HoltWeeklyResponse.Metrics(mape, quality(mape)));
    }

    private static String quality(double mape) {
        if (mape < 10) return "Аъло";
        if (mape < 20) return "Яхши";
        if (mape < 50) return "Қониқарли";
        return "Ёмон";
    }

    private static void validate(double alpha, double beta, double gamma, int season, int horizon) {
        if (!(alpha > 0.0 && alpha < 1.0)) throw new IllegalArgumentException("alpha must be in (0,1)");
        if (!(beta > 0.0 && beta < 1.0)) throw new IllegalArgumentException("beta must be in (0,1)");
        if (!(gamma > 0.0 && gamma < 1.0)) throw new IllegalArgumentException("gamma must be in (0,1)");
        if (season < 2) throw new IllegalArgumentException("season must be >= 2");
        if (horizon < 0 || horizon > 120) throw new IllegalArgumentException("horizon must be in [0..120]");
    }
}
