package uz.uzinfocom.isemid.features.forecast.service;

import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.forecast.models.dto.HoltWeeklyResponse;
import uz.uzinfocom.isemid.features.forecast.service.Form058WeeklyAggregationService.WeekSeriesPoint;

import java.util.ArrayList;
import java.util.List;

@Service
public class HoltWeeklyCalculator {

    public HoltWeeklyResponse compute(List<WeekSeriesPoint> weekly, double alpha, double beta) {
        validate(alpha, beta);

        if (weekly == null || weekly.isEmpty()) {
            return new HoltWeeklyResponse(alpha, beta, List.of());
        }

        int n = weekly.size();

        // INIT:
        // L1 = y1
        // T1 = y2 - y1  (y2 bo'lmasa 0)
        double L = weekly.get(0).y();
        double T = (n >= 2) ? (weekly.get(1).y() - weekly.get(0).y()) : 0.0;

        List<HoltWeeklyResponse.Point> out = new ArrayList<>(n);

        // t=1: yHat/error = null
        out.add(new HoltWeeklyResponse.Point(
                weekly.get(0).weekNo(),
                weekly.get(0).from(),
                weekly.get(0).to(),
                weekly.get(0).y(),
                null,
                null,
                L,
                T
        ));

        for (int i = 1; i < n; i++) {
            long y = weekly.get(i).y();

            // ŷ_t = L_{t-1} + T_{t-1}
            double yHat = L + T;

            // e_t = y_t - ŷ_t
            double error = y - yHat;

            // L_t = α*y_t + (1-α)*(L_{t-1}+T_{t-1})
            double newL = alpha * y + (1.0 - alpha) * (L + T);

            // T_t = β*(L_t - L_{t-1}) + (1-β)*T_{t-1}
            double newT = beta * (newL - L) + (1.0 - beta) * T;

            // update state
            L = newL;
            T = newT;

            out.add(new HoltWeeklyResponse.Point(
                    weekly.get(i).weekNo(),
                    weekly.get(i).from(),
                    weekly.get(i).to(),
                    y,
                    yHat,
                    error,
                    L,
                    T
            ));
        }

        return new HoltWeeklyResponse(alpha, beta, out);
    }

    private void validate(double alpha, double beta) {
        if (!(alpha > 0.0 && alpha < 1.0)) throw new IllegalArgumentException("alpha must be in (0,1)");
        if (!(beta > 0.0 && beta < 1.0)) throw new IllegalArgumentException("beta must be in (0,1)");
    }
}
