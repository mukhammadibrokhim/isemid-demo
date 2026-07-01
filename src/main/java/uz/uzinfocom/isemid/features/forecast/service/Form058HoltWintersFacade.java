package uz.uzinfocom.isemid.features.forecast.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.forecast.models.dto.HoltWintersResponse;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class Form058HoltWintersFacade {

    private final MonthBucketService bucketService;
    private final Form058MonthlyAggregationService aggregationService;
    private final HoltWintersCalculator calculator;

    public HoltWintersResponse holt(User currentUser,
                                    LocalDate startDate,
                                    LocalDate endDate,
                                    double alpha,
                                    double beta,
                                    double gamma,
                                    int season,
                                    int horizon,
                                    String stateCode,
                                    String mkb10Code) {
        if (startDate == null) throw new IllegalArgumentException("startDate is required");
        if (endDate == null) endDate = bucketService.today();
        if (startDate.isAfter(endDate)) throw new IllegalArgumentException("startDate > endDate");

        var monthly = aggregationService.monthlyCounts(currentUser, startDate, endDate, stateCode, mkb10Code);

        return calculator.compute(monthly, alpha, beta, gamma, season, horizon);
    }
}
