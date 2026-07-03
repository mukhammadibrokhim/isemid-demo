package uz.uzinfocom.isemid.features.forecast.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeekBucketService {

    // ISO-8601: weeks run Monday-Sunday, and a week belongs to whichever
    // calendar year owns its Thursday (equivalently: the week containing
    // Jan 4th is week 1). A week can straddle a calendar-year boundary —
    // e.g. the ISO week containing 2024-12-31 is week 1 of 2025, with its
    // Monday on 2024-12-30. weekNo below is WeekFields.ISO's
    // week-of-week-based-year (1..52, or 53 in long years), not a plain
    // sequential counter, so it reflects the actual calendar week rather
    // than being relative to whatever startDate a given query happened to use.
    private static final WeekFields ISO = WeekFields.ISO;

    public record WeekBucket(int weekNo, LocalDate from, LocalDate to) {}

    public LocalDate today() {
        return LocalDate.now();
    }

    // Snaps both ends to full ISO weeks (Monday..Sunday) so every returned
    // bucket is a complete week — the caller may see a few extra days before
    // startDate or after endDate as a result, matching MonthBucketService's
    // analogous month-boundary snapping.
    public List<WeekBucket> buckets(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) throw new IllegalArgumentException("startDate is required");
        if (endDate == null) throw new IllegalArgumentException("endDate is required");
        if (startDate.isAfter(endDate)) return List.of();

        List<WeekBucket> res = new ArrayList<>();
        LocalDate cur = startDate.with(DayOfWeek.MONDAY);
        LocalDate lastWeekStart = endDate.with(DayOfWeek.MONDAY);

        while (!cur.isAfter(lastWeekStart)) {
            int weekNo = cur.get(ISO.weekOfWeekBasedYear());
            res.add(new WeekBucket(weekNo, cur, cur.plusDays(6)));
            cur = cur.plusWeeks(1);
        }
        return res;
    }
}
