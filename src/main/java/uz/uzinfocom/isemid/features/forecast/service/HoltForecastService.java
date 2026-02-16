package uz.uzinfocom.isemid.features.forecast.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class HoltForecastService {

    public record SeriesPoint(
            int weekNo,
            LocalDate from,
            LocalDate to,
            long y,
            Double yHat,   // ŷ_t
            Double error,  // e_t = y - ŷ
            Double level,  // L_t
            Double trend   // T_t
    ) {}

    public record FuturePoint(int h, LocalDate from, LocalDate to, double yHat) {}

    public record Metrics(double mae, double rmse, double mape) {}

    public record Result(List<SeriesPoint> series, List<FuturePoint> future, Metrics metrics) {}

    public Result forecast(List<Form058WeeklyAggregationService.WeekSeriesPoint> weekly,
                           double alpha, double beta, int horizon) {

        validate(alpha, beta, horizon);

        if (weekly == null || weekly.isEmpty()) {
            return new Result(List.of(), List.of(), new Metrics(0, 0, 0));
        }

        int n = weekly.size();

        // init: L1=y1, T1=y2-y1 (y2 yo'q bo'lsa 0)
        double L = weekly.get(0).y();
        double T = (n >= 2) ? (weekly.get(1).y() - weekly.get(0).y()) : 0.0;

        List<SeriesPoint> series = new ArrayList<>(n);

        // t=1 (ŷ1 yo'q)
        series.add(new SeriesPoint(
                weekly.get(0).weekNo(),
                weekly.get(0).from(),
                weekly.get(0).to(),
                weekly.get(0).y(),
                null, null,
                L, T
        ));

        // metrics accumulators (t>=2)
        double absSum = 0, sqSum = 0, mapeSum = 0;
        int mCount = 0;

        for (int i = 1; i < n; i++) {
            long y = weekly.get(i).y();

            // ŷ_t = L_{t-1} + T_{t-1}
            double yHat = L + T;

            // e_t = y_t - ŷ_t
            double e = y - yHat;

            // L_t = α*y_t + (1-α)*(L_{t-1}+T_{t-1})
            double newL = alpha * y + (1 - alpha) * (L + T);

            // T_t = β*(L_t - L_{t-1}) + (1-β)*T_{t-1}
            double newT = beta * (newL - L) + (1 - beta) * T;

            // update state
            L = newL;
            T = newT;

            series.add(new SeriesPoint(
                    weekly.get(i).weekNo(),
                    weekly.get(i).from(),
                    weekly.get(i).to(),
                    y,
                    yHat,
                    e,
                    L,
                    T
            ));

            absSum += Math.abs(e);
            sqSum += e * e;
            // MAPE: y=0 bo'lsa division risk -> max(1,y)
            mapeSum += Math.abs(e) / Math.max(1.0, (double) y);
            mCount++;
        }

        double mae = (mCount == 0) ? 0 : absSum / mCount;
        double rmse = (mCount == 0) ? 0 : Math.sqrt(sqSum / mCount);
        double mape = (mCount == 0) ? 0 : mapeSum / mCount;

        // future: ŷ_{n+h} = L_n + h*T_n
        List<FuturePoint> future = new ArrayList<>();
        if (horizon > 0) {
            var last = weekly.get(n - 1);
            LocalDate baseFrom = last.to().plusDays(1);

            for (int h = 1; h <= horizon; h++) {
                LocalDate from = baseFrom.plusDays((long) (h - 1) * 7);
                LocalDate to = from.plusDays(6);
                double fh = L + h * T;
                future.add(new FuturePoint(h, from, to, fh));
            }
        }

        return new Result(series, future, new Metrics(mae, rmse, mape));
    }

    private void validate(double alpha, double beta, int horizon) {
        if (!(alpha > 0.0 && alpha < 1.0)) throw new IllegalArgumentException("alpha must be in (0,1)");
        if (!(beta > 0.0 && beta < 1.0)) throw new IllegalArgumentException("beta must be in (0,1)");
        if (horizon < 0 || horizon > 104) throw new IllegalArgumentException("horizon must be in [0..104]");
    }
}
