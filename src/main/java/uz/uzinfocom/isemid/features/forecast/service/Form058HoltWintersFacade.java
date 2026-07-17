package uz.uzinfocom.isemid.features.forecast.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.forecast.models.dto.HoltWintersResponse;
import uz.uzinfocom.isemid.features.forecast.service.Form058MonthlyAggregationService.MonthSeriesPoint;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Form058HoltWintersFacade {

    private final Form058MonthlyAggregationService monthlyAggregationService;
    private final Form058WeeklyAggregationService weeklyAggregationService;
    private final HoltWintersCalculator calculator;

    public HoltWintersResponse holt(User currentUser,
                                    LocalDate startDate,
                                    LocalDate endDate,
                                    double alpha,
                                    double beta,
                                    double gamma,
                                    Integer season,
                                    Integer horizon,
                                    String period,
                                    String stateCode,
                                    String mkb10Code) {
        if (startDate == null) throw new IllegalArgumentException("startDate is required");

        boolean weekly = "WEEKLY".equalsIgnoreCase(period);
        int effectiveSeason = season != null ? season : (weekly ? 52 : 12);
        int effectiveHorizon = horizon != null ? horizon : effectiveSeason;

        LocalDate end = endDate != null ? endDate : LocalDate.now();
        if (startDate.isAfter(end)) throw new IllegalArgumentException("startDate > endDate");

        // Fetch two extra seasons of history before the requested window so
        // the seasonal model always has enough context to fit properly,
        // regardless of how short a range the caller asked to see. The
        // response's history is trimmed back down to [startDate, end] below
        // — the extra data is only used to warm up the fit.
        LocalDate visibleFrom = weekly ? startDate : startDate.withDayOfMonth(1);
        LocalDate fetchStart = weekly
                ? startDate.minusDays(7L * 2 * effectiveSeason)
                : startDate.minusMonths(2L * effectiveSeason);

        List<MonthSeriesPoint> series;
        if (weekly) {
            series = weeklyAggregationService
                    .weeklyCounts(currentUser, fetchStart, end, stateCode, mkb10Code)
                    .stream()
                    .map(w -> new MonthSeriesPoint(w.weekNo(), w.from(), w.to(), w.y()))
                    .toList();
        } else {
            series = monthlyAggregationService.monthlyCounts(currentUser, fetchStart, end, stateCode, mkb10Code);
        }

        HoltWintersResponse result = calculator.compute(series, alpha, beta, gamma, effectiveSeason, effectiveHorizon, weekly);

        List<HoltWintersResponse.Point> visibleHistory = result.history().stream()
                .filter(p -> !p.from().isBefore(visibleFrom))
                .toList();

        return new HoltWintersResponse(result.alpha(), result.beta(), result.gamma(), result.season(),
                visibleHistory, result.future(), result.metrics());
    }
}
