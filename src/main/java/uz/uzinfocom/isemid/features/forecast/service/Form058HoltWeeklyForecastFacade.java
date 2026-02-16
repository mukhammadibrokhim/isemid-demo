package uz.uzinfocom.isemid.features.forecast.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.forecast.models.dto.HoltWeeklyResponse;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class Form058HoltWeeklyForecastFacade {

    private final WeekBucketService bucketService;
    private final Form058WeeklyAggregationService aggregationService;
    private final HoltWeeklyCalculator calculator;

    public HoltWeeklyResponse holt(User currentUser,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   double alpha,
                                   double beta) {

        if (startDate == null) throw new IllegalArgumentException("startDate is required");
        if (endDate == null) endDate = bucketService.today();
        if (startDate.isAfter(endDate)) throw new IllegalArgumentException("startDate > endDate");

        var weekly = aggregationService.weeklyCounts(currentUser, startDate, endDate);

        return calculator.compute(weekly, alpha, beta);
    }
}
