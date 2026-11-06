package uz.uzinfocom.app.integration.outbound.monitoring.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * The time frame of one dynamics request: the instants cases are filtered
 * by, and the first/last bucket start of the series (local
 * {@code Asia/Tashkent} time) so periods without cases still show up.
 * <p>
 * The upper bound is clamped to "now", so a range ending today or later
 * stops at the current hour/day/week/month instead of listing empty future
 * buckets. A range that starts in the future is rejected.
 */
public record MonitoringDynamicsWindow(
        MonitoringGranularity granularity,
        Instant fromInclusive,
        Instant toExclusive,
        LocalDateTime seriesStart,
        LocalDateTime seriesEnd,
        LocalDate weekAnchor
) {

    public static final ZoneId ZONE = ZoneId.of("Asia/Tashkent");

    public static MonitoringDynamicsWindow of(LocalDate from, LocalDate to, ZonedDateTime now) {
        LocalDateTime fromLocal = from.atStartOfDay();
        LocalDateTime toExclusiveLocal = to.plusDays(1).atStartOfDay();
        LocalDateTime nowLocal = now.withZoneSameInstant(ZONE).toLocalDateTime();
        if (toExclusiveLocal.isAfter(nowLocal)) {
            toExclusiveLocal = nowLocal;
        }
        if (!toExclusiveLocal.isAfter(fromLocal)) {
            throw new IllegalArgumentException("invalid date range");
        }

        MonitoringGranularity granularity = MonitoringGranularity.forRange(from, to);
        LocalDateTime lastLocal = toExclusiveLocal.minusNanos(1);

        LocalDateTime seriesStart = switch (granularity) {
            case HOUR, DAY, WEEK -> fromLocal;
            case MONTH -> fromLocal.withDayOfMonth(1);
        };
        LocalDateTime seriesEnd = switch (granularity) {
            case HOUR -> lastLocal.truncatedTo(ChronoUnit.HOURS);
            case DAY -> lastLocal.toLocalDate().atStartOfDay();
            case WEEK -> from.plusDays(ChronoUnit.DAYS.between(from, lastLocal.toLocalDate()) / 7 * 7)
                    .atStartOfDay();
            case MONTH -> lastLocal.toLocalDate().withDayOfMonth(1).atStartOfDay();
        };

        return new MonitoringDynamicsWindow(
                granularity,
                fromLocal.atZone(ZONE).toInstant(),
                toExclusiveLocal.atZone(ZONE).toInstant(),
                seriesStart,
                seriesEnd,
                from
        );
    }
}
