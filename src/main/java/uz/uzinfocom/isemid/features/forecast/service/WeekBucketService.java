package uz.uzinfocom.isemid.features.forecast.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeekBucketService {

    public record WeekBucket(int weekNo, LocalDate from, LocalDate to) {
    }

    public LocalDate today() {
        return LocalDate.now();
    }

    public List<WeekBucket> buckets(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) throw new IllegalArgumentException("startDate is required");
        if (endDate == null) throw new IllegalArgumentException("endDate is required");
        if (startDate.isAfter(endDate)) return List.of();

        List<WeekBucket> res = new ArrayList<>();
        int weekNo = 1;

        for (LocalDate cur = startDate; !cur.isAfter(endDate); cur = cur.plusDays(7)) {
            LocalDate to = cur.plusDays(6);
            if (to.isAfter(endDate)) to = endDate;
            res.add(new WeekBucket(weekNo++, cur, to));
        }
        return res;
    }
}
