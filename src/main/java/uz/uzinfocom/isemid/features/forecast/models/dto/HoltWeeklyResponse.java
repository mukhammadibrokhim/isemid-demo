package uz.uzinfocom.isemid.features.forecast.models.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record HoltWeeklyResponse(
        double alpha,
        double beta,
        List<Point> points
) {
    public record Point(
            int weekNo,
            LocalDate from,
            LocalDate to,
            long y,
            Double yHat,
            Double error,
            Double level,
            Double trend
    ) {
    }
}