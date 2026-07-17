package uz.uzinfocom.isemid.features.forecast.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class MonthBucketService {

    public record MonthBucket(int monthNo, LocalDate from, LocalDate to) {}

    public LocalDate today() {
        return LocalDate.now();
    }

    public List<MonthBucket> buckets(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) throw new IllegalArgumentException("startDate is required");
        if (endDate == null) throw new IllegalArgumentException("endDate is required");

        List<MonthBucket> res = new ArrayList<>();
        YearMonth cur = YearMonth.from(startDate);
        YearMonth last = YearMonth.from(endDate);
        int monthNo = 1;

        while (!cur.isAfter(last)) {
            res.add(new MonthBucket(monthNo++, cur.atDay(1), cur.atEndOfMonth()));
            cur = cur.plusMonths(1);
        }
        return res;
    }
}
