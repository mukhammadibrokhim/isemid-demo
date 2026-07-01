package uz.uzinfocom.isemid.features.forecast.models.dto;

import java.time.LocalDate;
import java.util.List;

public record HoltWintersResponse(
        double alpha,
        double beta,
        double gamma,
        int season,
        List<Point> history,
        List<FuturePoint> future,
        Metrics metrics
) {
    public record Point(
            int periodNo,
            LocalDate from,
            LocalDate to,
            long y,
            Double forecast,
            Double error,
            Double level,
            Double trend,
            Double seasonal
    ) {}

    public record FuturePoint(
            int h,
            LocalDate from,
            LocalDate to,
            double forecast
    ) {}

    public record Metrics(
            double mape,
            String quality
    ) {}
}
