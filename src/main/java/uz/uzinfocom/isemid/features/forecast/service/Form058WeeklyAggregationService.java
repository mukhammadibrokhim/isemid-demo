package uz.uzinfocom.isemid.features.forecast.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.forecast.models.projection.DayCountProjection;
import uz.uzinfocom.isemid.features.forecast.repositories.Form058ForecastRepository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.user.models.User;

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

    public List<WeekSeriesPoint> weeklyCounts(User currentUser, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) throw new IllegalArgumentException("startDate is required");
        if (endDate == null) endDate = bucketService.today();
        if (startDate.isAfter(endDate)) return List.of();

        // 1) buckets
        var buckets = bucketService.buckets(startDate, endDate);

        // 2) query range [from, to)
        LocalDateTime fromTs = startDate.atStartOfDay();
        LocalDateTime toTs = endDate.plusDays(1).atStartOfDay();

        // 3) scope
        ScopeParams scope = resolveScope(currentUser);

        // 4) daily counts from DB
        List<DayCountProjection> days = repo.countCreatedByDayScoped(
                fromTs, toTs,
                scope.levelType, scope.stateCode, scope.cityCode
        );

        Map<LocalDate, Long> dayMap = new HashMap<>(days.size());
        for (var p : days) dayMap.put(p.getDay(), p.getCnt());

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

    private record ScopeParams(String levelType, String stateCode, String cityCode) {
    }

    private ScopeParams resolveScope(User currentUser) {
        Organization org = currentUser != null ? currentUser.getCurrentOrganization() : null;
        if (org == null) return new ScopeParams("REPUBLICAN", null, null);
        if (org.getMedicalType() != MedicalType.SANEPID_SERVICE) return new ScopeParams("REPUBLICAN", null, null);

        LevelType levelType = org.getLevelType() != null ? org.getLevelType() : LevelType.NOT_DEFINED;

        String stateCode = org.getStateCode();

        String cityCode = org.getCityCode();
        if (cityCode == null && org.getCity() != null) {
            cityCode = org.getCity().getCode();
        }

        return new ScopeParams(levelType.name(), stateCode, cityCode);
    }
}
