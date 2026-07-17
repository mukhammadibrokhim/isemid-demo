package uz.uzinfocom.isemid.features.report.dtos.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MonitoringFilter(
        LocalDate fromDate,
        LocalDate toDate,
        String regionCode,
        String mkb10Code
) {

    public LocalDateTime fromDateTime() {
        return fromDate != null
                ? fromDate.atStartOfDay()
                : null;
    }

    public LocalDateTime toDateTimeExclusive() {
        return toDate != null
                ? toDate.plusDays(1).atStartOfDay()
                : null;
    }

    public boolean hasRegionFilter() {
        return regionCode != null && !regionCode.isBlank();
    }

    public boolean hasMkb10Filter() {
        return mkb10Code != null && !mkb10Code.isBlank();
    }
}