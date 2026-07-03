package uz.uzinfocom.isemid.features.forecast.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.forecast.models.projection.DayCountProjection;
import uz.uzinfocom.isemid.features.forecast.repositories.Form058ForecastRepository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Form058WeeklyAggregationService {

    private final Form058ForecastRepository repo;
    private final WeekBucketService bucketService;

    public record WeekSeriesPoint(int weekNo, LocalDate from, LocalDate to, long y) {
    }

    public List<WeekSeriesPoint> weeklyCounts(
            User currentUser,
            LocalDate startDate,
            LocalDate endDate,
            String stateCode,      // optional override
            String mkb10Code       // optional
    ) {
        if (startDate == null) throw new IllegalArgumentException("startDate is required");
        if (endDate == null) endDate = bucketService.today();
        if (startDate.isAfter(endDate)) return List.of();

        // effective filters (param overrides user org)
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

        // Never include the current, still-in-progress ISO week — it can
        // only ever hold a partial count, which would poison the model's
        // last (most-recent, most-influential) observation.
        LocalDate today = bucketService.today();
        LocalDate lastCompleteWeekEnd = today.with(DayOfWeek.MONDAY).minusDays(1);
        LocalDate effectiveEnd = endDate.isAfter(lastCompleteWeekEnd) ? lastCompleteWeekEnd : endDate;

        if (effectiveStart.isAfter(effectiveEnd)) return List.of();

        // 1) buckets — snapped to full ISO weeks (Monday..Sunday)
        var buckets = bucketService.buckets(effectiveStart, effectiveEnd);
        if (buckets.isEmpty()) return List.of();

        // 2) query range [from, to), aligned to the same ISO-week boundaries
        // the buckets use, so every bucket is fully covered by the query.
        LocalDateTime fromTs = buckets.getFirst().from().atStartOfDay();
        LocalDateTime toTs = buckets.getLast().to().plusDays(1).atStartOfDay();

        // 3) effective filters resolved above; daily counts from DB
        List<DayCountProjection> days = repo.countCreatedByDay(
                fromTs, toTs,
                effectiveStateCode,
                effectiveMkb10
        );

        Map<LocalDate, Long> dayMap = new HashMap<>(Math.max(16, days.size()));
        for (var p : days) {
            if (p.getDay() != null) dayMap.put(p.getDay(), p.getCnt());
        }

        // 5) sum daily -> weekly
        List<WeekSeriesPoint> out = new ArrayList<>(buckets.size());
        for (var b : buckets) {
            long sum = 0;
            for (LocalDate d = b.from(); !d.isAfter(b.to()); d = d.plusDays(1)) {
                sum += dayMap.getOrDefault(d, 0L);
            }
            out.add(new WeekSeriesPoint(b.weekNo(), b.from(), b.to(), sum));
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
