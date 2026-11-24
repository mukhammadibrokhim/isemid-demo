package uz.uzinfocom.app.modules.airquality.application.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A station's newest reading of a pollutant crossed above that pollutant's norm: the previous
 * stored reading was within the norm (or there was none). Edge-triggered on purpose - a
 * sustained exceedance raises one event, not one per hourly sync.
 * <p>
 * Published inside the writing transaction, so listeners should be {@code AFTER_COMMIT}.
 * {@code regionCode}/{@code districtCode} are the station's resolved territory and may be
 * {@code null}; {@code value} and {@code limit} are both in µg/m³.
 */
public record AirQualityExceededEvent(
        Long stationId,
        String stationName,
        String regionCode,
        String districtCode,
        String parameter,
        BigDecimal value,
        BigDecimal limit,
        BigDecimal ratio,
        LocalDateTime measuredAt
) {
}
