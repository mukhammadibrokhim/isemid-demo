package uz.uzinfocom.isemid.features.forecast.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.forecast.models.projection.DayCountProjection;
import uz.uzinfocom.isemid.features.forecast.repositories.Form058ForecastRepository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Form058MonthlyAggregationService {

    private final Form058ForecastRepository repo;
    private final MonthBucketService bucketService;

    public record MonthSeriesPoint(int monthNo, LocalDate from, LocalDate to, long y) {}

    public List<MonthSeriesPoint> monthlyCounts(User currentUser, LocalDate startDate, LocalDate endDate,
                                                String stateCode, String mkb10Code) {
        if (startDate == null) throw new IllegalArgumentException("startDate is required");
        if (endDate == null) endDate = bucketService.today();
        if (startDate.isAfter(endDate)) return List.of();

        String effectiveStateCode = normalize(stateCode);
        if (effectiveStateCode == null) {
            effectiveStateCode = normalize(resolveUserStateCode(currentUser));
        }
        String effectiveMkb10 = normalize(mkb10Code);

        // Don't zero-fill buckets from before the org/state has any recorded
        // data at all — those synthetic zeros would drag the model's level
        // and seasonal estimates down and mislead the forecast. Only the
        // leading edge is trimmed; a zero count within an otherwise-active
        // stretch is still a real observation and stays in the series.
        LocalDate earliestRecord = repo.earliestRecordDate(effectiveStateCode);
        if (earliestRecord == null) return List.of();
        LocalDate effectiveStart = earliestRecord.isAfter(startDate) ? earliestRecord : startDate;

        // Never include the current, still-in-progress calendar month — it
        // can only ever hold a partial count, which would poison the
        // model's last (most-recent, most-influential) observation.
        LocalDate today = bucketService.today();
        LocalDate lastCompleteMonthEnd = YearMonth.from(today).minusMonths(1).atEndOfMonth();
        LocalDate effectiveEnd = endDate.isAfter(lastCompleteMonthEnd) ? lastCompleteMonthEnd : endDate;

        if (effectiveStart.isAfter(effectiveEnd)) return List.of();

        var buckets = bucketService.buckets(effectiveStart, effectiveEnd);
        if (buckets.isEmpty()) return List.of();

        LocalDateTime fromTs = buckets.getFirst().from().atStartOfDay();
        LocalDateTime toTs = buckets.getLast().to().plusDays(1).atStartOfDay();

        List<DayCountProjection> days = repo.countCreatedByDay(fromTs, toTs, effectiveStateCode, effectiveMkb10);

        Map<LocalDate, Long> dayMap = new HashMap<>(Math.max(16, days.size()));
        for (var p : days) {
            if (p.getDay() != null) dayMap.put(p.getDay(), p.getCnt());
        }

        List<MonthSeriesPoint> out = new ArrayList<>(buckets.size());
        for (var b : buckets) {
            long sum = 0;
            for (LocalDate d = b.from(); !d.isAfter(b.to()); d = d.plusDays(1)) {
                sum += dayMap.getOrDefault(d, 0L);
            }
            out.add(new MonthSeriesPoint(b.monthNo(), b.from(), b.to(), sum));
        }
        return out;
    }

    private String resolveUserStateCode(User currentUser) {
        if (currentUser == null) return null;
        Organization org = currentUser.getCurrentOrganization();
        if (org == null) return null;
        return org.getStateCode();
    }

    private static String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
