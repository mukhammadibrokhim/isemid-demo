package uz.uzinfocom.app.modules.airquality.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.airquality.application.AirStatus;

import java.math.BigDecimal;

@Schema(description = "One pollutant of a station's latest measurement, compared with its norm.")
public record AirReadingStatusResponse(
        @Schema(description = "Normalized pollutant code.", example = "PM2.5") String parameter,
        @Schema(description = "Value as sent by AIR_CONTROL.", example = "31.5") BigDecimal value,
        @Schema(description = "Unit as sent by AIR_CONTROL.", example = "µg/m³") String unit,
        @Schema(description = "The pollutant's norm in µg/m³; null when it has none.", example = "15") BigDecimal limit,
        @Schema(description = "value / limit (both in µg/m³); null when it cannot be computed.", example = "2.1000") BigDecimal ratio,
        @Schema(description = "NO_DATA / GOOD (<=0.5) / MODERATE (<=1) / EXCEEDED (<=2) / SEVERE (>2).") AirStatus status
) {
}
