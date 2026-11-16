package uz.uzinfocom.app.modules.airquality.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "One stored pollutant value.")
public record AirReadingResponse(
        @Schema(description = "Normalized pollutant code.", example = "PM2.5") String parameter,
        @Schema(description = "Value.", example = "25.884") BigDecimal value,
        @Schema(description = "Unit as sent by AIR_CONTROL.", example = "µg/m³") String unit,
        @Schema(description = "Measurement time (station local time).") LocalDateTime measuredAt
) {
}
